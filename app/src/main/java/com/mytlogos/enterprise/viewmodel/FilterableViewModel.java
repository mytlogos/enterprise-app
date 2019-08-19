package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

public abstract class FilterableViewModel extends RepoViewModel {

    FilterableViewModel(Application application) {
        super(application);
    }

    String processStringFilter(String filter) {
        if (filter != null && filter.isEmpty()) {
            filter = null;
        }
        if (filter != null) {
            filter = filter.toLowerCase();
        }
        return filter;
    }

    public abstract void resetFilter();
}
