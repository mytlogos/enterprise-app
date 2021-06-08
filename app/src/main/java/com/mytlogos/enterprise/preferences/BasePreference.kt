package com.mytlogos.enterprise.preferences

import android.content.SharedPreferences

open class BasePreference(val preferences: SharedPreferences) {
    protected fun getInt(key: String, defaultV: Int): Int {
        return try {
            preferences.getInt(key, defaultV)
        } catch (e: ClassCastException) {
            val string = preferences.getString(key, "") ?: return defaultV
            string.toInt()
        }
    }

    protected fun getLong(key: String, defaultV: Long): Float {
        return try {
            preferences.getLong(key, defaultV).toFloat()
        } catch (e: ClassCastException) {
            val string = preferences.getString(key, "") ?: return defaultV.toFloat()
            string.toLong().toFloat()
        }
    }

    protected fun getFloat(key: String, defaultV: Float): Float {
        return try {
            preferences.getFloat(key, defaultV)
        } catch (e: ClassCastException) {
            val string = preferences.getString(key, "") ?: return defaultV
            string.toFloat()
        }
    }

    companion object {
        var IGNORE_INT_VALUE = -1
    }
}