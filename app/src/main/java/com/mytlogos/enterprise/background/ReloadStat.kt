package com.mytlogos.enterprise.background

data class ReloadStat(
    val loadPartEpisodes: Collection<Int>,
    val loadPartReleases: Collection<Int>,
    val loadMediumTocs: Collection<Int>,
    val loadMedium: Collection<Int>,
    val loadPart: Collection<Int>,
    val loadLists: Collection<Int>,
    val loadExUser: Collection<String>
)