package com.mytlogos.enterprise.preferences;

import android.content.SharedPreferences;

import com.mytlogos.enterprise.model.MediumType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class DownloadPreference extends BasePreference {
    private final Map<String, Set<BiConsumer<Integer, Integer>>> map = new HashMap<>();

    DownloadPreference(SharedPreferences preferences) {
        super(preferences);
    }

    public boolean isDownloadEnabled() {
        return this.preferences.getBoolean("enable_download", true);
    }

    private int getDefaultMediumCountLimit(int medium) {
        int limit = Integer.MAX_VALUE;
        if (MediumType.is(medium, MediumType.TEXT)) {
            limit = Math.min(limit, 100);
        }
        if (MediumType.is(medium, MediumType.IMAGE)) {
            limit = Math.min(limit, 10);
        }
        if (MediumType.is(medium, MediumType.VIDEO)) {
            limit = Math.min(limit, 5);
        }
        if (MediumType.is(medium, MediumType.IMAGE)) {
            limit = Math.min(limit, 20);
        }
        if (limit == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("unknown mediumType: " + medium);
        }
        return limit;
    }

    public int getDownloadLimitCount(int medium) {
        return this.getInt("download-medium-" + medium + "-count", this.getDefaultMediumCountLimit(medium));
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
