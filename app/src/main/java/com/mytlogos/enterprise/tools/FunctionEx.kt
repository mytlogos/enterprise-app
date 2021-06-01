package com.mytlogos.enterprise.tools

interface FunctionEx<T, R> {
    @Throws(Exception::class)
    fun apply(t: T): R
}