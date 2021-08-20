package com.mytlogos.enterprise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.mytlogos.enterprise.ui.Home
import com.mytlogos.enterprise.ui.ListsFragment
import com.mytlogos.enterprise.ui.NewsFragment

abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var progressView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkToolbar()
        setContentView(content)

        progressView = findViewById(R.id.load_progress)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * @return resource Id which uses [BaseLayout] as its root
     */
    protected abstract val content: Int

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var activityClass: Class<out Activity?>? = null
        var fragment: Fragment? = null
        var addToBackStack = true

        when (item.itemId) {
            R.id.home -> {
                fragment = Home()
                addToBackStack = false
            }
            R.id.news -> fragment = NewsFragment()
            R.id.logout -> {
            }
            R.id.lists -> fragment = ListsFragment()
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
            switchWindow(fragment, addToBackStack)
            selected = true
        }
        if (selected) {
            val drawer = findViewById<DrawerLayout>(R.id.BASE_ID)
            drawer.closeDrawers()
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun switchWindow(fragment: Fragment, addToBackStack: Boolean) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        //replace your current container being most of the time as FrameLayout
        transaction.replace(R.id.container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        checkToolbar()
    }

    private fun checkToolbar() {
        if (supportActionBar != null) {
            return
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        if (toolbar != null) {
            setSupportActionBar(toolbar)
            requireSupportActionBar().setDisplayHomeAsUpEnabled(true)

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            val drawer = findViewById<DrawerLayout>(R.id.BASE_ID)
            // fixme remove this from this activity?
            if (drawer != null) {
                val toggle = ActionBarDrawerToggle(
                    this,
                    drawer,
                    toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close)
                drawer.addDrawerListener(toggle)
                toggle.syncState()
            }
        }
    }
}

fun AppCompatActivity.requireSupportActionBar(): ActionBar {
    return supportActionBar!!
}