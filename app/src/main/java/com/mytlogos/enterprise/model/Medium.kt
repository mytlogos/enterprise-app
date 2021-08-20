package com.mytlogos.enterprise.model

interface Medium {
    val currentRead: Int?
    val mediumId: Int
    val countryOfOrigin: String
    val languageOfOrigin: String
    val author: String
    val title: String
    val medium: Int
    val artist: String
    val lang: String
    val stateOrigin: Int
    val stateTL: Int
    val series: String
    val universe: String
}