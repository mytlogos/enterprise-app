package com.mytlogos.enterprise.background

interface ClientConsumer<T> {
    val type: Class<T>?
    fun consume(data: Collection<T>?)
}