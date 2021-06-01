package com.mytlogos.enterprise.model;

public class HomeStats {
    private final int unreadNewsCount;
    private final int unreadChapterCount;
    private final int readTodayCount;
    private final int readTotalCount;
    private final int internalLists;
    private final int externalLists;
    private final int externalUser;
    private final int unusedMedia;
    private final int audioMedia;
    private final int videoMedia;
    private final int textMedia;
    private final int imageMedia;
    private final String name;

    public HomeStats(int unreadNewsCount, int unreadChapterCount, int readTodayCount, int readTotalCount, int internalLists, int externalLists, int externalUser, int unusedMedia, int audioMedia, int videoMedia, int textMedia, int imageMedia, String name) {
        this.unreadNewsCount = unreadNewsCount;
        this.unreadChapterCount = unreadChapterCount;
        this.readTodayCount = readTodayCount;
        this.readTotalCount = readTotalCount;
        this.internalLists = internalLists;
        this.externalLists = externalLists;
        this.externalUser = externalUser;
        this.unusedMedia = unusedMedia;
        this.audioMedia = audioMedia;
        this.videoMedia = videoMedia;
        this.textMedia = textMedia;
        this.imageMedia = imageMedia;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getUnreadNewsCount() {
        return unreadNewsCount;
    }

    public int getUnreadChapterCount() {
        return unreadChapterCount;
    }

    public int getReadTodayCount() {
        return readTodayCount;
    }

    public int getReadTotalCount() {
        return readTotalCount;
    }

    public int getInternalLists() {
        return internalLists;
    }

    public int getExternalLists() {
        return externalLists;
    }

    public int getExternalUser() {
        return externalUser;
    }

    public int getUnusedMedia() {
        return unusedMedia;
    }

    public int getAudioMedia() {
        return audioMedia;
    }

    public int getVideoMedia() {
        return videoMedia;
    }

    public int getTextMedia() {
        return textMedia;
    }

    public int getImageMedia() {
        return imageMedia;
    }
}
