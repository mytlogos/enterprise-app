package com.mytlogos.enterprise

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import androidx.fragment.app.clearFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.ServerDiscovery
import kotlinx.coroutines.launch

/**
 * A [PreferenceFragmentCompat] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_headers)
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"))
            bindPreferenceSummaryToValue(findPreference("example_list"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsFragment::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class NotificationPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//            addPreferencesFromResource(R.xml.pref_notification)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsFragment::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DataSyncPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_data_sync)
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsFragment::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DownloadPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_download)
            bindPreferenceSummaryToValue(findPreference("download-medium-1-count"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-2-count"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-4-count"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-8-count"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-1-space"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-2-space"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-4-space"), true)
            bindPreferenceSummaryToValue(findPreference("download-medium-8-space"), true)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsFragment::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }


    /**
     * This fragment shows preferences for the server.
     */
    class ServerPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        fun addPossibleServer(toAdd: Collection<String>, pref: MultiSelectListPreference) {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())
            val current = sharedPreferences.getStringSet("possible_client_server", mutableSetOf())!!.toMutableSet()
            current.addAll(toAdd)
            pref.entries = current.toTypedArray()
            pref.entryValues = pref.entries

            val editor = sharedPreferences.edit()
            editor.putStringSet("possible_client_server", current)
            editor.apply()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_server)

            val discover: Preference? = findPreference("discover_server")
            val discovered: MultiSelectListPreference? = findPreference("discovered_server")
            val clientServer: MultiSelectListPreference? = findPreference("client_server")
            val custom: EditTextPreference? = findPreference("add_custom")

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())
            println(sharedPreferences.all)

            val possibleServer = sharedPreferences.getStringSet("possible_client_server", setOf())
            clientServer?.entries = possibleServer!!.toTypedArray()
            clientServer?.entryValues = clientServer?.entries

            custom?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is String && newValue.isNotBlank()) {
                    this.addPossibleServer(setOf(newValue), clientServer!!)
                }
                custom.text = ""
                true
            }

            discovered?.entries = arrayOf()
            discovered?.entryValues = arrayOf()
            discovered?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is MutableSet<*>) {
                    this.addPossibleServer(newValue as Collection<String>, clientServer!!)
                } else {
                    println("unknown newvalue for discovered server")
                }
                true
             }

            discover?.setOnPreferenceClickListener {
                val broadcastAddress =
                    AndroidNetworkIdentificator(requireContext()).broadcastAddress

                this.lifecycleScope.launch {
                    val server = ServerDiscovery().discover(broadcastAddress)

                    discovered?.entries = server.map { it.address }.toTypedArray()
                    discovered?.entryValues = server.map { it.address }.toTypedArray()
                    discovered?.values = setOf()
                }
                true
            }
            bindPreferenceSummaryToValue(clientServer)
            bindPreferenceSummaryToValue(findPreference("server_auto_discover"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsFragment::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference: Preference, value: Any? ->
                val stringValue: String = if (value == null || value is Number && value.toLong() < 0
                    || value is String && value.matches(Regex("-\\d+"))
                ) {
                    "No Limit"
                } else {
                    value.toString()
                }
                println("preference $preference has value $stringValue")
                if (preference is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0) preference.entries[index] else null)
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
                }
                true
            }
        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see .sBindPreferenceSummaryToValueListener
         */
        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(
            preference: Preference?,
            numeric: Boolean = false,
        ) {
            if (preference == null) {
                return
            }
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .all
                    .getOrDefault(preference.key, "").toString())
            if (preference is EditTextPreference && numeric) {
                preference.setOnBindEditTextListener { editText: EditText ->
                    editText.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                }
            }
        }
    }
}