package com.mytlogos.enterprise.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;

public class UserPreferences {
    private static UserPreferences INSTANCE;
    private static String LOGGED_STATUS = "user_login_status";
    private static String LOGGED_USER = "user_login_uuid";
    private static String LAST_SYNC = "last_time_sync";
    private static String EPISODES_FILTER = "episodes_filter";
    private SharedPreferences sharedPreferences;

    private UserPreferences(Context context) {
        if (INSTANCE != null) {
            throw new IllegalAccessError("Do not instantiate UserPreferences!");
        }
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences(context);
        }
    }

    public static UserPreferences get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("UserPreference not yet initialized!");
        }
        return INSTANCE;
    }

    public DownloadPreference getDownloadPreference() {
        return new DownloadPreference(this.sharedPreferences);
    }

    public static String getEpisodesFilter() {
        return UserPreferences.get().sharedPreferences.getString(EPISODES_FILTER, null);
    }

    public static void setEpisodesFilter(String filter) {
        SharedPreferences.Editor editor = UserPreferences.get().sharedPreferences.edit();
        editor.putString(EPISODES_FILTER, filter);
        editor.apply();
    }

    public static DateTime getLastSync() {
        String last_sync_string = UserPreferences.get().sharedPreferences.getString(LAST_SYNC, null);
        return last_sync_string == null ? new DateTime(0) : DateTime.parse(last_sync_string);
    }

    public static void setLastSync(DateTime lastSyncDatetime) {
        SharedPreferences.Editor editor = UserPreferences.get().sharedPreferences.edit();
        editor.putString(LAST_SYNC, lastSyncDatetime == null ? null : lastSyncDatetime.toString());
        editor.apply();
    }

    public static boolean getLoggedStatus() {
        return UserPreferences.get().sharedPreferences.getBoolean(LOGGED_STATUS, false);
    }

    public static String getLoggedUuid() {
        return UserPreferences.get().sharedPreferences.getString(LOGGED_USER, null);
    }

    public static void putLoggedStatus(boolean loggedIn) {
        SharedPreferences.Editor editor = UserPreferences.get().sharedPreferences.edit();
        editor.putBoolean(LOGGED_STATUS, loggedIn);
        editor.apply();
    }

    public static void putLoggedUuid(String uuid) {
        SharedPreferences.Editor editor = UserPreferences.get().sharedPreferences.edit();
        editor.putString(LOGGED_USER, uuid);
        editor.apply();
    }


}
