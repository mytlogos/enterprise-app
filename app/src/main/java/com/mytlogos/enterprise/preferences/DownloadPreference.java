package com.mytlogos.enterprise.preferences;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.mytlogos.enterprise.preferences.UserPreferences.IGNORE_INT_VALUE;

public class DownloadPreference extends BasePreference {
    private final Map<String, Set<BiConsumer<Integer, Integer>>> map = new HashMap<>();

    DownloadPreference(SharedPreferences preferences) {
        super(preferences);
    }

    public boolean isDownloadEnabled() {
        return this.preferences.getBoolean("enable_download", true);
    }

    public int getDownloadLimitCount(int medium) {
        return this.getInt("download-medium-" + medium + "-count", IGNORE_INT_VALUE);
    }

    public int getDownloadLimitSize(int medium) {
        return this.getInt("download-medium-" + medium + "-space", IGNORE_INT_VALUE);
    }

    public int getMediumDownloadLimitCount(int mediumId) {
        return this.getInt("download-mediumId-" + mediumId + "-count", IGNORE_INT_VALUE);
    }

    public int getMediumDownloadLimitSize(int mediumId) {
        return this.getInt("download-mediumId-" + mediumId + "-space", IGNORE_INT_VALUE);
    }

    public int getListDownloadLimitCount(int listId) {
        return this.getInt("download-listId-" + listId + "-count", IGNORE_INT_VALUE);
    }

    public int getListDownloadLimitSize(int listId) {
        return this.getInt("download-listId-" + listId + "-space", IGNORE_INT_VALUE);
    }
}
