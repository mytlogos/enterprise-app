package com.mytlogos.enterprise;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

/**
 * TODO: document your custom view class.
 */
public class BaseLayout extends DrawerLayout {
    // fixme this could lead to problems if multiple custom views have unknowingly the same id
    private ViewGroup container;
    private View containerContent;
    private NavigationView navRight;
    private AppBarLayout appBar;
    private TabLayout tabLayout;

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
        LayoutInflater
                .from(getContext())
                .inflate(R.layout.base_layout, this, true);
        this.appBar = this.findViewById(R.id.appbar);
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

    public void setMenu(int resId) {
        this.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, this.navRight);
        this.navRight.inflateMenu(resId);
    }

    private boolean callSuperAddView(View child) {
        if (child.getId() == R.id.nav_view) {
            return true;
        }
        if (child.getId() == R.id.nav_view_right) {
            this.navRight = (NavigationView) child;
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

    @Override
    public void addView(View child) {
        boolean navRightNull = this.navRight == null;

        if (this.callSuperAddView(child)) {
            super.addView(child);
        }
        if (navRightNull && (this.navRight != null)) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.navRight);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        boolean navRightNull = this.navRight == null;

        if (this.callSuperAddView(child)) {
            super.addView(child, index, params);
        }

        if (navRightNull && (this.navRight != null)) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.navRight);
        }
    }

    @Override
    public void addView(View child, int index) {
        boolean navRightNull = this.navRight == null;

        if (this.callSuperAddView(child)) {
            super.addView(child, index);
        }

        if (navRightNull && (this.navRight != null)) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.navRight);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        boolean navRightNull = this.navRight == null;

        if (this.callSuperAddView(child)) {
            super.addView(child, width, height);
        }

        if (navRightNull && (this.navRight != null)) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.navRight);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        boolean navRightNull = this.navRight == null;

        if (this.callSuperAddView(child)) {
            super.addView(child, params);
        }

        if (navRightNull && (this.navRight != null)) {
            this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.navRight);
        }
    }
}
