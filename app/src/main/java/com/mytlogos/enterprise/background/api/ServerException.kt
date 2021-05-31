package com.mytlogos.enterprise.background.api

import android.annotation.SuppressLint
import java.io.IOException

class ServerException : IOException {
    val responseCode: Int
    val errorMessage: String?

    @SuppressLint("DefaultLocale")
    constructor(responseCode: Int, errorMessage: String?) : super(String.format("No Body, Http-Code %d, ErrorBody: %s", responseCode, errorMessage)) {
        this.responseCode = responseCode
        this.errorMessage = errorMessage
    }

    constructor(cause: Throwable?, responseCode: Int, errorMessage: String?) : super(cause) {
        this.responseCode = responseCode
        this.errorMessage = errorMessage
    }

    constructor(cause: ServerException) : super(cause) {
        responseCode = cause.responseCode
        errorMessage = cause.errorMessage
    }
}