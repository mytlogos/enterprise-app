package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.ui.ActionCount;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TocEpisodeViewModel extends FilterableViewModel implements SortableViewModel {
    private MutableLiveData<SortFilter> sortFilterLiveData = new MutableLiveData<>();
    private LiveData<PagedList<TocEpisode>> repositoryToc;

    public TocEpisodeViewModel(Application application) {
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

    public Sortings getSort() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? Sortings.INDEX_DESC : value.sortings;
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

    public void deleteLocalEpisode(Set<Integer> episodeIds, List<Double> combiIndices, ActionCount count, int mediumId) throws IOException {
        switch (count) {
            case ALL:
                repository.deleteAllLocalEpisodes(mediumId, this.getApplication());
                break;
            case CURRENT:
                repository.deleteLocalEpisodes(episodeIds, mediumId, this.getApplication());
                break;
            case CURRENT_AND_ONWARDS:
                double lowest = getLowest(combiIndices);
                repository.deleteLocalEpisodesWithHigherIndex(lowest, mediumId, this.getApplication());
                break;
            case CURRENT_AND_PREVIOUSLY:
                double highest = getHighest(combiIndices);
                repository.deleteLocalEpisodesWithLowerIndex(highest, mediumId, this.getApplication());
                break;
        }
    }

    public void updateRead(Set<Integer> episodeIds, List<Double> combiIndices, ActionCount count, int mediumId, boolean read) throws Exception {
        switch (count) {
            case ALL:
                repository.updateAllRead(mediumId, read);
                break;
            case CURRENT:
                repository.updateRead(episodeIds, read);
                break;
            case CURRENT_AND_ONWARDS:
                double lowest = getLowest(combiIndices);
                repository.updateReadWithHigherIndex(lowest, read, mediumId);
                break;
            case CURRENT_AND_PREVIOUSLY:
                double highest = getHighest(combiIndices);
                repository.updateReadWithLowerIndex(highest, read, mediumId);
                break;
        }
    }

    public void reload(Set<Integer> episodeIds, List<Double> combiIndices, ActionCount count, int mediumId) throws Exception {
        switch (count) {
            case ALL:
                repository.reloadAll(mediumId);
                break;
            case CURRENT:
                repository.reload(episodeIds);
                break;
            case CURRENT_AND_ONWARDS:
                double lowest = getLowest(combiIndices);
                repository.reloadHigherIndex(lowest, mediumId);
                break;
            case CURRENT_AND_PREVIOUSLY:
                double highest = getHighest(combiIndices);
                repository.reloadLowerIndex(highest, mediumId);
                break;
        }
    }

    public void download(Set<Integer> episodeIds, List<Double> combiIndices, ActionCount count, int mediumId) {
        switch (count) {
            case ALL:
                repository.downloadAll(mediumId, this.getApplication());
                break;
            case CURRENT:
                repository.download(episodeIds, mediumId, this.getApplication());
                break;
            case CURRENT_AND_ONWARDS:
                double lowest = getLowest(combiIndices);
                repository.downloadHigherIndex(lowest, mediumId, this.getApplication());
                break;
            case CURRENT_AND_PREVIOUSLY:
                double highest = getHighest(combiIndices);
                repository.downloadLowerIndex(highest, mediumId, this.getApplication());
                break;
        }
    }

    private double getLowest(List<Double> combiIndices) {
        double lowest = Integer.MAX_VALUE;
        for (Double index : combiIndices) {
            lowest = Math.min(index, lowest);
        }
        return lowest;
    }

    private double getHighest(List<Double> combiIndices) {
        double highest = Integer.MIN_VALUE;
        for (Double index : combiIndices) {
            highest = Math.max(index, highest);
        }
        return highest;
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
