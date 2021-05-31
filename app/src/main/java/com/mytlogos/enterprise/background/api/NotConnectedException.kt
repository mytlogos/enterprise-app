package com.mytlogos.enterprise.background.api;

import java.io.IOException;

public class NotConnectedException extends IOException {
    public NotConnectedException() {
    }

    public NotConnectedException(String message) {
        super(message);
    }

    public NotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotConnectedException(Throwable cause) {
        super(cause);
    }
}
