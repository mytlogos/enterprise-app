package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.DisplayRelease;

public class EpisodeViewModel extends RepoViewModel implements MediumFilterableViewModel {

    private LiveData<PagedList<DisplayRelease>> episodes;
    private MutableLiveData<Filter> filter = new MutableLiveData<>();

    public EpisodeViewModel(@NonNull Application application) {
        super(application);
        filter.setValue(new Filter());
    }

    public LiveData<PagedList<DisplayRelease>> getDisplayEpisodes() {
        if (this.episodes == null) {
            this.episodes = Transformations.switchMap(filter, input -> {
                System.out.println("filtering episodes after: " + input);
//                if (input.grouped) {
//                    return repository.getDisplayEpisodesGrouped(input.saved, input.medium);
//                } else {
                    return repository.getDisplayEpisodes(input.saved, input.medium, input.read, input.minIndex, input.maxIndex, input.latestOnly);
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

    private static class FilterBuilder {
        private boolean grouped;
        private int medium;
        private int saved;
        private int read;
        private int minIndex;
        private int maxIndex;
        private String host;
        private boolean latestOnly;

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

        private Filter build() {
            return new Filter(this.grouped, this.medium, this.saved, this.read, this.minIndex, this.maxIndex, this.host, this.latestOnly);
        }
    }


    private static class Filter {
        private final boolean grouped;
        private final int medium;
        private final int saved;
        private final int read;
        private final int minIndex;
        private final int maxIndex;
        private final String host;
        private final boolean latestOnly;

        private Filter(boolean grouped, int medium, int saved, int read, int minIndex, int maxIndex, String host, boolean latestOnly) {
            this.grouped = grouped;
            this.medium = medium;
            this.saved = saved > 0 ? 1 : saved;
            this.read = read > 0 ? 1 : read;
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
            this.host = host;
            this.latestOnly = latestOnly;
        }

        private Filter() {
            this(false, 0, -1, -1, -1, -1, null, false);
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
                    '}';
        }
    }

}
