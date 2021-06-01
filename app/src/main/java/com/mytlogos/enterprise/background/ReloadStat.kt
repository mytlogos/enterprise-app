package com.mytlogos.enterprise.background

class ReloadStat(
    val loadPartEpisodes: Collection<Int>,
    val loadPartReleases: Collection<Int>,
    val loadMediumTocs: Collection<Int>,
    val loadMedium: Collection<Int>,
    val loadPart: Collection<Int>,
    val loadLists: Collection<Int>,
    val loadExUser: Collection<String>
) {
    override fun hashCode(): Int {
        var result = loadPartEpisodes.hashCode()
        result = 31 * result + loadPartReleases.hashCode()
        result = 31 * result + loadMediumTocs.hashCode()
        result = 31 * result + loadMedium.hashCode()
        result = 31 * result + loadPart.hashCode()
        result = 31 * result + loadLists.hashCode()
        result = 31 * result + loadExUser.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReloadStat) return false
        if (loadPartEpisodes != other.loadPartEpisodes) return false
        if (loadPartReleases != other.loadPartReleases) return false
        if (loadMediumTocs != other.loadMediumTocs) return false
        if (loadMedium != other.loadMedium) return false
        if (loadPart != other.loadPart) return false
        return if (loadLists != other.loadLists) false else loadExUser == other.loadExUser
    }

    override fun toString(): String {
        return "ReloadStat{" +
                "loadPartEpisodes=" + loadPartEpisodes +
                ", loadPartReleases=" + loadPartReleases +
                ", loadMediumTocs=" + loadMediumTocs +
                ", loadMedium=" + loadMedium +
                ", loadPart=" + loadPart +
                ", loadLists=" + loadLists +
                ", loadExUser=" + loadExUser +
                '}'
    }
}