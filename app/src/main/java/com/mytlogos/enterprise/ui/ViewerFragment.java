package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.tools.ScrollHideHelper;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.List;

abstract class ViewerFragment<T> extends BaseFragment {
    private BottomNavigationView navigationView;
    private View appbar;
    SwipyRefreshLayout swipeLayout;
    List<T> readableEpisodes = new ArrayList<>();
    T currentlyReading;
    private final ScrollHideHelper scrollHideHelper = new ScrollHideHelper();
    static final String MEDIUM = "MEDIUM_FILE";
    static final String START_EPISODE = "START_EPISODE";

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        this.navigationView = view.findViewById(R.id.navigation);
        this.appbar = requireActivity().findViewById(R.id.appbar);
        this.swipeLayout = view.findViewById(R.id.swiper);
        this.swipeLayout.setOnRefreshListener(this::navigateEpisode);
        this.navigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.left_nav) {
                navigateEpisode(SwipyRefreshLayoutDirection.TOP);
            } else if (item.getItemId() == R.id.right_nav) {
                navigateEpisode(SwipyRefreshLayoutDirection.BOTTOM);
            } else {
                System.out.println("unknown MenuItem for Text Navigation: " + item.getItemId());
                showToast("Unknown MenuItem");
            }
            return true;
        });
        return view;
    }

    void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY != oldScrollY) {
            this.scrollHideHelper.hideGroups(oldScrollX, scrollX, oldScrollY, scrollY, this.navigationView, null, this.appbar, null);
        }
    }


    void toggleReadingMode() {
        this.scrollHideHelper.toggleGroups(this.navigationView, null, this.appbar, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.scrollHideHelper.showGroups(this.navigationView, null, this.appbar, null);
    }

    private void navigateEpisode(SwipyRefreshLayoutDirection direction) {
        if (this.currentlyReading == null) {
            if (this.readableEpisodes.isEmpty()) {
                return;
            } else {
                this.currentlyReading = this.readableEpisodes.get(0);
            }
        } else {
            int index = this.readableEpisodes.indexOf(currentlyReading);
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                index--;
            } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                index++;
            } else {
                System.out.println("Unknown swipe direction in TextViewerFragment, neither top or bottom");
                return;
            }
            if (index >= this.readableEpisodes.size()) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the last saved episode");
                return;
            } else if (index < 0) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the first saved episode");
                return;
            }
            this.currentlyReading = this.readableEpisodes.get(index);
        }
        updateContent();
    }

    @LayoutRes
    abstract int getLayoutRes();

    abstract void updateContent();
}
