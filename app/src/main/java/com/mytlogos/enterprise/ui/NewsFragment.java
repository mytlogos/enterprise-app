package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mytlogos.enterprise.MainActivity;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.viewmodel.NewsViewModel;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class NewsFragment extends BaseFragment {

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.news, container, false);

        MainActivity activity = this.getMainActivity();

        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(supportFragmentManager);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = view.findViewById(R.id.tab_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabs = activity.getTabLayout();
        tabs.setupWithViewPager(mViewPager);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        this.setTitle("News");
        return view;
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

    private static class NewsItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final News news;
        private final Fragment newsFragment;

        NewsItem(@NonNull News news, Fragment newsFragment) {
            super(new HeaderItem(news.getTimeStamp().toLocalDate()));
            this.news = news;
            this.newsFragment = newsFragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NewsItem)) return false;

            NewsItem other = (NewsItem) o;
            return this.news.getNewsId() == other.news.getNewsId();
        }

        @Override
        public int hashCode() {
            return this.news.getNewsId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.news_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.news;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(this.news.getTimeStamp().toString("HH:mm:ss"));

            holder.contentView.setText(this.news.getTitle());
            holder.mView.setOnClickListener(v -> {
                String url = this.news.getUrl();
                if (url == null || url.isEmpty()) {
                    Toast
                            .makeText(this.newsFragment.getContext(), "No Link available", Toast.LENGTH_SHORT)
                            .show();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                PackageManager manager = Objects.requireNonNull(this.newsFragment.getActivity()).getPackageManager();

                if (intent.resolveActivity(manager) != null) {
                    this.newsFragment.startActivity(intent);
                } else {
                    Toast
                            .makeText(this.newsFragment.getContext(), "No Browser available", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private LiveData<List<News>> newsLiveData;
        private int typeFilter = MediumType.ALL;
        private NewsViewModel newsViewModel;
        private Observer<List<News>> listObserver;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.swipe_list, container, false);

            RecyclerView recyclerView = view.findViewById(R.id.list);

            Context context = Objects.requireNonNull(getContext());

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);

            DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
            recyclerView.addItemDecoration(decoration);

            FlexibleAdapter<IFlexible> flexibleAdapter = new FlexibleAdapter<>(null)
                    .setStickyHeaders(true)
                    .setDisplayHeadersAtStartUp(true);

            recyclerView.setAdapter(flexibleAdapter);

            this.listObserver = news -> {
                TextView textView = view.findViewById(R.id.empty_view);

                if (news == null || news.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                }
                flexibleAdapter.updateDataSet(this.transformNews(news));
            };

            this.newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
            this.loadNews();

            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiper);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                List<News> news = this.newsLiveData.getValue();
                DateTime latest = null;

                if (news != null) {
                    News latestNews = Collections.max(news, (o1, o2) -> o1.getTimeStamp().compareTo(o2.getTimeStamp()));
                    latest = latestNews == null ? null : latestNews.getTimeStamp();
                }
                this.newsViewModel.refresh(latest).observe(this, loadingComplete -> {
                    if (loadingComplete != null && loadingComplete) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            });

            return view;
        }

        private List<IFlexible> transformNews(List<News> newsList) {
            if (newsList == null) {
                return null;
            }

            List<IFlexible> items = new ArrayList<>();
            for (News news : newsList) {
                if ((news.getMediumType() & this.typeFilter) == this.typeFilter) {
                    items.add(new NewsItem(news, this));
                }
            }
            return items;
        }

        private void loadNews() {
            if (this.newsLiveData != null) {
                this.newsLiveData.removeObservers(this);
            }
            if (this.newsViewModel == null) {
                return;
            }
            // todo load data according to current
            this.newsLiveData = this.newsViewModel.getNews();
            this.newsLiveData.observe(this, this.listObserver);
        }

        void setTypeFilter(int typeFilter) {
            this.typeFilter = typeFilter;
            this.loadNews();
        }
    }


    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private final int[] typeFilter;
        private final PlaceholderFragment[] fragments;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.typeFilter = new int[]{
                    MediumType.ALL,
                    MediumType.NOVEL,
                    MediumType.MANGA,
                    MediumType.ANIME,
                    MediumType.SERIES
            };
            this.fragments = new PlaceholderFragment[this.typeFilter.length];
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            PlaceholderFragment fragment = this.fragments[position];

            if (fragment == null) {
                fragment = this.fragments[position] = new PlaceholderFragment();
                fragment.setTypeFilter(this.typeFilter[position]);
            }

            return fragment;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            this.fragments[position] = null;
            super.destroyItem(container, position, object);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "All";
                case 1:
                    return "Novel";
                case 2:
                    return "Manga";
                case 3:
                    return "Anime";
                case 4:
                    return "Series";
            }
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return this.typeFilter.length;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        News mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            metaView = view.findViewById(R.id.item_meta);
            contentView = view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
