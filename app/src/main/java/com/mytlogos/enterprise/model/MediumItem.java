package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public class MediumItem implements Medium {
    private final String title;
    private final int mediumId;
    private final String author;
    private final String artist;
    private final int medium;
    private final int stateTL;
    private final int stateOrigin;
    private final String countryOfOrigin;
    private final String languageOfOrigin;
    private final String lang;
    private final String series;
    private final String universe;
    private final Integer currentRead;
    private final Integer currentReadEpisode;
    private final Integer lastEpisode;
    private final DateTime lastUpdated;

    public MediumItem(String title, int mediumId, String author, String artist, int medium, int stateTL, int stateOrigin, String countryOfOrigin, String languageOfOrigin, String lang, String series, String universe, Integer currentRead, Integer currentReadEpisode, Integer lastEpisode, DateTime lastUpdated) {
        this.title = title;
        this.mediumId = mediumId;
        this.author = author;
        this.artist = artist;
        this.medium = medium;
        this.stateTL = stateTL;
        this.stateOrigin = stateOrigin;
        this.countryOfOrigin = countryOfOrigin;
        this.languageOfOrigin = languageOfOrigin;
        this.lang = lang;
        this.series = series;
        this.universe = universe;
        this.currentRead = currentRead;
        this.currentReadEpisode = currentReadEpisode;
        this.lastEpisode = lastEpisode;
        this.lastUpdated = lastUpdated;
    }

    public Integer getCurrentReadEpisode() {
        return currentReadEpisode;
    }

    public Integer getLastEpisode() {
        return lastEpisode;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
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
