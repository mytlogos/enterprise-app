package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.tools.Sortings;

import java.io.IOException;

public class TocEpisodeViewModel extends FilterableViewModel implements SortableViewModel {
    private MutableLiveData<SortFilter> sortFilterLiveData = new MutableLiveData<>();
    private LiveData<PagedList<TocEpisode>> repositoryToc;

    TocEpisodeViewModel(Application application) {
        super(application);
        this.resetFilter();
    }

    @Override
    public void resetFilter() {
        this.sortFilterLiveData.setValue(new Builder(null).createSortFilter());
    }

    @Override
    public void setSort(Sortings sort) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new Builder(value).setSortings(sort).createSortFilter());
    }

    public void setReadFilter(byte readFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new Builder(value).setRead(readFilter).createSortFilter());
    }

    public void setSavedFilter(byte savedFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new Builder(value).setSaved(savedFilter).createSortFilter());
    }

    public byte getSavedFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? -1 : value.saved;
    }

    public byte getReadFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? -1 : value.read;
    }

    public LiveData<PagedList<TocEpisode>> getToc(int mediumId) {
        if (this.repositoryToc == null) {
            this.repositoryToc = Transformations.switchMap(
                    sortFilterLiveData,
                    input -> repository.getToc(mediumId, input.sortings, input.read, input.saved)
            );
        }
        return this.repositoryToc;
    }

    public void deleteAllLocalEpisodes(int mediumId) throws IOException {
        repository.deleteAllLocalEpisodes(mediumId, this.getApplication());
    }

    public void deleteLocalEpisodesWithLowerIndex(int episodeId, int mediumId) throws IOException {
        repository.deleteLocalEpisodesWithLowerIndex(episodeId, mediumId, this.getApplication());
    }

    public void deleteLocalEpisode(int episodeId, int mediumId) throws IOException {
        repository.deleteLocalEpisode(episodeId, mediumId, this.getApplication());
    }

    public void updateAllRead(int mediumId, boolean read) throws IOException {
        repository.updateAllRead(mediumId, read);
    }

    public void updateReadWithLowerIndex(int episodeId, boolean read) throws IOException {
        repository.updateReadWithLowerIndex(episodeId, read);
    }

    public void updateRead(int episodeId, boolean read) throws IOException {
        repository.updateRead(episodeId, read);
    }

    private static class Builder {

        private Sortings sortings;
        private byte read;
        private byte saved;

        Builder(SortFilter sortFilter) {
            if (sortFilter == null) {
                sortings = Sortings.INDEX_DESC;
                read = -1;
                saved = -1;
            } else {
                sortings = sortFilter.sortings;
                read = sortFilter.read;
                saved = sortFilter.saved;
            }
        }

        public Builder setSortings(Sortings sortings) {
            this.sortings = sortings;
            return this;
        }

        public Builder setRead(byte read) {
            if (read < -1) {
                read = -1;
            } else if (read > 1) {
                read = 1;
            }
            this.read = read;
            return this;
        }

        public Builder setSaved(byte saved) {
            if (saved < -1) {
                saved = -1;
            } else if (saved > 1) {
                saved = 1;
            }
            this.saved = saved;
            return this;
        }

        public SortFilter createSortFilter() {
            return new SortFilter(sortings, read, saved);
        }
    }

    private static class SortFilter {
        private final Sortings sortings;
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        private final byte read;
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        private final byte saved;

        private SortFilter(Sortings sortings, byte read, byte saved) {
            this.sortings = sortings;
            this.read = read;
            this.saved = saved;
        }
    }
}
