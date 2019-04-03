package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientMedium {
    private int[] parts;
    private int[] latestReleased;
    private int currentRead;
    private int[] unreadEpisodes;
    private int id;
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

    public ClientMedium(int[] parts, int[] latestReleased, int currentRead, int[] unreadEpisodes, int id,
                        String countryOfOrigin, String languageOfOrigin, String author,
                        String title, int medium, String artist, String lang, int stateOrigin,
                        int stateTL, String series, String universe) {
        this.parts = parts;
        this.latestReleased = latestReleased;
        this.currentRead = currentRead;
        this.unreadEpisodes = unreadEpisodes;

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

    public ClientMedium(int id, String title, int medium) {
        this.id = id;
        this.title = title;
        this.medium = medium;
    }

    @Override
    public String toString() {
        return "ClientMedium{" +
                "parts=" + Arrays.toString(parts) +
                ", latestReleased=" + Arrays.toString(latestReleased) +
                ", currentRead=" + currentRead +
                ", unreadEpisodes=" + Arrays.toString(unreadEpisodes) +
                ", id=" + id +
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

        ClientMedium that = (ClientMedium) o;

        if (getCurrentRead() != that.getCurrentRead()) return false;
        if (getId() != that.getId()) return false;
        if (getMedium() != that.getMedium()) return false;
        if (getStateOrigin() != that.getStateOrigin()) return false;
        if (getStateTL() != that.getStateTL()) return false;
        if (!Arrays.equals(getParts(), that.getParts())) return false;
        if (!Arrays.equals(getLatestReleased(), that.getLatestReleased())) return false;
        if (!Arrays.equals(getUnreadEpisodes(), that.getUnreadEpisodes())) return false;
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
        int result = Arrays.hashCode(getParts());
        result = 31 * result + Arrays.hashCode(getLatestReleased());
        result = 31 * result + getCurrentRead();
        result = 31 * result + Arrays.hashCode(getUnreadEpisodes());
        result = 31 * result + getId();
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

    public int[] getParts() {
        return parts;
    }

    public int[] getLatestReleased() {
        return latestReleased;
    }

    public int getCurrentRead() {
        return currentRead;
    }

    public int[] getUnreadEpisodes() {
        return unreadEpisodes;
    }
}
