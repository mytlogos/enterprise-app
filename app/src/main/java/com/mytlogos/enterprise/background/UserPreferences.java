package com.mytlogos.enterprise.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserPreferences {
    private static String LOGGED_STATUS = "user_login_status";
    private static String LOGGED_USER = "user_login_uuid";

    private UserPreferences() throws IllegalAccessException {
        throw new IllegalAccessException("Do not instantiate UserPreferences!");
    }

    private static SharedPreferences getPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getLoggedStatus(Context context) {
        return UserPreferences.getPreference(context).getBoolean(LOGGED_STATUS, false);
    }

    public static String getLoggedUuid(Context context) {
        return UserPreferences.getPreference(context).getString(LOGGED_USER, null);
    }

    public static void putLoggedStatus(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = UserPreferences.getPreference(context).edit();
        editor.putBoolean(LOGGED_STATUS, loggedIn);
        editor.apply();
    }

    public static void putLoggedUuid(Context context, String uuid) {
        SharedPreferences.Editor editor = UserPreferences.getPreference(context).edit();
        editor.putString(LOGGED_USER, uuid);
        editor.apply();
    }


}
