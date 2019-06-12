package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public class MediumSetting {
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
    private final int currentRead;
    private final int currentReadEpisode;
    private final int lastEpisode;
    private final DateTime lastUpdated;
    private final boolean toDownload;

    public MediumSetting(String title, int mediumId, String author, String artist, int medium, int stateTL, int stateOrigin, String countryOfOrigin, String languageOfOrigin, String lang, String series, String universe, int currentRead, int currentReadEpisode, int lastEpisode, DateTime lastUpdated, boolean toDownload) {
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
        this.toDownload = toDownload;
    }

    public String getTitle() {
        return title;
    }

    public int getMediumId() {
        return mediumId;
    }

    public String getAuthor() {
        return author;
    }

    public String getArtist() {
        return artist;
    }

    public int getMedium() {
        return medium;
    }

    public int getStateTL() {
        return stateTL;
    }

    public int getStateOrigin() {
        return stateOrigin;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public String getLanguageOfOrigin() {
        return languageOfOrigin;
    }

    public String getLang() {
        return lang;
    }

    public String getSeries() {
        return series;
    }

    public String getUniverse() {
        return universe;
    }

    public int getCurrentRead() {
        return currentRead;
    }

    public int getCurrentReadEpisode() {
        return currentReadEpisode;
    }

    public int getLastEpisode() {
        return lastEpisode;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public boolean isToDownload() {
        return toDownload;
    }

    public static class MediumSettingBuilder {
        private String title;
        private int mediumId;
        private String author;
        private String artist;
        private int medium;
        private int stateTL;
        private int stateOrigin;
        private String countryOfOrigin;
        private String languageOfOrigin;
        private String lang;
        private String series;
        private String universe;
        private int currentRead;
        private int currentReadEpisode;
        private int lastEpisode;
        private DateTime lastUpdated;
        private boolean toDownload;

        public MediumSettingBuilder(MediumSetting setting) {
            this.title = setting.title;
            this.mediumId = setting.mediumId;
            this.author = setting.author;
            this.artist = setting.artist;
            this.medium = setting.medium;
            this.stateTL = setting.stateTL;
            this.stateOrigin = setting.stateOrigin;
            this.countryOfOrigin = setting.countryOfOrigin;
            this.languageOfOrigin = setting.languageOfOrigin;
            this.lang = setting.lang;
            this.series = setting.series;
            this.universe = setting.universe;
            this.currentRead = setting.currentRead;
            this.currentReadEpisode = setting.currentReadEpisode;
            this.lastEpisode = setting.lastEpisode;
            this.lastUpdated = setting.lastUpdated;
            this.toDownload = setting.toDownload;
        }

        public MediumSettingBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public MediumSettingBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public MediumSettingBuilder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public MediumSettingBuilder setMedium(int medium) {
            this.medium = medium;
            return this;
        }

        public MediumSettingBuilder setStateTL(int stateTL) {
            this.stateTL = stateTL;
            return this;
        }

        public MediumSettingBuilder setStateOrigin(int stateOrigin) {
            this.stateOrigin = stateOrigin;
            return this;
        }

        public MediumSettingBuilder setCountryOfOrigin(String countryOfOrigin) {
            this.countryOfOrigin = countryOfOrigin;
            return this;
        }

        public MediumSettingBuilder setLanguageOfOrigin(String languageOfOrigin) {
            this.languageOfOrigin = languageOfOrigin;
            return this;
        }

        public MediumSettingBuilder setLang(String lang) {
            this.lang = lang;
            return this;
        }

        public MediumSettingBuilder setSeries(String series) {
            this.series = series;
            return this;
        }

        public MediumSettingBuilder setUniverse(String universe) {
            this.universe = universe;
            return this;
        }

        public MediumSettingBuilder setCurrentRead(int currentRead) {
            this.currentRead = currentRead;
            return this;
        }

        public MediumSettingBuilder setCurrentReadEpisode(int currentReadEpisode) {
            this.currentReadEpisode = currentReadEpisode;
            return this;
        }

        public MediumSettingBuilder setLastEpisode(int lastEpisode) {
            this.lastEpisode = lastEpisode;
            return this;
        }

        public MediumSettingBuilder setLastUpdated(DateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public MediumSettingBuilder setToDownload(boolean toDownload) {
            this.toDownload = toDownload;
            return this;
        }

        public MediumSetting createMediumSetting() {
            return new MediumSetting(title, mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, currentRead, currentReadEpisode, lastEpisode, lastUpdated, toDownload);
        }
    }
}
