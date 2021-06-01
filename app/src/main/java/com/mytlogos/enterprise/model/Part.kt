package com.mytlogos.enterprise.model

interface Part {
    val partId: Int
    val title: String?
    val totalIndex: Int
    val partialIndex: Int
    val episodes: List<Int?>?
}