package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.NotificationItem;

public class NotificationViewModel extends RepoViewModel {

    NotificationViewModel(Application application) {
        super(application);
    }

    public LiveData<PagedList<NotificationItem>> getNotifications() {
        return this.repository.getNotifications();
    }

    public void clearNotifications() {
        TaskManager.runTask(this.repository::clearNotifications);
    }
}
