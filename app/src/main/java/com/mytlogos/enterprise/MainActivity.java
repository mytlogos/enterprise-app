package com.mytlogos.enterprise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.UserPreferences;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.service.BootReceiver;
import com.mytlogos.enterprise.service.DownloadWorker;
import com.mytlogos.enterprise.service.SynchronizeWorker;
import com.mytlogos.enterprise.ui.Home;
import com.mytlogos.enterprise.ui.ListsFragment;
import com.mytlogos.enterprise.ui.MediaInWaitListFragment;
import com.mytlogos.enterprise.ui.MediumFragment;
import com.mytlogos.enterprise.ui.NewsFragment;
import com.mytlogos.enterprise.viewmodel.UserViewModel;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = this::handlePreferences;
    private UserViewModel viewModel;
    private View container;
    private BaseLayout baseLayout;
    private LiveData<User> userData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DownloadWorker.watchDatabase(this.getApplication(), this);
        BootReceiver.startWorker(this.getApplication());

        setContentView(R.layout.activity_main);

        this.container = findViewById(R.id.container);
        this.baseLayout = findViewById(R.id.BASE_ID);
        this.showLoading(true);
        // start periodic worker if it isn't running yet
        checkToolbar();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userData = this.viewModel.getUserLiveData();
        userData.observe(this, this::handleUserChanges);

        this.getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean showHomeAsUp = this.getSupportFragmentManager().getBackStackEntryCount() > 0;
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(showHomeAsUp);
        });
    }

    public TabLayout getTabLayout() {
        return this.baseLayout.activateTabs();
    }

    public void setTitle(String title) {
        Objects.requireNonNull(this.getSupportActionBar()).setTitle(title);
    }

    public void setTitle(@StringRes int title) {
        Objects.requireNonNull(this.getSupportActionBar()).setTitle(title);
    }

    /**
     * Shows the progress UI and hides the main content.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showLoading(final boolean showLoading) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        this.container.setVisibility(showLoading ? View.GONE : View.VISIBLE);
        this.container.animate().setDuration(shortAnimTime).alpha(
                showLoading ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(showLoading ? View.GONE : View.VISIBLE);
            }
        });

        this.baseLayout.showLoading(showLoading);
    }

    private void handleUserChanges(User user) {
        this.checkLogin(user);
        UserPreferences.putLoggedStatus(this, user != null);
        UserPreferences.putLoggedUuid(this, user == null ? null : user.getUuid());
        System.out.println("roomUser changed to: " + user);
        this.showLoading(false);
    }

    private void checkLogin(@Nullable User user) {
        if (user == null) {
            user = this.userData.getValue();
        }
        if (user != null) {
            this.showLoading(false);
            if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
                this.switchWindow(new Home(), false);
            }
        } else if (UserPreferences.getLoggedStatus(this) || this.viewModel.isLoading()) {
            this.showLoading(true);
        } else {
            this.showLoading(false);
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            startActivity(intent);
        }
    }

    public void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> viewModel.logout())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void checkToolbar() {
        if (getSupportActionBar() != null) {
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            /*DrawerLayout drawer = findViewById(R.id.BASE_ID);
            // fixme remove this from this activity?
            if (drawer != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }*/
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resuming");

        checkToolbar();
        checkLogin(null);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        for (String key : sharedPreferences.getAll().keySet()) {
            this.handlePreferences(sharedPreferences, key);
        }

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() > 0) {
            Fragment fragment = fragments.get(fragments.size() - 1);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!(fragment instanceof Home));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferenceManager
                .getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.userData.getValue() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Class<?> activityClass = null;

        boolean selected = false;

        switch (item.getItemId()) {
            case R.id.action_settings:
                activityClass = SettingsActivity.class;
                selected = true;
                break;
            case R.id.logout:
                this.logout();
                selected = true;
                break;
            case R.id.download_now:
                DownloadWorker.enqueueDownloadTask(this.getApplication());
                selected = true;
                break;
            case R.id.stop_download_now:
                DownloadWorker.stopWorker(this.getApplication());
                selected = true;
                break;
            case R.id.synch_now:
                SynchronizeWorker.enqueueOneTime(this.getApplication());
                selected = true;
                break;
            case R.id.clear_media:
                this.clearLocalMediaData();
                selected = true;
                break;
            case R.id.reset_fail_counter:
                RepositoryImpl.getInstance().clearFailEpisodes();
                selected = true;
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            this.startActivity(intent);
            selected = true;
        }
        if (selected) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearLocalMediaData() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to clear local Media Data?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    RepositoryImpl.getInstance().clearLocalMediaData();
                    SynchronizeWorker.enqueueOneTime(this.getApplication());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class<?> activityClass = null;
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
                this.logout();
                break;
            case R.id.lists:
                fragment = new ListsFragment();
                break;
            case R.id.medium:
                fragment = new MediumFragment();
                break;
            case R.id.mediaInWait:
                fragment = new MediaInWaitListFragment();
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
        this.baseLayout.deactivateTabs();
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.BASE_ID);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // check if the navigationView drawer is open
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawers();
            return;
        }

        FragmentManager manager = getSupportFragmentManager();

        if (!manager.popBackStackImmediate()) {
            super.onBackPressed();
        }
        this.baseLayout.deactivateTabs();

        if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.switchWindow(new Home(), false);
        }
    }

    public void switchWindow(@NonNull Fragment fragment, boolean addToBackStack) {
        this.switchWindow(fragment, null, addToBackStack);
    }

    public void switchWindow(@NonNull Fragment fragment) {
        this.switchWindow(fragment, null, true);
    }

    public void switchWindow(@NonNull Fragment fragment, @Nullable Bundle bundle, boolean addToBackStack) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //replace your current container being most of the time as FrameLayout
        transaction.replace(R.id.container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
        transaction.commit();
        this.baseLayout.deactivateTabs();
    }

    private void handlePreferences(SharedPreferences preferences, String key) {
        String downloadKey = "auto-download";
        if (key.equals(downloadKey)) {
            if (preferences.getBoolean(downloadKey, false)) {

            }
        }
    }
}
