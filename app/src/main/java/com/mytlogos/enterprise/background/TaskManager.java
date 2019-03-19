package com.mytlogos.enterprise.background;

import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskManager {
    private final static TaskManager INSTANCE = new TaskManager();
    private final ExecutorService service = Executors.newCachedThreadPool();

    private TaskManager() {
        if (INSTANCE != null) {
            throw new IllegalStateException("only one instance allowed");
        }
    }

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    public void runTask(Runnable task) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.service.execute(task);
        } else {
            task.run();
        }
    }

    public Future<?> runAsyncTask(Runnable runnable) {
        return service.submit(runnable);
    }

    public <T> Future<T> runAsyncTask(Callable<T> callable) {
        return service.submit(callable);
    }
}
