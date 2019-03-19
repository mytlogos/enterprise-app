package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.Medium;

import java.util.List;

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
    private int currentRead;
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

    public RoomMedium(int currentRead, int mediumId, String countryOfOrigin, String languageOfOrigin, String author, String title, int medium, String artist, String lang, int stateOrigin, int stateTL, String series, String universe) {
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
    public int getCurrentRead() {
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
