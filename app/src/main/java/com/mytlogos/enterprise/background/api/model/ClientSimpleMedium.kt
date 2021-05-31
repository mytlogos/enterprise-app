package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * API Model for SimpleMedium.
 */
public class ClientSimpleMedium {
    private int id;
    private String languageOfOrigin;
    private String countryOfOrigin;
    private String author;
    private String title;
    private int medium;
    private String artist;
    private String lang;
    private int stateOrigin;
    private int stateTL;
    private String series;
    private String universe;

    public ClientSimpleMedium() {

    }

    public ClientSimpleMedium(ClientMedium medium) {
        this.id = medium.getId();
        this.countryOfOrigin = medium.getCountryOfOrigin();
        this.languageOfOrigin = medium.getLanguageOfOrigin();
        this.author = medium.getAuthor();
        this.title = medium.getTitle();
        this.medium = medium.getMedium();
        this.artist = medium.getArtist();
        this.lang = medium.getLang();
        this.stateOrigin = medium.getStateOrigin();
        this.stateTL = medium.getStateTL();
        this.series = medium.getSeries();
        this.universe = medium.getUniverse();
    }

    public ClientSimpleMedium(int id, String countryOfOrigin, String languageOfOrigin, String author,
                              String title, int medium, String artist, String lang, int stateOrigin,
                              int stateTL, String series, String universe) {
        this.id = id;
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

    public ClientSimpleMedium(int id, String title, int medium) {
        this.id = id;
        this.title = title;
        this.medium = medium;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientSimpleMedium{" +
                "id=" + id +
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

        ClientSimpleMedium that = (ClientSimpleMedium) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    public int getId() {
        return id;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public String getLanguageOfOrigin() {
        return languageOfOrigin;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public int getMedium() {
        return medium;
    }

    public String getArtist() {
        return artist;
    }

    public String getLang() {
        return lang;
    }

    public int getStateOrigin() {
        return stateOrigin;
    }

    public int getStateTL() {
        return stateTL;
    }

    public String getSeries() {
        return series;
    }

    public String getUniverse() {
        return universe;
    }
}
