package com.mytlogos.enterprise.background.api

import java.io.IOException

class NotConnectedException : IOException {
    constructor() {}
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}