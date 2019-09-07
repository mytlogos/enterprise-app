package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.NewsViewModel;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.utils.LayoutUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class NewsFragment extends BaseSwipeListFragment<News, NewsViewModel> {

    private AttachedListener attachedListener;
    private ScheduledFuture<?> attachedFuture;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.list);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        Runnable attachedTask = new Runnable() {
            @Override
            public void run() {
                if (attachedListener == null) {
                    attachedFuture = service.schedule(this, 2, TimeUnit.SECONDS);
                    return;
                }
                attachedListener.handle();
            }
        };
        attachedFuture = service.schedule(attachedTask, 2, TimeUnit.SECONDS);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (attachedFuture != null) {
                    attachedFuture.cancel(true);
                }

                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    attachedFuture = service.schedule(attachedTask, 2, TimeUnit.SECONDS);
                }
            }
        });

        // FIXME: 22.07.2019 does not work quite correctly:
        //  -when one does not scroll
        //  -one switches to one news page a second time
        //  it does not get the correct newsItems
        this.attachedListener = () -> {
            int firstPosition = LayoutUtils.findFirstCompletelyVisibleItemPosition(recyclerView);
            int lastPosition = LayoutUtils.findLastCompletelyVisibleItemPosition(recyclerView);

            if (firstPosition == RecyclerView.NO_POSITION || lastPosition == RecyclerView.NO_POSITION) {
                return;
            }
            DateTime minimumVisible = DateTime.now().minusSeconds(2);
            List<Integer> readNews = new ArrayList<>();

            for (int i = firstPosition; i <= lastPosition; i++) {
                IFlexible item = getFlexibleAdapter().getItem(i);

                if (!(item instanceof NewsItem)) {
                    continue;
                }
                NewsItem newsItem = (NewsItem) item;
                if (newsItem.attached == null) {
                    continue;
                }
                if (newsItem.attached.isBefore(minimumVisible) && !newsItem.news.isRead()) {
                    readNews.add(newsItem.news.getId());
                }
            }
            if (!readNews.isEmpty()) {
                this.getViewModel().markNewsRead(readNews);
            }
        };
        this.setTitle("News");
        return view;
    }

    @Override
    Class<NewsViewModel> getViewModelClass() {
        return NewsViewModel.class;
    }

    @Override
    LiveData<PagedList<News>> createPagedListLiveData() {
        return this.getViewModel().getNews();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<News> list) {
        if (list == null) {
            return null;
        }

        List<IFlexible> items = new ArrayList<>();
        for (News news : list) {
            if (news == null) {
                break;
            }
            NewsItem item = new NewsItem(news);
            item.listener = this.attachedListener;
            items.add(item);
        }
        return items;
    }

    @Override
    void onSwipeRefresh() {
        List<News> news = this.getLivePagedList().getValue();
        DateTime latest = null;

        if (news != null) {
            Comparator<News> newsComparator = Comparator.nullsLast((o1, o2) -> o1.getTimeStamp().compareTo(o2.getTimeStamp()));
            News latestNews = Collections.max(news, newsComparator);
            latest = latestNews == null ? null : latestNews.getTimeStamp();
        }
        this.getViewModel().refresh(latest).observe(this, loadingComplete -> {
            if (loadingComplete != null && loadingComplete) {
                getListContainer().setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onItemClick(View view, int position) {
        IFlexible item = getFlexibleAdapter().getItem(position);
        if (!(item instanceof NewsItem)) {
            return false;
        }
        String url = ((NewsItem) item).news.getUrl();
        this.openInBrowser(url);
        return false;
    }

    @FunctionalInterface
    private interface AttachedListener {
        void handle();

    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {
        private final LocalDate date;

        private HeaderItem(LocalDate date) {
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderItem)) return false;

            HeaderItem other = (HeaderItem) o;
            return this.date.equals(other.date);
        }

        @Override
        public int hashCode() {
            return date.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.flexible_header;
        }

        @Override
        public HeaderViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new HeaderViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, HeaderViewHolder holder, int position, List<Object> payloads) {
            holder.textView.setText(this.date.toString("E, dd.MM.yyyy"));
        }
    }

    private static class HeaderViewHolder extends FlexibleViewHolder {
        private TextView textView;

        HeaderViewHolder(@NonNull View itemView, FlexibleAdapter<IFlexible> adapter) {
            super(itemView, adapter, true);
            textView = itemView.findViewById(R.id.text);
        }
    }

    private static class NewsItem extends AbstractSectionableItem<MetaViewHolder, HeaderItem> {
        private final News news;
        private DateTime attached;
        private AttachedListener listener;

        NewsItem(@NonNull News news) {
            super(new HeaderItem(news.getTimeStamp().toLocalDate()));
            this.news = news;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NewsItem)) return false;

            NewsItem other = (NewsItem) o;
            return this.news.getId() == other.news.getId();
        }

        @Override
        public int hashCode() {
            return this.news.getId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.meta_item;
        }

        @Override
        public MetaViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new MetaViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position, List<Object> payloads) {
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.topLeftText.setText(this.news.getTimeStamp().toString("HH:mm:ss"));
            holder.mainText.setText(this.news.getTitle());
            holder.topRightText.setText(Utils.getDomain(this.news.getUrl()));
        }

        @Override
        public void onViewAttached(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position) {
            super.onViewAttached(adapter, holder, position);
            this.attached = DateTime.now();
        }

        @Override
        public void onViewDetached(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position) {
            super.onViewDetached(adapter, holder, position);
            this.attached = null;

            if (this.listener != null) {
                this.listener.handle();
            }
        }
    }
}
