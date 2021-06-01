package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.google.gson.Gson;
import com.mytlogos.enterprise.model.DisplayRelease;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.preferences.UserPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EpisodeViewModel extends RepoViewModel implements MediumFilterableViewModel {

    private LiveData<PagedList<DisplayRelease>> episodes;
    private LiveData<List<MediaList>> listLiveData;
    private MutableLiveData<Filter> filter = new MutableLiveData<>();

    public EpisodeViewModel(@NonNull Application application) {
        super(application);
        String episodesFilter = UserPreferences.getEpisodesFilter();
        Filter filter = new Gson().fromJson(episodesFilter, Filter.class);
        this.filter.setValue(filter != null ? filter : new Filter());
        this.filter.observeForever(newFilter -> {
            String json = new Gson().toJson(newFilter);
            UserPreferences.setEpisodesFilter(json);
        });
    }

    public LiveData<PagedList<DisplayRelease>> getDisplayEpisodes() {
        if (this.episodes == null) {
            this.episodes = Transformations.switchMap(this.filter, input -> {
                System.out.println("filtering episodes after: " + input);
//                if (input.grouped) {
//                    return repository.getDisplayEpisodesGrouped(input.saved, input.medium);
//                } else {
                return repository.getDisplayEpisodes(input);
//                }
            });
        }
        return this.episodes;
    }

    public void setSaved(int saved) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setSaved(saved).build());
    }

    public void setGrouped(boolean grouped) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setGrouped(grouped).build());
    }

    public void setRead(int read) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setRead(read).build());
    }

    public void setMinIndex(int minIndex) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setMinIndex(minIndex).build());
    }

    public void setMaxIndex(int maxIndex) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setMaxIndex(maxIndex).build());
    }

    public void setHost(String host) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setHost(host).build());
    }

    public void setLatestOnly(boolean latestOnly) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setLatestOnly(latestOnly).build());
    }

    public void setFilterListIds(List<Integer> filterListIds) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setFilterListIds(filterListIds).build());
    }

    @Override
    public void setMediumFilter(int filter) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setMedium(filter).build());
    }

    @Override
    public int getMediumFilter() {
        Filter value = this.filter.getValue();
        return value == null ? 0 : value.medium;
    }

    public int getSaved() {
        Filter value = this.filter.getValue();
        return value != null ? value.saved : -1;
    }

    public int getRead() {
        Filter value = this.filter.getValue();
        return value != null ? value.read : -1;
    }

    public int getMaxIndex() {
        Filter value = this.filter.getValue();
        return value != null ? value.maxIndex : -1;
    }

    public int getMinIndex() {
        Filter value = this.filter.getValue();
        return value != null ? value.minIndex : -1;
    }

    public String getHost() {
        Filter value = this.filter.getValue();
        return value != null ? value.host : null;
    }

    public boolean isLatestOnly() {
        Filter value = this.filter.getValue();
        return value != null && value.latestOnly;
    }

    public List<Integer> getFilterListIds() {
        Filter value = this.filter.getValue();
        return value != null ? new ArrayList<>(value.filterListIds) : Collections.emptyList();
    }

    public LiveData<List<MediaList>> getLists() {
        if (this.listLiveData == null) {
            this.listLiveData = this.repository.getInternLists();
        }
        return this.listLiveData;
    }

    private static class FilterBuilder {
        private boolean grouped;
        private int medium;
        private int saved;
        private int read;
        private int minIndex;
        private int maxIndex;
        private String host;
        private boolean latestOnly;
        private List<Integer> filterListIds;

        private FilterBuilder(Filter filter) {
            if (filter == null) {
                filter = new Filter();
            }
            this.grouped = filter.grouped;
            this.saved = filter.saved;
            this.medium = filter.medium;
            this.read = filter.read;
            this.maxIndex = filter.maxIndex;
            this.minIndex = filter.minIndex;
            this.host = filter.host;
            this.latestOnly = filter.latestOnly;
            this.filterListIds = filter.filterListIds;
        }

        private FilterBuilder setGrouped(boolean grouped) {
            this.grouped = grouped;
            return this;
        }

        private FilterBuilder setMedium(int medium) {
            this.medium = medium;
            return this;
        }

        private FilterBuilder setSaved(int saved) {
            this.saved = saved;
            return this;
        }

        private FilterBuilder setRead(int read) {
            this.read = read;
            return this;
        }

        public FilterBuilder setMinIndex(int minIndex) {
            this.minIndex = minIndex;
            return this;
        }

        public FilterBuilder setMaxIndex(int maxIndex) {
            this.maxIndex = maxIndex;
            return this;
        }

        public FilterBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public FilterBuilder setLatestOnly(boolean latestOnly) {
            this.latestOnly = latestOnly;
            return this;
        }

        public FilterBuilder setFilterListIds(List<Integer> filterListIds) {
            this.filterListIds = filterListIds;
            return this;
        }

        private Filter build() {
            return new Filter(this.grouped, this.medium, this.saved, this.read, this.minIndex, this.maxIndex, this.host, this.latestOnly, this.filterListIds);
        }
    }


    public static class Filter {
        public final boolean grouped;
        public final int medium;
        public final int saved;
        public final int read;
        public final int minIndex;
        public final int maxIndex;
        public final String host;
        public final boolean latestOnly;
        public final List<Integer> filterListIds;

        private Filter(boolean grouped, int medium, int saved, int read, int minIndex, int maxIndex, String host, boolean latestOnly, List<Integer> filterListIds) {
            this.grouped = grouped;
            this.medium = medium;
            this.saved = saved > 0 ? 1 : saved;
            this.read = read > 0 ? 1 : read;
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
            this.host = host;
            this.latestOnly = latestOnly;
            this.filterListIds = Collections.unmodifiableList(filterListIds);
        }

        private Filter() {
            this(false, 0, -1, -1, -1, -1, null, false, Collections.emptyList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Filter filter = (Filter) o;

            if (grouped != filter.grouped) return false;
            if (medium != filter.medium) return false;
            if (saved != filter.saved) return false;
            if (read != filter.read) return false;
            if (minIndex != filter.minIndex) return false;
            if (maxIndex != filter.maxIndex) return false;
            if (latestOnly != filter.latestOnly) return false;
            if (!Objects.equals(host, filter.host)) return false;
            return filterListIds.equals(filter.filterListIds);
        }

        @Override
        public int hashCode() {
            int result = (grouped ? 1 : 0);
            result = 31 * result + medium;
            result = 31 * result + saved;
            result = 31 * result + read;
            result = 31 * result + minIndex;
            result = 31 * result + maxIndex;
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + (latestOnly ? 1 : 0);
            result = 31 * result + filterListIds.hashCode();
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "Filter{" +
                    "grouped=" + grouped +
                    ", medium=" + medium +
                    ", saved=" + saved +
                    ", read=" + read +
                    ", minIndex=" + minIndex +
                    ", maxIndex=" + maxIndex +
                    ", host='" + host + '\'' +
                    ", latestOnly=" + latestOnly +
                    ", filterListIds=" + filterListIds +
                    '}';
        }
    }

}
