package com.mytlogos.enterprise.background

import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Supplier
import kotlin.coroutines.coroutineContext

class TaskManager private constructor() {
    private val service = Executors.newCachedThreadPool()

    companion object {
        val instance: TaskManager = TaskManager()

        fun <T> runAsyncTask(callable: Callable<T>?): Future<T> {
            return instance.service.submit(callable)
        }

        @kotlin.jvm.JvmStatic
        fun <T> runCompletableTask(supplier: Supplier<T>?): CompletableFuture<T> {
            return CompletableFuture.supplyAsync(supplier, instance.service)
        }

        @kotlin.jvm.JvmStatic
        fun runTask(task: Runnable) {
            if (Looper.getMainLooper().isCurrentThread) {
                instance.service.execute(task)
            } else {
                task.run()
            }
        }

        @kotlin.jvm.JvmStatic
        fun runTaskSuspend(task: suspend () -> Unit) {
            CoroutineScope(instance.service.asCoroutineDispatcher()).launch {
                task()
            }
        }

        fun runAsyncTask(runnable: Runnable?): Future<*> {
            return instance.service.submit(runnable)
        }
    }
}