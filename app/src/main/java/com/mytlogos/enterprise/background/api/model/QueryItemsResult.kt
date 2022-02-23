package com.mytlogos.enterprise.background.api.model

class QueryItemsResult(
    val episodeReleases: List<ClientRelease>, // by episode id
    val episodes: List<ClientEpisode>,
    val partEpisodes: Map<String, List<Int>>, // by part id
    val partReleases: Map<String, List<ClientSimpleRelease>>, // by part id
    val parts: List<ClientPart>,
    val media: List<ClientSimpleMedium>,
    val tocs: List<ClientToc>, // by toc id
    val mediaTocs: List<ClientToc>, // by medium id
    val mediaLists: List<ClientMediaList>,
    val externalMediaLists: List<ClientExternalMediaList>,
    val externalUser: List<ClientExternalUser>,
)