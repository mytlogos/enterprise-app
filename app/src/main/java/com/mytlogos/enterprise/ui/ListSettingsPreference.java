package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.mytlogos.enterprise.R;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListSettingsPreference extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.list_preferences, rootKey);

        Bundle arguments = getArguments();

        if (arguments == null) {
            throw new IllegalArgumentException("no arguments available");
        }

        String prefix = arguments.getString("prefix");
        int intId = arguments.getInt("id");
        String stringId = arguments.getString("id");

        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("invalid 'prefix' argument: empty or null");
        }

        if ((intId == 0) && (stringId == null || stringId.isEmpty())) {
            throw new IllegalArgumentException("invalid 'id' argument: zero, empty or null");
        }
        PreferenceManager manager = this.getPreferenceManager();
        ItemPreferenceDataStore store;

        if (intId != 0) {
            store = new ItemPreferenceDataStore(this.getContext(), prefix, intId);
        } else {
            store = new ItemPreferenceDataStore(this.getContext(), prefix, stringId);
        }
        manager.setPreferenceDataStore(store);
    }


    public static class ItemPreferenceDataStore extends PreferenceDataStore {

        private final SharedPreferences sharedPreferences;
        private final String prefixKey;
        private final int intId;
        private final String stringId;
        private final String prefix;

        public ItemPreferenceDataStore(Context context, String prefixKey, int intId) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.prefixKey = prefixKey;
            this.intId = intId;
            this.stringId = null;
            this.prefix = getPrefix(prefixKey, intId);
        }

        public ItemPreferenceDataStore(Context context, String prefixKey, String stringId) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.prefixKey = prefixKey;
            this.intId = 0;
            this.stringId = stringId;
            this.prefix = getPrefix(prefixKey, stringId);
        }

        public String getPrefixKey() {
            return prefixKey;
        }

        public int getIntId() {
            return intId;
        }

        public String getStringId() {
            return stringId;
        }

        private static Pattern intIdPattern = Pattern.compile("\\w+-(\\d+)-.+");
        private static Pattern stringIdPattern = Pattern.compile("\\w+-(\\w+)-.+");
        private static Pattern prefixKeyPattern = Pattern.compile("(\\w+)-(\\w+|\\d+)-.+");

        public static String getPrefixKey(String key) {
            Matcher matcher = prefixKeyPattern.matcher(key);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        }

        public static int getIntId(String key) {
            Matcher matcher = intIdPattern.matcher(key);
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 0;
        }

        public static String getStringId(String key) {
            Matcher matcher = stringIdPattern.matcher(key);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        }

        public static String transformKey(String key, String prefix) {
            return prefix + key;
        }

        public static String getPrefix(String prefix, int id) {
            return prefix + "-" + id + "-";
        }

        public static String getPrefix(String prefix, String id) {
            return prefix + "-" + id + "-";
        }

        @Override
        public void putString(String key, @Nullable String value) {
            this.sharedPreferences
                    .edit()
                    .putString(transformKey(key, this.prefix), value)
                    .apply();
        }

        @Override
        public void putStringSet(String key, @Nullable Set<String> values) {
            this.sharedPreferences
                    .edit()
                    .putStringSet(transformKey(key, this.prefix), values)
                    .apply();
        }

        @Override
        public void putInt(String key, int value) {
            this.sharedPreferences
                    .edit()
                    .putInt(transformKey(key, this.prefix), value)
                    .apply();
        }

        @Override
        public void putLong(String key, long value) {
            this.sharedPreferences
                    .edit()
                    .putLong(transformKey(key, this.prefix), value)
                    .apply();
        }

        @Override
        public void putFloat(String key, float value) {
            this.sharedPreferences
                    .edit()
                    .putFloat(transformKey(key, this.prefix), value)
                    .apply();
        }

        @Override
        public void putBoolean(String key, boolean value) {
            this.sharedPreferences
                    .edit()
                    .putBoolean(transformKey(key, this.prefix), value)
                    .apply();
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return sharedPreferences.getString(transformKey(key, this.prefix), defValue);
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return sharedPreferences.getStringSet(transformKey(key, this.prefix), defValues);
        }

        @Override
        public int getInt(String key, int defValue) {
            return sharedPreferences.getInt(transformKey(key, this.prefix), defValue);
        }

        @Override
        public long getLong(String key, long defValue) {
            return sharedPreferences.getLong(transformKey(key, this.prefix), defValue);
        }

        @Override
        public float getFloat(String key, float defValue) {
            return sharedPreferences.getFloat(transformKey(key, this.prefix), defValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return sharedPreferences.getBoolean(transformKey(key, this.prefix), defValue);
        }
    }
}

