package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.mytlogos.enterprise.model.Medium;

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

        return mediumId == that.mediumId;
    }

    @Override
    public int hashCode() {
        return mediumId;
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
