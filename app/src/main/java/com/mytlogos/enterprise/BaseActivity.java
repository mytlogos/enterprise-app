package com.mytlogos.enterprise;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.mytlogos.enterprise.ui.Home;
import com.mytlogos.enterprise.ui.ListsFragment;
import com.mytlogos.enterprise.ui.NewsFragment;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private View progressView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkToolbar();
        setContentView(this.getContent());
        this.progressView = findViewById(R.id.load_progress);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @return resource Id which uses {@link BaseLayout} as its root
     */
    protected abstract int getContent();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class<? extends Activity> activityClass = null;
        Fragment fragment = null;
        boolean addToBackStack = true;

        switch (item.getItemId()) {
            case R.id.home:
                fragment = new Home();
                addToBackStack = false;
                break;
            case R.id.news:
                fragment = new NewsFragment();
                break;
            case R.id.logout:
                // todo show verification popup
                break;
            case R.id.lists:
                fragment = new ListsFragment();
                break;
            case R.id.add_medium:
                activityClass = AddMediumActivity.class;
                break;
            case R.id.add_list:
                activityClass = AddListActivity.class;
                break;
            case R.id.settings:
                activityClass = SettingsActivity.class;
                break;
        }
        boolean selected = false;
        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            this.startActivity(intent);
            selected = true;
        }
        if (fragment != null) {
            this.switchWindow(fragment, addToBackStack);
            selected = true;
        }
        if (selected) {
            DrawerLayout drawer = findViewById(R.id.BASE_ID);
            drawer.closeDrawers();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void switchWindow(Fragment fragment, boolean addToBackStack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //replace your current container being most of the time as FrameLayout
        transaction.replace(R.id.container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkToolbar();
    }

    private void checkToolbar() {
        if (getSupportActionBar() != null) {
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            DrawerLayout drawer = findViewById(R.id.BASE_ID);
            // fixme remove this from this activity?
            if (drawer != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }
        }
    }
}
