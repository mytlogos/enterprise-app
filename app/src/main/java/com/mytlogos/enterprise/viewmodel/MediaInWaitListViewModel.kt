package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.tools.Sortings;

import java.io.IOException;

public class MediaInWaitListViewModel extends FilterableViewModel implements SortableViewModel, MediumFilterableViewModel {

    private LiveData<PagedList<MediumInWait>> mediaInWait;
    private MutableLiveData<FilterSort> filterSortLiveData = new MutableLiveData<>();

    public MediaInWaitListViewModel(Application application) {
        super(application);
    }

    public LiveData<PagedList<MediumInWait>> getMediaInWait() {
        if (this.mediaInWait == null) {
            this.mediaInWait = Transformations.switchMap(
                    this.filterSortLiveData,
                    input -> {
                        if (input == null) {
                            return repository.getMediaInWaitBy(
                                    null,
                                    0,
                                    null,
                                    Sortings.TITLE_AZ
                            );
                        } else {
                            return repository.getMediaInWaitBy(
                                    input.titleFilter,
                                    input.mediumFilter,
                                    input.hostFilter,
                                    input.sortings
                            );
                        }
                    }
            );
            this.filterSortLiveData.setValue(new FilterSort());
        }
        return this.mediaInWait;
    }

    public void loadMediaInWait() throws IOException {
        this.repository.loadMediaInWaitSync();
    }

    @Override
    public void setSort(Sortings sort) {
        FilterSort value = filterSortLiveData.getValue();

        if (value != null) {
            value = new FilterSort(sort, value.mediumFilter, value.titleFilter, value.hostFilter);
        } else {
            value = new FilterSort(sort, 0, null, null);
        }
        filterSortLiveData.setValue(value);
    }

    public void setTitleFilter(String filter) {
        FilterSort value = filterSortLiveData.getValue();

        filter = processStringFilter(filter);
        if (value != null) {
            value = new FilterSort(value.sortings, value.mediumFilter, filter, value.hostFilter);
        } else {
            value = new FilterSort(Sortings.TITLE_AZ, 0, filter, null);
        }
        filterSortLiveData.setValue(value);
    }

    public void setHostFilter(String filter) {
        FilterSort value = filterSortLiveData.getValue();

        filter = processStringFilter(filter);
        if (value != null) {
            value = new FilterSort(value.sortings, value.mediumFilter, value.titleFilter, filter);
        } else {
            value = new FilterSort(Sortings.TITLE_AZ, 0, null, filter);
        }
        filterSortLiveData.setValue(value);
    }

    @Override
    public void setMediumFilter(int filter) {
        FilterSort value = filterSortLiveData.getValue();

        if (value != null) {
            value = new FilterSort(value.sortings, filter, value.titleFilter, value.hostFilter);
        } else {
            value = new FilterSort(Sortings.TITLE_AZ, filter, null, null);
        }
        filterSortLiveData.setValue(value);
    }

    public String getTitleFilter() {
        FilterSort value = this.filterSortLiveData.getValue();
        return value == null ? "" : value.titleFilter == null ? "" : value.titleFilter;
    }

    public String getHostFilter() {
        FilterSort value = this.filterSortLiveData.getValue();
        return value == null ? "" : value.hostFilter == null ? "" : value.hostFilter;
    }

    @Override
    public int getMediumFilter() {
        FilterSort value = this.filterSortLiveData.getValue();
        return value == null ? 0 : value.mediumFilter < 0 ? 0 : value.mediumFilter;
    }

    public void resetFilter() {
        FilterSort sort;
        if (this.filterSortLiveData.getValue() != null) {
            sort = new FilterSort(this.filterSortLiveData.getValue().sortings);
        } else {
            sort = new FilterSort();
        }
        this.filterSortLiveData.setValue(sort);
    }

    private static class FilterSort {
        private final Sortings sortings;
        private final int mediumFilter;
        private final String titleFilter;
        private final String hostFilter;

        private FilterSort(Sortings sortings, int mediumFilter, String titleFilter, String hostFilter) {
            this.sortings = sortings;
            this.mediumFilter = mediumFilter;
            this.titleFilter = titleFilter;
            this.hostFilter = hostFilter;
        }

        FilterSort() {
            this(Sortings.TITLE_AZ);
        }

        FilterSort(Sortings sortings) {
            this(sortings, 0, null, null);
        }
    }
}
