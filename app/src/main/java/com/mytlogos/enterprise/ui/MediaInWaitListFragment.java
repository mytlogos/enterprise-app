package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.MediaInWaitListViewModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class MediaInWaitListFragment extends BaseSwipeListFragment<MediumInWait, MediaInWaitListViewModel> {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle("Unused Media");
        return view;
    }

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {

            @Override
            public void onCreateFilter(View view, AlertDialog.Builder builder) {
                setMediumCheckbox(view, R.id.text_medium, MediumType.TEXT);
                setMediumCheckbox(view, R.id.audio_medium, MediumType.AUDIO);
                setMediumCheckbox(view, R.id.video_medium, MediumType.VIDEO);
                setMediumCheckbox(view, R.id.image_medium, MediumType.IMAGE);
            }

            @Override
            public int getFilterLayout() {
                return R.layout.filter_medium_in_wait_layout;
            }

            @Override
            public FilterProperty[] getSearchFilterProperties() {
                return new FilterProperty[]{
                        new FilterProperty() {
                            @Override
                            public int getSearchViewId() {
                                return R.id.title_filter;
                            }

                            @Override
                            public int getClearSearchButtonId() {
                                return R.id.clear_title;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getTitleFilter();
                            }

                            @Override
                            public void set(String newFilter) {
                                getViewModel().setTitleFilter(newFilter);
                            }
                        },
                        new FilterProperty() {
                            @Override
                            public int getSearchViewId() {
                                return R.id.host_filter;
                            }

                            @Override
                            public int getClearSearchButtonId() {
                                return R.id.clear_host;
                            }

                            @Override
                            public String get() {
                                return getViewModel().getHostFilter();
                            }

                            @Override
                            public void set(String newFilter) {
                                getViewModel().setHostFilter(newFilter);
                            }
                        }
                };
            }

        };
    }

    @Override
    LinkedHashMap<String, Sortings> getSortMap() {
        LinkedHashMap<String, Sortings> map = new LinkedHashMap<>();
        map.put("Title A-Z", Sortings.TITLE_AZ);
        map.put("Title Z-A", Sortings.TITLE_ZA);
        map.put("Medium Asc", Sortings.MEDIUM);
        map.put("Medium Desc", Sortings.MEDIUM_REVERSE);
        map.put("Host A-Z", Sortings.HOST_AZ);
        map.put("Host Z-A", Sortings.HOST_ZA);
        return map;
    }

    @Override
    MediaInWaitListViewModel createViewModel() {
        return ViewModelProviders.of(this).get(MediaInWaitListViewModel.class);
    }

    @Override
    LiveData<PagedList<MediumInWait>> createPagedListLiveData() {
        return this.getViewModel().getMediaInWait();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<MediumInWait> mediumItems) {
        List<IFlexible> items = new ArrayList<>();

        for (MediumInWait item : mediumItems) {
            if (item == null) {
                break;
            }
            items.add(new MediumItem(item));
        }
        return items;
    }

    @Override
    void onSwipeRefresh() {
        new LoadingTask().execute();
    }

    @Override
    public boolean onItemClick(View view, int position) {
        MediumItem item = (MediumItem) getFlexibleAdapter().getItem(position);

        if (item == null) {
            return false;
        }
        getMainActivity().switchWindow(MediaInWaitFragment.getInstance(item.item));
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadingTask extends AsyncTask<Void, Void, Void> {
        private String errorMsg = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getViewModel().loadMediaInWait();
            } catch (IOException e) {
                errorMsg = "Loading went wrong";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (this.errorMsg != null) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
            getListContainer().setRefreshing(false);
        }
    }

    private static class MediumFilter implements Serializable {
        private final String title;
        private final int medium;
        private final String link;

        private MediumFilter(String title, int medium, String link) {
            this.title = title == null ? "" : title.toLowerCase();
            this.medium = medium;
            this.link = link == null ? "" : link.toLowerCase();
        }
    }

    private static class MediumItem extends AbstractFlexibleItem<MetaViewHolder> /*implements IFilterable<MediumFilter>*/ {
        private final MediumInWait item;

        MediumItem(@NonNull MediumInWait item) {
            this.item = item;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
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
            String mediumType;

            switch (this.item.getMedium()) {
                case MediumType.AUDIO:
                    mediumType = "Audio";
                    break;
                case MediumType.IMAGE:
                    mediumType = "Bild";
                    break;
                case MediumType.TEXT:
                    mediumType = "Text";
                    break;
                case MediumType.VIDEO:
                    mediumType = "Video";
                    break;
                default:
                    String msg = String.format("no valid medium type: %d", this.item.getMedium());
                    throw new IllegalStateException(msg);
            }
            holder.topLeftText.setText(mediumType);

            String domain = Utils.getDomain(this.item.getLink());
            holder.topRightText.setText(domain);
            holder.mainText.setText(this.item.getTitle());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediumItem that = (MediumItem) o;

            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return item != null ? item.hashCode() : 0;
        }

        public boolean filter(MediumFilter constraint) {
            if (constraint == null) {
                return true;
            }
            if (constraint.medium > 0 && (this.item.getMedium() & constraint.medium) == 0) {
                return false;
            }
            if (!constraint.link.isEmpty() && !(this.item.getLink().toLowerCase().contains(constraint.link))) {
                return false;
            }
            return constraint.title.isEmpty() || this.item.getTitle().toLowerCase().contains(constraint.title);
        }
    }

}
