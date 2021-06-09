package com.mytlogos.enterprise.background.api.model

/**
 * API Model for NewData.
 * TODO: missing properties: tocs: FullMediumToc[]
 */
class ClientChangedEntities {
    lateinit var media: MutableList<ClientSimpleMedium>
    lateinit var releases: MutableList<ClientRelease>
    lateinit var episodes: MutableList<ClientEpisodePure>
    lateinit var parts: MutableList<ClientPartPure>
    lateinit var lists: MutableList<ClientUserList>
    lateinit var extLists: MutableList<ClientExternalMediaListPure>
    lateinit var extUser: MutableList<ClientExternalUserPure>
    lateinit var mediaInWait: MutableList<ClientMediumInWait>
    lateinit var news: MutableList<ClientNews>

    override fun toString(): String {
        return "ClientChangedEntities{" +
                "media=" + media +
                ", releases=" + releases +
                ", episodes=" + episodes +
                ", parts=" + parts +
                ", lists=" + lists +
                ", extLists=" + extLists +
                ", extUser=" + extUser +
                ", mediaInWait=" + mediaInWait +
                ", news=" + news +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientChangedEntities
        if (media != that.media) return false
        if (releases != that.releases) return false
        if (episodes != that.episodes) return false
        if (parts != that.parts) return false
        if (lists != that.lists) return false
        if (extLists != that.extLists) return false
        if (extUser != that.extUser) return false
        return if (mediaInWait != that.mediaInWait) false else news == that.news
    }

    override fun hashCode(): Int {
        var result = media.hashCode()
        result = 31 * result + releases.hashCode()
        result = 31 * result + episodes.hashCode()
        result = 31 * result + parts.hashCode()
        result = 31 * result + lists.hashCode()
        result = 31 * result + extLists.hashCode()
        result = 31 * result + extUser.hashCode()
        result = 31 * result + mediaInWait.hashCode()
        result = 31 * result + news.hashCode()
        return result
    }
}