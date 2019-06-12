package com.mytlogos.enterprise.model;

public class ExternalMediaListSetting extends MediaListSetting {
    private final String url;

    public ExternalMediaListSetting(int listId, String uuid, String name, int medium, String url, int size, boolean toDownload) {
        super(listId, uuid, name, medium, size, toDownload);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean isNameMutable() {
        return false;
    }

    @Override
    public boolean isMediumMutable() {
        return false;
    }
}
