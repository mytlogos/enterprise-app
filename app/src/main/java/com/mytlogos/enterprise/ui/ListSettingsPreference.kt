package com.mytlogos.enterprise.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mytlogos.enterprise.R
import java.util.regex.Pattern

/**
 * TODO: decide what to do with this, delete it or replace [ListSettings] with this one
 */
class ListSettingsPreference : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.list_preferences, rootKey)
        val arguments = arguments ?: throw IllegalArgumentException("no arguments available")
        val prefix = arguments.getString("prefix")
        val intId = arguments.getInt("id")
        val stringId = arguments.getString("id")
        require(!(prefix == null || prefix.isEmpty())) { "invalid 'prefix' argument: empty or null" }
        require(!(intId == 0 && (stringId == null || stringId.isEmpty()))) { "invalid 'id' argument: zero, empty or null" }
        val manager = this.preferenceManager
        val store: ItemPreferenceDataStore = if (intId != 0) {
            ItemPreferenceDataStore(this.context, prefix, intId)
        } else {
            ItemPreferenceDataStore(this.context, prefix, stringId)
        }
        manager.preferenceDataStore = store
    }

    class ItemPreferenceDataStore : PreferenceDataStore {
        private val sharedPreferences: SharedPreferences
        val prefixKey: String
        val intId: Int
        val stringId: String?
        private val prefix: String

        constructor(context: Context?, prefixKey: String, intId: Int) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            this.prefixKey = prefixKey
            this.intId = intId
            stringId = null
            prefix = getPrefix(prefixKey, intId)
        }

        constructor(context: Context?, prefixKey: String, stringId: String?) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            this.prefixKey = prefixKey
            intId = 0
            this.stringId = stringId
            prefix = getPrefix(prefixKey, stringId)
        }

        override fun putString(key: String, value: String?) {
            sharedPreferences
                .edit()
                .putString(transformKey(key, prefix), value)
                .apply()
        }

        override fun putStringSet(key: String, values: Set<String>?) {
            sharedPreferences
                .edit()
                .putStringSet(transformKey(key, prefix), values)
                .apply()
        }

        override fun putInt(key: String, value: Int) {
            sharedPreferences
                .edit()
                .putInt(transformKey(key, prefix), value)
                .apply()
        }

        override fun putLong(key: String, value: Long) {
            sharedPreferences
                .edit()
                .putLong(transformKey(key, prefix), value)
                .apply()
        }

        override fun putFloat(key: String, value: Float) {
            sharedPreferences
                .edit()
                .putFloat(transformKey(key, prefix), value)
                .apply()
        }

        override fun putBoolean(key: String, value: Boolean) {
            sharedPreferences
                .edit()
                .putBoolean(transformKey(key, prefix), value)
                .apply()
        }

        override fun getString(key: String, defValue: String?): String? {
            return sharedPreferences.getString(transformKey(key, prefix), defValue)
        }

        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
            return sharedPreferences.getStringSet(transformKey(key, prefix), defValues)
        }

        override fun getInt(key: String, defValue: Int): Int {
            return sharedPreferences.getInt(transformKey(key, prefix), defValue)
        }

        override fun getLong(key: String, defValue: Long): Long {
            return sharedPreferences.getLong(transformKey(key, prefix), defValue)
        }

        override fun getFloat(key: String, defValue: Float): Float {
            return sharedPreferences.getFloat(transformKey(key, prefix), defValue)
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            return sharedPreferences.getBoolean(transformKey(key, prefix), defValue)
        }

        companion object {
            private val intIdPattern = Pattern.compile("\\w+-(\\d+)-.+")
            private val stringIdPattern = Pattern.compile("\\w+-(\\w+)-.+")
            private val prefixKeyPattern = Pattern.compile("(\\w+)-(\\w+|\\d+)-.+")
            fun getPrefixKey(key: String?): String? {
                val matcher = prefixKeyPattern.matcher(key)
                return if (matcher.matches()) {
                    matcher.group(1)
                } else null
            }

            fun getIntId(key: String?): Int {
                val matcher = intIdPattern.matcher(key)
                return if (matcher.matches()) {
                    matcher.group(1).toInt()
                } else 0
            }

            fun getStringId(key: String?): String? {
                val matcher = stringIdPattern.matcher(key)
                return if (matcher.matches()) {
                    matcher.group(1)
                } else null
            }

            fun transformKey(key: String, prefix: String): String {
                return prefix + key
            }

            fun getPrefix(prefix: String, id: Int): String {
                return "$prefix-$id-"
            }

            fun getPrefix(prefix: String, id: String?): String {
                return "$prefix-$id-"
            }
        }
    }
}