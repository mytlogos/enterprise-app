package com.mytlogos.enterprise.service;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.Event;
import com.mytlogos.enterprise.model.WorkerEvent;

abstract class MonitorableWorker extends Worker {

    private Observer<WorkInfo> observer;

    MonitorableWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.monitor(context);
    }

    private void monitor(Context context) {
        LiveData<WorkInfo> liveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(this.getId());
        Handler handler = new Handler(Looper.getMainLooper());
        this.observer = workInfo -> {
            if (workInfo == null) {
                return;
            }
            int type = 0;
            switch (workInfo.getState()) {
                case FAILED:
                    type = Event.WORKER_FAILED;
                    break;
                case BLOCKED:
                    break;
                case RUNNING:
                    type = Event.WORKER_RUNNING;
                    break;
                case ENQUEUED:
                    type = Event.WORKER_QUEUED;
                    break;
                case CANCELLED:
                    type = Event.WORKER_STOPPED;
                    break;
                case SUCCEEDED:
                    type = Event.WORKER_SUCCEEDED;
                    break;
            }
            if (type == 0) {
                System.err.println("unknown worker state: " + workInfo.getState());
                return;
            }
            WorkerEvent event = new WorkerEvent(type, workInfo.getId().toString(), getWorkerName(), getArguments());
            RepositoryImpl.getInstance((Application) getApplicationContext()).addWorkerEvent(event);

            if (workInfo.getState().isFinished()) {
                handler.post(() -> liveData.removeObserver(this.observer));
            }
        };
        handler.post(() -> liveData.observeForever(this.observer));
    }

    abstract String getWorkerName();

    String getArguments() {
        return null;
    }
}
