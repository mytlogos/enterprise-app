package com.mytlogos.enterprise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

/**
 * TODO: document your custom view class.
 */
public class BaseLayout extends DrawerLayout {
    // fixme this could lead to problems if multiple custom views have unknowingly the same id
    private ViewGroup container;
    private View containerContent;
    private AppBarLayout appBar;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private ViewGroup progressContainer;

    public BaseLayout(Context context) {
        super(context);
        init();
    }

    public BaseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setId(R.id.BASE_ID);
        LayoutInflater.from(getContext()).inflate(R.layout.base_layout, this, true);
        this.appBar = this.findViewById(R.id.appbar);
        this.progressBar = this.findViewById(R.id.load_progress);
        this.progressContainer = this.findViewById(R.id.progress_container);
    }

    public TabLayout activateTabs() {
        if (this.tabLayout != null) {
            this.tabLayout.removeAllTabs();
            this.tabLayout.setTabMode(TabLayout.MODE_FIXED);
        } else {
            this.tabLayout = new TabLayout(this.getContext());
        }
        if (this.tabLayout.getParent() == null) {
            this.appBar.addView(this.tabLayout);
        }
        return this.tabLayout;
    }

    public void deactivateTabs() {
        if (this.tabLayout != null && this.tabLayout.getParent() != null) {
            this.appBar.removeView(this.tabLayout);
        }
    }

    private boolean callSuperAddView(View child) {
        if (child.getId() == R.id.nav_view) {
            return true;
        }
        if (child.getId() == R.id.base_content) {
            this.container = (ViewGroup) child;
            if (this.containerContent != null) {
                this.container.addView(this.containerContent);
            }
            return true;
        }
        if (this.container != null) {
            this.containerContent = child;
            this.container.addView(child);
        }
        return false;
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

        this.progressContainer.setVisibility(showLoading ? View.VISIBLE : View.GONE);
        this.progressBar.animate().setDuration(shortAnimTime).alpha(
                showLoading ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                System.out.println("animation start");
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                System.out.println("animation ended");
                progressBar.setVisibility(showLoading ? View.VISIBLE : View.GONE);
            }
        });
    }


   /* @Override
    public void addView(View child) {
        if (this.callSuperAddView(child)) {
            super.addView(child);
        }
    }*/

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (this.callSuperAddView(child)) {
            super.addView(child, index, params);
        }
    }

    @Override
    public void addView(View child, int index) {
        if (this.callSuperAddView(child)) {
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if (this.callSuperAddView(child)) {
            super.addView(child, width, height);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (this.callSuperAddView(child)) {
            super.addView(child, params);
        }
    }
}
