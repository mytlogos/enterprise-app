package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.DisplayEpisode;

public class EpisodeViewModel extends RepoViewModel implements MediumFilterableViewModel {

    private LiveData<PagedList<DisplayEpisode>> unReadEpisodes;
    private MutableLiveData<Filter> filter = new MutableLiveData<>();

    public EpisodeViewModel(@NonNull Application application) {
        super(application);
        filter.setValue(new Filter());
    }

    public LiveData<PagedList<DisplayEpisode>> getUnreadEpisodes() {
        if (this.unReadEpisodes == null) {
            this.unReadEpisodes = Transformations.switchMap(filter, input -> {
                if (input.grouped) {
                    return repository.getUnReadEpisodesGrouped(input.saved, input.medium);
                } else {
                    return repository.getUnReadEpisodes(input.saved, input.medium, -1);
                }
            });
        }
        return this.unReadEpisodes;
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

    @Override
    public void setMediumFilter(int filter) {
        this.filter.setValue(new FilterBuilder(this.filter.getValue()).setMedium(filter).build());
    }

    @Override
    public int getMediumFilter() {
        Filter value = this.filter.getValue();
        return value == null ? 0 : value.medium;
    }

    public boolean getSaved() {
        Filter value = this.filter.getValue();
        return value != null && value.saved > 0;
    }

    public boolean getRead() {
        Filter value = this.filter.getValue();
        return value != null && value.read > 0;
    }

    private static class FilterBuilder {
        private boolean grouped;
        private int medium;
        private int saved;
        private int read;

        private FilterBuilder(Filter filter) {
            if (filter == null) {
                filter = new Filter();
            }
            this.grouped = filter.grouped;
            this.saved = filter.saved;
            this.medium = filter.medium;
            this.read = filter.read;
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

        private Filter build() {
            return new Filter(this.grouped, this.medium, this.saved, this.read);
        }
    }


    private static class Filter {
        private final boolean grouped;
        private final int medium;
        private final int saved;
        private final int read;

        private Filter(boolean grouped, int medium, int saved, int read) {
            this.grouped = grouped;
            this.medium = medium;
            this.saved = saved > 0 ? 1 : saved;
            this.read = read > 0 ? 1 : read;
        }

        private Filter() {
            this(false, 0, -1, -1);
        }
    }

}
