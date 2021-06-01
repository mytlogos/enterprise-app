package com.mytlogos.enterprise.background.resourceLoader;

interface Dependant {
    Object getValue();

    Runnable getRunBefore();
}
