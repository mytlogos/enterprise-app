package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.SimpleMedium;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MediumInWaitViewModel extends FilterableViewModel {

    private MutableLiveData<String> mediumTitleFilterLiveData = new MutableLiveData<>();
    private MutableLiveData<String> mediumInWaitTitleFilterLiveData = new MutableLiveData<>();
    private MutableLiveData<String> listNameFilterLiveData = new MutableLiveData<>();

    MediumInWaitViewModel(Application application) {
        super(application);
    }

    @Override
    public void resetFilter() {

    }

    public LiveData<List<MediumInWait>> getSimilarMediaInWait(MediumInWait mediumInWait) {
        return repository.getSimilarMediaInWait(mediumInWait);
    }

    public LiveData<List<SimpleMedium>> getMediumSuggestions(int medium) {
        return Transformations.switchMap(
                mediumTitleFilterLiveData,
                input -> repository.getMediaSuggestions(input, medium)
        );
    }

    public LiveData<List<MediumInWait>> getMediumInWaitSuggestions(int medium) {
        return Transformations.switchMap(
                mediumInWaitTitleFilterLiveData,
                input -> repository.getMediaInWaitSuggestions(input, medium)
        );
    }

    public void setMediumInWaitTitleFilter(String titleFilter) {
        titleFilter = processStringFilter(titleFilter);
        this.mediumInWaitTitleFilterLiveData.setValue(titleFilter);
    }

    public void setMediumTitleFilter(String titleFilter) {
        titleFilter = processStringFilter(titleFilter);
        this.mediumTitleFilterLiveData.setValue(titleFilter);
    }

    public void setListNameFilter(String filter) {
        filter = processStringFilter(filter);
        this.listNameFilterLiveData.setValue(filter);
    }

    public CompletableFuture<Boolean> consumeMediumInWait(SimpleMedium selectedMedium, List<MediumInWait> mediumInWaits) {
        return repository.consumeMediumInWait(selectedMedium, mediumInWaits);
    }

    public CompletableFuture<Boolean> createMedium(MediumInWait mediumInWait, List<MediumInWait> mediumInWaits, MediaList list) {
        return this.repository.createMedium(mediumInWait, mediumInWaits, list);
    }

    public LiveData<List<MediaList>> getInternalLists() {
        return repository.getInternLists();
    }

    public LiveData<List<MediaList>> getListSuggestion() {
        return Transformations.switchMap(listNameFilterLiveData, this.repository::getListSuggestion);
    }
}
