package com.mytlogos.enterprise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mytlogos.enterprise.background.UserPreferences;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.ui.AddMedium;
import com.mytlogos.enterprise.ui.Home;
import com.mytlogos.enterprise.ui.NewsFragment;
import com.mytlogos.enterprise.ui.ReadHistoryFragment;
import com.mytlogos.enterprise.ui.UnreadChapterFragment;
import com.mytlogos.enterprise.viewmodel.UserViewModel;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ReadHistoryFragment.ReadHistoryClickListener,
        NewsFragment.NewsClickListener,
        UnreadChapterFragment.UnreadChapterClickListener {

    private UserViewModel viewModel;
    private View container;
    private View progressView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkToolbar();

        setContentView(R.layout.activity_main);

        this.container = findViewById(R.id.main_content);
        this.progressView = findViewById(R.id.load_progress);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.viewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        this.viewModel.getUser().observe(this, this::handleUserChanges);

        System.out.println(this.viewModel.isLoading());
        this.checkLogin(null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showLoading(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        this.container.setVisibility(show ? View.GONE : View.VISIBLE);
        this.container.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        this.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        this.progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void handleUserChanges(User roomUser) {
        UserPreferences.putLoggedStatus(this, roomUser != null);
        System.out.println("roomUser changed to: " + roomUser);
        this.showLoading(false);
        this.checkLogin(roomUser);
    }

    private void checkLogin(@Nullable User roomUser) {
        if (roomUser == null) {
            roomUser = this.viewModel.getUser().getValue();
        }
        if (roomUser != null) {
            this.switchWindow(new Home(), false);
        } else if (UserPreferences.getLoggedStatus(this) || this.viewModel.isLoading()) {
            this.showLoading(true);
        } else {
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            startActivity(intent);
        }
    }

    public void logout() {
        viewModel.logout();
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
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Class<?> activityClass = null;

        boolean selected = false;
        if (id == R.id.action_settings) {
            activityClass = SettingsActivity.class;
            selected = true;
        } else if (id == R.id.logout) {
            this.logout();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class<?> activityClass = null;
        Fragment fragment = null;
        boolean addToBackStack = false;

        switch (item.getItemId()) {
            case R.id.home:
                fragment = new Home();
                break;
            case R.id.news:
                // todo
                break;
            case R.id.logout:
                // todo
                break;
            case R.id.lists:
                // todo
                break;
            case R.id.add_medium:
                fragment = new AddMedium();
                addToBackStack = true;
                break;
            case R.id.add_list:
                // todo
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
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawers();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // fixme on addList Fragment, id.drawer_layout gives an frameLayout
        if (drawer.isDrawerOpen(drawer)) {
            drawer.closeDrawers();
            return;
        }

        FragmentManager manager = getSupportFragmentManager();

        if (!manager.popBackStackImmediate()) {
            super.onBackPressed();
        }
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
    public void onListFragmentInteraction(Object item) {

    }

    @Override
    public void onListFragmentInteraction(News item) {

    }

    private static class LoginTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

}
