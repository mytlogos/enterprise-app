package com.mytlogos.enterprise.background.resourceLoader

class DependencyTask<T> @JvmOverloads constructor(
    val idValue: T,
    val dependantValue: DependantValue?,
    val loader: NetworkLoader<T>,
    val optional: Boolean = false
)