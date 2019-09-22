package com.mytlogos.enterprise.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserPreferences {
    private static UserPreferences INSTANCE;
    private static String LOGGED_STATUS = "user_login_status";
    private static String LOGGED_USER = "user_login_uuid";
    private SharedPreferences sharedPreferences;

    private UserPreferences(Context context) {
        if (INSTANCE != null) {
            throw new IllegalAccessError("Do not instantiate UserPreferences!");
        }
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static UserPreferences get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences(context);
        }
        return INSTANCE;
    }

    public DownloadPreference getDownloadPreference() {
        return new DownloadPreference(this.sharedPreferences);
    }

    public static boolean getLoggedStatus(Context context) {
        return UserPreferences.get(context).sharedPreferences.getBoolean(LOGGED_STATUS, false);
    }

    public static String getLoggedUuid(Context context) {
        return UserPreferences.get(context).sharedPreferences.getString(LOGGED_USER, null);
    }

    public static void putLoggedStatus(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = UserPreferences.get(context).sharedPreferences.edit();
        editor.putBoolean(LOGGED_STATUS, loggedIn);
        editor.apply();
    }

    public static void putLoggedUuid(Context context, String uuid) {
        SharedPreferences.Editor editor = UserPreferences.get(context).sharedPreferences.edit();
        editor.putString(LOGGED_USER, uuid);
        editor.apply();
    }


}
