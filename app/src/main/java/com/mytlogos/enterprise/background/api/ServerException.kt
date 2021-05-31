package com.mytlogos.enterprise.background.api;

import android.annotation.SuppressLint;

import java.io.IOException;

public class ServerException extends IOException {
    public final int responseCode;
    public final String errorMessage;

    @SuppressLint("DefaultLocale")
    public ServerException(int responseCode, String errorMessage) {
        super(String.format("No Body, Http-Code %d, ErrorBody: %s", responseCode, errorMessage));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public ServerException(Throwable cause, int responseCode, String errorMessage) {
        super(cause);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public ServerException(ServerException cause) {
        super(cause);
        this.responseCode = cause.responseCode;
        this.errorMessage = cause.errorMessage;
    }
}
