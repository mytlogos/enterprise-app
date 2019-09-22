package com.mytlogos.enterprise.preferences;

import android.content.SharedPreferences;

class BasePreference {
    final SharedPreferences preferences;

    BasePreference(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    protected int getInt(String key, int defaultV) {
        try {
            return this.preferences.getInt(key, defaultV);
        } catch (ClassCastException e) {
            String string = this.preferences.getString(key, "");

            if (string == null) {
                return defaultV;
            }
            return Integer.parseInt(string);
        }
    }

    protected float getLong(String key, long defaultV) {
        try {
            return this.preferences.getLong(key, defaultV);
        } catch (ClassCastException e) {
            String string = this.preferences.getString(key, "");

            if (string == null) {
                return defaultV;
            }
            return Long.parseLong(string);
        }
    }

    protected float getFloat(String key, float defaultV) {
        try {
            return this.preferences.getFloat(key, defaultV);
        } catch (ClassCastException e) {
            String string = this.preferences.getString(key, "");

            if (string == null) {
                return defaultV;
            }
            return Float.parseFloat(string);
        }
    }
}
