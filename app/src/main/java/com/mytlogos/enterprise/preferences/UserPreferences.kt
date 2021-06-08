package com.mytlogos.enterprise.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.joda.time.DateTime

class UserPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    val downloadPreference: DownloadPreference
        get() = DownloadPreference(sharedPreferences)

    companion object {
        private var INSTANCE: UserPreferences? = null
        private const val LOGGED_STATUS = "user_login_status"
        private const val LOGGED_USER = "user_login_uuid"
        private const val LAST_SYNC = "last_time_sync"
        private const val EPISODES_FILTER = "episodes_filter"

        @JvmStatic
        @Synchronized
        fun init(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserPreferences(context)
            }
        }

        fun get(): UserPreferences {
            checkNotNull(INSTANCE) { "UserPreference not yet initialized!" }
            return INSTANCE!!
        }

        @JvmStatic
        var episodesFilter: String?
            get() = get().sharedPreferences.getString(EPISODES_FILTER, null)
            set(filter) {
                val editor = get().sharedPreferences.edit()
                editor.putString(EPISODES_FILTER, filter)
                editor.apply()
            }

        var lastSync: DateTime
            get() {
                val lastSyncString = get().sharedPreferences.getString(LAST_SYNC, null)
                return if (lastSyncString == null) DateTime(0) else DateTime.parse(
                    lastSyncString)
            }
            set(lastSyncDatetime) {
                val editor = get().sharedPreferences.edit()
                editor.putString(LAST_SYNC, lastSyncDatetime.toString())
                editor.apply()
            }

        @JvmStatic
        val loggedStatus: Boolean
            get() = get().sharedPreferences.getBoolean(LOGGED_STATUS, false)

        val loggedUuid: String?
            get() = get().sharedPreferences.getString(LOGGED_USER, null)

        @JvmStatic
        fun putLoggedStatus(loggedIn: Boolean) {
            val editor = get().sharedPreferences.edit()
            editor.putBoolean(LOGGED_STATUS, loggedIn)
            editor.apply()
        }

        @JvmStatic
        fun putLoggedUuid(uuid: String?) {
            val editor = get().sharedPreferences.edit()
            editor.putString(LOGGED_USER, uuid)
            editor.apply()
        }
    }

    init {
        if (INSTANCE != null) {
            throw IllegalAccessError("Do not instantiate UserPreferences!")
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
}