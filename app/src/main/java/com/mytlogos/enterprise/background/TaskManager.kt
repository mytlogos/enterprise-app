package com.mytlogos.enterprise.background;

import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

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

    public static <T> Future<T> runAsyncTask(Callable<T> callable) {
        return INSTANCE.service.submit(callable);
    }

    public static <T> CompletableFuture<T> runCompletableTask(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, INSTANCE.service);
    }

    public static void runTask(Runnable task) {
        if (Looper.getMainLooper().isCurrentThread()) {
            INSTANCE.service.execute(task);
        } else {
            task.run();
        }
    }

    public static Future<?> runAsyncTask(Runnable runnable) {
        return INSTANCE.service.submit(runnable);
    }
}
