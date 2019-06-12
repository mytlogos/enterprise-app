package com.mytlogos.enterprise.service;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;

import java.io.IOException;

public class SynchronizeWorker extends Worker {
    static final String SYNCHRONIZE_WORKER = "SYNCHRONIZE_WORKER";

    public SynchronizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!(this.getApplicationContext() instanceof Application)) {
            System.out.println("Context not instance of Application");
            return Result.failure();
        }
        try {
            Repository repository = RepositoryImpl.getInstance((Application) this.getApplicationContext());

            if (!repository.isClientAuthenticated()) {
                return Result.retry();
            }

            repository.loadInvalidated();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }
}
