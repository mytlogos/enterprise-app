package com.mytlogos.enterprise

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.User
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.init
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.loggedStatus
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.putLoggedStatus
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.putLoggedUuid
import com.mytlogos.enterprise.ui.*
import com.mytlogos.enterprise.viewmodel.UserViewModel
import com.mytlogos.enterprise.worker.BootReceiver.Companion.startWorker
import com.mytlogos.enterprise.worker.CheckSavedWorker.Companion.checkLocal
import com.mytlogos.enterprise.worker.DownloadWorker.Companion.enqueueDownloadTask
import com.mytlogos.enterprise.worker.DownloadWorker.Companion.stopWorker
import com.mytlogos.enterprise.worker.DownloadWorker.Companion.watchDatabase
import com.mytlogos.enterprise.worker.SynchronizeWorker.Companion.enqueueOneTime
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val preferenceChangeListener =
        OnSharedPreferenceChangeListener { preferences: SharedPreferences, key: String ->
            handlePreferences(preferences,
                key)
        }
    private var viewModel: UserViewModel? = null
    private var container: View? = null
    private var baseLayout: BaseLayout? = null
    private var userData: LiveData<User?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        watchDatabase(this.application, this)
        startWorker(this.application)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)
        baseLayout = findViewById(R.id.BASE_ID)
        showLoading(true)
        // start periodic worker if it isn't running yet
        checkToolbar()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userData = viewModel!!.userLiveData
        userData!!.observe(this, { user: User? -> handleUserChanges(user) })
        this.supportFragmentManager.addOnBackStackChangedListener {
            val showHomeAsUp = this.supportFragmentManager.backStackEntryCount > 0
            Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(showHomeAsUp)
        }
    }

    val tabLayout: TabLayout
        get() = baseLayout!!.activateTabs()

    fun setTitle(title: String?) {
        Objects.requireNonNull(this.supportActionBar)!!.title = title
    }

    override fun setTitle(@StringRes title: Int) {
        Objects.requireNonNull(this.supportActionBar)!!.setTitle(title)
    }

    /**
     * Shows the progress UI and hides the main content.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showLoading(showLoading: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        container!!.visibility = if (showLoading) View.GONE else View.VISIBLE
        container!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (showLoading) 0.0f else 1.0f).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                container!!.visibility = if (showLoading) View.GONE else View.VISIBLE
            }
        })
        baseLayout!!.showLoading(showLoading)
    }

    private fun handleUserChanges(user: User?) {
        checkLogin(user)
        putLoggedStatus(user != null)
        putLoggedUuid(user?.uuid)
        println("roomUser changed to: $user")
        showLoading(false)
    }

    private fun checkLogin(user: User?) {
        var user = user
        if (user == null) {
            user = userData!!.value
        }
        if (user != null) {
            showLoading(false)
            if (this.supportFragmentManager.backStackEntryCount == 0) {
                this.switchWindow(Home(), false)
            }
        } else if (loggedStatus || viewModel!!.isLoading) {
            showLoading(true)
        } else {
            showLoading(false)
            val intent = Intent(this, LoginRegisterActivity::class.java)
            startActivity(intent)
        }
    }

    fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, which: Int -> viewModel!!.logout() }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun checkToolbar() {
        if (supportActionBar != null) {
            return
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.let { setSupportActionBar(it) }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        init(this)
        println("resuming")
        checkToolbar()
        checkLogin(null)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        for (key in sharedPreferences.all.keys) {
            handlePreferences(sharedPreferences, key)
        }
        val fragments = supportFragmentManager.fragments
        if (fragments.size > 0) {
            val fragment = fragments[fragments.size - 1]
            Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(fragment !is Home)
        }
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (userData!!.value == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return true
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val activityClass: Class<*>? = null
        var fragment: Fragment? = null
        var selected = false
        when (item.itemId) {
            R.id.action_settings -> {
                fragment = SettingsFragment()
                selected = true
            }
            R.id.logout -> {
                logout()
                selected = true
            }
            R.id.download_now -> {
                enqueueDownloadTask(this.application)
                selected = true
            }
            R.id.stop_download_now -> {
                stopWorker(this.application)
                selected = true
            }
            R.id.synch_now -> {
                enqueueOneTime(this.application)
                selected = true
            }
            R.id.check_saved_now -> {
                checkLocal(this.application)
                selected = true
            }
            R.id.clear_media -> {
                clearLocalMediaData()
                selected = true
            }
            R.id.reset_fail_counter -> {
                instance.clearFailEpisodes()
                selected = true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        if (fragment != null) {
            this.switchWindow(fragment, true)
        }
        if (activityClass != null) {
            val intent = Intent(this, activityClass)
            this.startActivity(intent)
            selected = true
        }
        return if (selected) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun clearLocalMediaData() {
        AlertDialog.Builder(this)
            .setTitle("Are you sure you want to clear local Media Data?")
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                instance.clearLocalMediaData(this)
                enqueueOneTime(this.application)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var activityClass: Class<*>? = null
        var fragment: Fragment? = null
        var addToBackStack = true
        when (item.itemId) {
            R.id.home -> {
                fragment = Home()
                addToBackStack = false
            }
            R.id.news -> fragment = NewsFragment()
            R.id.logout -> logout()
            R.id.lists -> fragment = ListsFragment()
            R.id.medium -> fragment = MediumListFragment()
            R.id.mediaInWait -> fragment = MediaInWaitListFragment()
            R.id.add_medium -> activityClass = AddMediumActivity::class.java
            R.id.add_list -> activityClass = AddListActivity::class.java
            R.id.settings -> fragment = SettingsFragment()
        }
        var selected = false
        if (activityClass != null) {
            val intent = Intent(this, activityClass)
            this.startActivity(intent)
            selected = true
        }
        if (fragment != null) {
            this.switchWindow(fragment, addToBackStack)
            selected = true
        }
        if (selected) {
            val drawer = findViewById<DrawerLayout>(R.id.BASE_ID)
            drawer.closeDrawers()
            return true
        }
        baseLayout!!.deactivateTabs()
        return false
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.BASE_ID)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        // check if the navigationView drawer is open
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawers()
            return
        }
        val manager = supportFragmentManager
        if (!manager.popBackStackImmediate()) {
            super.onBackPressed()
        }
        baseLayout!!.deactivateTabs()
        if (this.supportFragmentManager.backStackEntryCount == 0) {
            this.switchWindow(Home(), false)
        }
    }

    fun switchWindow(fragment: Fragment, addToBackStack: Boolean) {
        this.switchWindow(fragment, null, addToBackStack)
    }

    @JvmOverloads
    fun switchWindow(fragment: Fragment, bundle: Bundle? = null, addToBackStack: Boolean = true) {
        if (bundle != null) {
            fragment.arguments = bundle
        }
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        //replace your current container being most of the time as FrameLayout
        transaction.replace(R.id.container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
            Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        }
        transaction.commit()
        baseLayout!!.deactivateTabs()
    }

    private fun handlePreferences(preferences: SharedPreferences, key: String) {
        val downloadKey = "auto-download"
        if (key == downloadKey) {
            if (preferences.getBoolean(downloadKey, false)) {
            }
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment)
        fragment.setTargetFragment(caller, 0)
        switchWindow(fragment, args, true)
        return true
    }
}