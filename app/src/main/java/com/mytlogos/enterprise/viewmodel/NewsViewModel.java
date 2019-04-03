package com.mytlogos.enterprise.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.News;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

public class NewsViewModel extends AndroidViewModel {

    private final Repository repository;
    private final Handler handler;
    private final int LOADING_COMPLETE = 0x1;
    private final MutableLiveData<Boolean> loadingComplete = new MutableLiveData<>();

    public NewsViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        repository.getNews();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                loadingComplete.setValue(true);
            }
        };
    }

    public LiveData<List<News>> getNews() {
        return repository.getNews();
    }

    public void deleteOldNews() {
        repository.removeOldNews();
    }

    public LiveData<Boolean> refresh(DateTime latest) {
        TaskManager.runTask(() -> {
            try {
                repository.refreshNews(latest);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Message message = this.handler.obtainMessage(this.LOADING_COMPLETE);
                message.sendToTarget();
            }
        });
        return loadingComplete;
    }
}
