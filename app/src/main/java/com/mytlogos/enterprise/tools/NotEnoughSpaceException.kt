package com.mytlogos.enterprise.tools

import java.io.IOException

class NotEnoughSpaceException : IOException {
    constructor()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}