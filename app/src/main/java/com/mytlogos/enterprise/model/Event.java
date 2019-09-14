package com.mytlogos.enterprise.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Event {
    public final static int WORKER_QUEUED = 1;
    public final static int WORKER_RUNNING = 2;
    public final static int WORKER_STOPPED = 3;
    public final static int WORKER_FAILED = 4;
    public final static int WORKER_SUCCEEDED = 5;
    public static final int EPISODE_SAVE_SUCCEEDED = 1;
    public static final int EPISODE_SAVE_FAILED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            EPISODE_SAVE_SUCCEEDED,
            EPISODE_SAVE_FAILED
    })
    public @interface EpisodeEvent {

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            WORKER_QUEUED,
            WORKER_RUNNING,
            WORKER_STOPPED,
            WORKER_FAILED,
            WORKER_SUCCEEDED
    })
    public @interface WorkerEvent {

    }

    public static String workerEventToString(@WorkerEvent int event) {
        switch (event) {
            case WORKER_FAILED:
                return "Failed";
            case WORKER_QUEUED:
                return "Queued";
            case WORKER_RUNNING:
                return "Running";
            case WORKER_STOPPED:
                return "Stopped";
            case WORKER_SUCCEEDED:
                return "Succeeded";
            default:
                return "Unknown Event";
        }
    }
}
