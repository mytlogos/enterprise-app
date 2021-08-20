package com.mytlogos.enterprise.model

interface Part : Indexable {
    val partId: Int
    val title: String?
    override val totalIndex: Int
    override val partialIndex: Int
    val episodes: List<Int?>?
}