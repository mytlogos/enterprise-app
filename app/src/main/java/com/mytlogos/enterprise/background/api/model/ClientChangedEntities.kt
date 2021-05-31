package com.mytlogos.enterprise.background.api.model;

import java.util.List;

/**
 * API Model for NewData.
 * TODO: missing properties: tocs: FullMediumToc[]
 */
public class ClientChangedEntities {
    public final List<ClientSimpleMedium> media;
    public final List<ClientRelease> releases;
    public final List<ClientEpisodePure> episodes;
    public final List<ClientPartPure> parts;
    public final List<ClientUserList> lists;
    public final List<ClientExternalMediaListPure> extLists;
    public final List<ClientExternalUserPure> extUser;
    public final List<ClientMediumInWait> mediaInWait;
    public final List<ClientNews> news;

    public ClientChangedEntities(List<ClientSimpleMedium> media, List<ClientRelease> releases, List<ClientEpisodePure> episodes, List<ClientPartPure> parts, List<ClientUserList> lists, List<ClientExternalMediaListPure> extLists, List<ClientExternalUserPure> extUser, List<ClientMediumInWait> mediaInWait, List<ClientNews> news) {
        this.media = media;
        this.releases = releases;
        this.episodes = episodes;
        this.parts = parts;
        this.lists = lists;
        this.extLists = extLists;
        this.extUser = extUser;
        this.mediaInWait = mediaInWait;
        this.news = news;
    }

    @Override
    public String toString() {
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientChangedEntities that = (ClientChangedEntities) o;

        if (!media.equals(that.media)) return false;
        if (!releases.equals(that.releases)) return false;
        if (!episodes.equals(that.episodes)) return false;
        if (!parts.equals(that.parts)) return false;
        if (!lists.equals(that.lists)) return false;
        if (!extLists.equals(that.extLists)) return false;
        if (!extUser.equals(that.extUser)) return false;
        if (!mediaInWait.equals(that.mediaInWait)) return false;
        return news.equals(that.news);
    }

    @Override
    public int hashCode() {
        int result = media.hashCode();
        result = 31 * result + releases.hashCode();
        result = 31 * result + episodes.hashCode();
        result = 31 * result + parts.hashCode();
        result = 31 * result + lists.hashCode();
        result = 31 * result + extLists.hashCode();
        result = 31 * result + extUser.hashCode();
        result = 31 * result + mediaInWait.hashCode();
        result = 31 * result + news.hashCode();
        return result;
    }
}
