package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.WorkerEvent;

public class WorkerEventViewModel extends FilterableViewModel {
    private final MutableLiveData<Filter> filterMutableLiveData;

    public WorkerEventViewModel(Application application) {
        super(application);
        filterMutableLiveData = new MutableLiveData<>(new Filter());
    }

    @Override
    public void resetFilter() {
        filterMutableLiveData.postValue(new Filter());
    }

    public LiveData<PagedList<WorkerEvent>> getEvents() {
        return Transformations.switchMap(filterMutableLiveData, input -> this.repository.getWorkerEvents());
    }

    public void clearAll() {
        this.repository.clearWorkerEvents();
    }

    private static class Filter {
    }
}
