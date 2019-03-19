package com.mytlogos.enterprise.background;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Resource<T> {
    @NonNull
    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String message;

    private Resource(@NonNull Status status, @Nullable T data,
                     @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    public enum Status {SUCCESS, ERROR, LOADING}
}
