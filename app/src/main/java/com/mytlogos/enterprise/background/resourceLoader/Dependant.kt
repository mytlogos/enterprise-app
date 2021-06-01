package com.mytlogos.enterprise.background.resourceLoader

interface Dependant {
    val value: Any
    val runBefore: Runnable?
}