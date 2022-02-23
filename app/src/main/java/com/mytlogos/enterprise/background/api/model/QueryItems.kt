package com.mytlogos.enterprise.background.api.model

class QueryItems(
    val episodeReleases: List<Int> = emptyList(), // by episode id
    val episodes: List<Int> = emptyList(),
    val partEpisodes: List<Int> = emptyList(), // by part id
    val partReleases: List<Int> = emptyList(), // by part id
    val parts: List<Int> = emptyList(),
    val media: List<Int> = emptyList(),
    val tocs: List<Int> = emptyList(), // by toc id
    val mediaTocs: List<Int> = emptyList(), // by medium id
    val mediaLists: List<Int> = emptyList(),
    val externalMediaLists: List<Int> = emptyList(),
    val externalUser: List<String> = emptyList(),
) {
    fun isEmpty(): Boolean {
        return episodeReleases.isEmpty()
                && episodes.isEmpty()
                && partEpisodes.isEmpty()
                && partReleases.isEmpty()
                && parts.isEmpty()
                && media.isEmpty()
                && tocs.isEmpty()
                && mediaTocs.isEmpty()
                && mediaLists.isEmpty()
                && externalMediaLists.isEmpty()
                && externalUser.isEmpty()
    }
}