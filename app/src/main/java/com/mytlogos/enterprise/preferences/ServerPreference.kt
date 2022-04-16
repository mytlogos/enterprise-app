package com.mytlogos.enterprise.preferences

import android.content.SharedPreferences

class ServerPreference internal constructor(preferences: SharedPreferences) :
    BasePreference(preferences) {

    fun autoDiscoverEnabled(): Boolean {
        return this.getBoolean("server_auto_discover", false)
    }

    fun getPossibleServer(): MutableSet<String> {
        return preferences.getStringSet("possible_client_server", mutableSetOf())!!.toMutableSet()
    }

    fun getActiveServer(): MutableSet<String> {
        return preferences.getStringSet("client_server", mutableSetOf())!!.toMutableSet()
    }

    fun getLastActiveServer(): String? {
        return preferences.getString("last_client_server", null)
    }

    fun setLastActiveServer(server: String) {
        preferences.edit().putString("last_client_server", server).apply()
    }
}