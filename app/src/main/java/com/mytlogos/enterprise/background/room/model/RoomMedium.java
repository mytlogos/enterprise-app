package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.Medium;

import java.util.List;
import java.util.Objects;

@Entity(
        foreignKeys = @ForeignKey(
                parentColumns = "episodeId",
                childColumns = "currentRead",
                onDelete = ForeignKey.SET_NULL,
                entity = RoomEpisode.class
        ),
        indices = {
                @Index(value = "currentRead"),
                @Index(value = "mediumId"),
        }
)
public class RoomMedium implements Medium {
    @Ignore
    private List<Integer> parts;
    @Ignore
    private List<Integer> latestReleased;
    @Ignore
    private List<Integer> unreadEpisodes;
    @PrimaryKey
    private int mediumId;
    private Integer currentRead;
    private String countryOfOrigin;
    private String languageOfOrigin;
    private String author;
    private String title;
    private int medium;
    private String artist;
    private String lang;
    private int stateOrigin;
    private int stateTL;
    private String series;
    private String universe;

    public RoomMedium(Integer currentRead, int mediumId, String countryOfOrigin, String languageOfOrigin, String author, String title, int medium, String artist, String lang, int stateOrigin, int stateTL, String series, String universe) {
        this.currentRead = currentRead;
        this.mediumId = mediumId;
        this.countryOfOrigin = countryOfOrigin;
        this.languageOfOrigin = languageOfOrigin;
        this.author = author;
        this.title = title;
        this.medium = medium;
        this.artist = artist;
        this.lang = lang;
        this.stateOrigin = stateOrigin;
        this.stateTL = stateTL;
        this.series = series;
        this.universe = universe;
    }

    @Override
    public String toString() {
        return "RoomMedium{" +
                "parts=" + parts +
                ", latestReleased=" + latestReleased +
                ", unreadEpisodes=" + unreadEpisodes +
                ", mediumId=" + mediumId +
                ", currentRead=" + currentRead +
                ", countryOfOrigin='" + countryOfOrigin + '\'' +
                ", languageOfOrigin='" + languageOfOrigin + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", medium=" + medium +
                ", artist='" + artist + '\'' +
                ", lang='" + lang + '\'' +
                ", stateOrigin=" + stateOrigin +
                ", stateTL=" + stateTL +
                ", series='" + series + '\'' +
                ", universe='" + universe + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomMedium that = (RoomMedium) o;

        if (getMediumId() != that.getMediumId()) return false;
        if (!Objects.equals(getCurrentRead(), that.getCurrentRead())) return false;
        if (getMedium() != that.getMedium()) return false;
        if (getStateOrigin() != that.getStateOrigin()) return false;
        if (getStateTL() != that.getStateTL()) return false;
        if (parts != null ? !parts.equals(that.parts) : that.parts != null) return false;
        if (latestReleased != null ? !latestReleased.equals(that.latestReleased) : that.latestReleased != null)
            return false;
        if (unreadEpisodes != null ? !unreadEpisodes.equals(that.unreadEpisodes) : that.unreadEpisodes != null)
            return false;
        if (getCountryOfOrigin() != null ? !getCountryOfOrigin().equals(that.getCountryOfOrigin()) : that.getCountryOfOrigin() != null)
            return false;
        if (getLanguageOfOrigin() != null ? !getLanguageOfOrigin().equals(that.getLanguageOfOrigin()) : that.getLanguageOfOrigin() != null)
            return false;
        if (getAuthor() != null ? !getAuthor().equals(that.getAuthor()) : that.getAuthor() != null)
            return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        if (getArtist() != null ? !getArtist().equals(that.getArtist()) : that.getArtist() != null)
            return false;
        if (getLang() != null ? !getLang().equals(that.getLang()) : that.getLang() != null)
            return false;
        if (getSeries() != null ? !getSeries().equals(that.getSeries()) : that.getSeries() != null)
            return false;
        return getUniverse() != null ? getUniverse().equals(that.getUniverse()) : that.getUniverse() == null;
    }

    @Override
    public int hashCode() {
        int result = parts != null ? parts.hashCode() : 0;
        result = 31 * result + (latestReleased != null ? latestReleased.hashCode() : 0);
        result = 31 * result + (unreadEpisodes != null ? unreadEpisodes.hashCode() : 0);
        result = 31 * result + getMediumId();
        result = 31 * result + getCurrentRead();
        result = 31 * result + (getCountryOfOrigin() != null ? getCountryOfOrigin().hashCode() : 0);
        result = 31 * result + (getLanguageOfOrigin() != null ? getLanguageOfOrigin().hashCode() : 0);
        result = 31 * result + (getAuthor() != null ? getAuthor().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + getMedium();
        result = 31 * result + (getArtist() != null ? getArtist().hashCode() : 0);
        result = 31 * result + (getLang() != null ? getLang().hashCode() : 0);
        result = 31 * result + getStateOrigin();
        result = 31 * result + getStateTL();
        result = 31 * result + (getSeries() != null ? getSeries().hashCode() : 0);
        result = 31 * result + (getUniverse() != null ? getUniverse().hashCode() : 0);
        return result;
    }

    @Override
    public Integer getCurrentRead() {
        return currentRead;
    }

    @Override
    public int getMediumId() {
        return mediumId;
    }

    @Override
    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    @Override
    public String getLanguageOfOrigin() {
        return languageOfOrigin;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getMedium() {
        return medium;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public int getStateOrigin() {
        return stateOrigin;
    }

    @Override
    public int getStateTL() {
        return stateTL;
    }

    @Override
    public String getSeries() {
        return series;
    }

    @Override
    public String getUniverse() {
        return universe;
    }
}
