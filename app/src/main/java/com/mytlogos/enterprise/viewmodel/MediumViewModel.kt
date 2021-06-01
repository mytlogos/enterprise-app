package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.tools.Sortings;

import org.joda.time.DateTime;

public class MediumViewModel extends FilterableViewModel implements SortableViewModel, MediumFilterableViewModel {

    private LiveData<PagedList<MediumItem>> allMedia;
    private MutableLiveData<SortFilter> sortFilterLiveData = new MutableLiveData<>();

    public MediumViewModel(@NonNull Application application) {
        super(application);
        sortFilterLiveData.setValue(new SortFilterBuilder(null).createSortFilter());
    }

    public LiveData<PagedList<MediumItem>> getAllMedia() {
        if (this.allMedia == null) {
            this.allMedia = Transformations.switchMap(sortFilterLiveData, input -> repository.getAllMedia(
                    input.sortings,
                    input.title,
                    input.medium,
                    input.author,
                    input.lastUpdate,
                    input.minCountEpisodes,
                    input.minCountReadEpisodes
            ));
        }
        return this.allMedia;
    }

    @Override
    public void resetFilter() {
        this.sortFilterLiveData.setValue(new SortFilterBuilder(null).createSortFilter());
    }

    @Override
    public void setSort(Sortings sort) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setSortings(sort).createSortFilter());
    }

    public void setMediumFilter(int medium) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setMedium(medium).createSortFilter());
    }

    @Override
    public int getMediumFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? 0 : value.medium < 0 ? 0 : value.medium;
    }

    public void setMinReadEpisodeFilter(int minReadEpisodeFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setMinReadEpisodes(minReadEpisodeFilter).createSortFilter());
    }

    public int getMinReadEpisodeFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? -1 : value.minCountReadEpisodes;
    }

    public void setMinEpisodeFilter(int minEpisodeFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setMinCountEpisodes(minEpisodeFilter).createSortFilter());
    }

    public int getMinEpisodeFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? -1 : value.minCountEpisodes;
    }

    public void setTitleFilter(String titleFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        titleFilter = processStringFilter(titleFilter);
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setTitle(titleFilter).createSortFilter());
    }

    public String getTitleFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? "" : value.title;
    }

    public void setAuthorFilter(String titleFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        titleFilter = processStringFilter(titleFilter);
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setAuthor(titleFilter).createSortFilter());
    }


    public String getAuthorFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? "" : value.author;
    }

    public void setLastUpdateFilter(DateTime lastUpdateFilter) {
        SortFilter value = this.sortFilterLiveData.getValue();
        this.sortFilterLiveData.setValue(new SortFilterBuilder(value).setLastUpdate(lastUpdateFilter).createSortFilter());
    }

    public DateTime getLastUpdateFilter() {
        SortFilter value = this.sortFilterLiveData.getValue();
        return value == null ? null : value.lastUpdate;
    }

    private static class SortFilterBuilder {
        private Sortings sortings;
        private int medium;
        private int minReadEpisodes;
        private int minCountEpisodes;
        private String title;
        private String author;
        private DateTime lastUpdate;

        private SortFilterBuilder(SortFilter filter) {
            if (filter == null) {
                sortings = Sortings.TITLE_AZ;
                minCountEpisodes = -1;
                minReadEpisodes = -1;
            } else {
                sortings = filter.sortings;
                author = filter.author;
                lastUpdate = filter.lastUpdate;
                medium = filter.medium;
                minCountEpisodes = filter.minCountEpisodes;
                minReadEpisodes = filter.minCountReadEpisodes;
                title = filter.title;
            }
        }

        private SortFilterBuilder setSortings(Sortings sortings) {
            this.sortings = sortings;
            return this;
        }

        private SortFilterBuilder setMedium(int medium) {
            this.medium = medium;
            return this;
        }

        private SortFilterBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        private SortFilterBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        private SortFilterBuilder setMinReadEpisodes(int minReadEpisodes) {
            this.minReadEpisodes = minReadEpisodes;
            return this;
        }

        private SortFilterBuilder setMinCountEpisodes(int minCountEpisodes) {
            this.minCountEpisodes = minCountEpisodes;
            return this;
        }

        private SortFilterBuilder setLastUpdate(DateTime lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        private SortFilter createSortFilter() {
            return new SortFilter(sortings, medium, title, author, minReadEpisodes, minCountEpisodes, lastUpdate);
        }
    }

    private static class SortFilter {
        private final Sortings sortings;
        private final int medium;
        private final String title;
        private final String author;
        private final int minCountReadEpisodes;
        private final int minCountEpisodes;
        private final DateTime lastUpdate;

        private SortFilter(Sortings sortings, int medium, String title, String author, int minCountReadEpisodes, int minCountEpisodes, DateTime lastUpdate) {
            this.sortings = sortings;
            this.medium = medium;
            this.title = title;
            this.author = author;
            this.minCountReadEpisodes = minCountReadEpisodes;
            this.minCountEpisodes = minCountEpisodes;
            this.lastUpdate = lastUpdate;
        }
    }

}
