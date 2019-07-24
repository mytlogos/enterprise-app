package com.mytlogos.enterprise.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.News;

import org.joda.time.DateTime;

import java.util.List;

public class NewsViewModel extends RepoViewModel {

    private final Handler handler;
    private final int LOADING_COMPLETE = 0x1;
    private final MutableLiveData<Boolean> loadingComplete = new MutableLiveData<>();

    public NewsViewModel(@NonNull Application application) {
        super(application);
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Message message = this.handler.obtainMessage(this.LOADING_COMPLETE);
                message.sendToTarget();
            }
        });
        return loadingComplete;
    }

    public void markNewsRead(List<Integer> readNews) {
        // TODO: 22.07.2019 send this list to client readNews
        System.out.println("read news: " + readNews);
    }
}
