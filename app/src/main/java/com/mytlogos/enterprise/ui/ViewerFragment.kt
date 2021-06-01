package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.tools.ScrollHideHelper;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

abstract class ViewerFragment<T> extends BaseFragment {
    private final ScrollHideHelper scrollHideHelper = new ScrollHideHelper();
    private View navigationView;
    private View appbar;
    private SwipyRefreshLayout swipeLayout;
    private TextView progressView;
    private View scrollView;
    private float progress = 0;
    private int maxScrolledY = 0;
    int currentEpisode;
    String currentBook;
    List<T> readableEpisodes = new ArrayList<>();
    T currentlyReading;
    static final String MEDIUM = "MEDIUM_FILE";
    static final String START_EPISODE = "START_EPISODE";

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewer_layout, container, false);
        this.swipeLayout = view.findViewById(R.id.swiper);
        inflater.inflate(getLayoutRes(), this.swipeLayout, true);

        this.navigationView = view.findViewById(R.id.navigation);
        this.appbar = requireActivity().findViewById(R.id.appbar);
        this.swipeLayout.setOnRefreshListener(this::navigateEpisode);
        this.progressView = view.findViewById(R.id.progress);
        view.findViewById(R.id.left_nav).setOnClickListener(v -> navigateEpisode(SwipyRefreshLayoutDirection.TOP));
        view.findViewById(R.id.right_nav).setOnClickListener(v -> navigateEpisode(SwipyRefreshLayoutDirection.BOTTOM));

        int scrolledViewId = this.getScrolledViewId();
        if (scrolledViewId != View.NO_ID) {
            this.scrollView = view.findViewById(scrolledViewId);
            this.scrollView.setOnScrollChangeListener(
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                            this.onScroll(scrollX, scrollY, oldScrollX, oldScrollY)
            );
        }
        return view;
    }

    void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY != oldScrollY) {
            this.scrollHideHelper.hideGroups(oldScrollX, scrollX, oldScrollY, scrollY, this.navigationView, null, this.appbar, null);
        }
        if (scrollY > this.maxScrolledY) {
            this.maxScrolledY = scrollY;
            float progress = this.calculateProgressByScroll(scrollY, scrollX);
            this.updateProgress(progress);
        }
    }

    void toggleReadingMode() {
        this.scrollHideHelper.toggleGroups(this.navigationView, null, this.appbar, null);
    }

    void onLoadFinished() {
        this.maxScrolledY = 0;
        this.updateProgress(this.getCurrentProgress());
        this.seekFromProgress(this.progress);
        this.swipeLayout.setRefreshing(false);
    }

    @IdRes
    int getScrolledViewId() {
        return View.NO_ID;
    }

    void seekFromProgress(float progress) {

    }

    /**
     * Progress with value of 0 to 1.
     *
     * @param progress newProgress
     */
    void updateProgress(float progress) {
        if (progress > 1) {
            progress = 1;
        }
        progress = BigDecimal
                .valueOf(progress)
                .setScale(3, RoundingMode.CEILING)
                .floatValue();

        if (progress > this.progress) {
            this.progress = progress;
        } else {
            return;
        }
        progress = progress * 100;
        this.updateViewProgress(this.getProgressDescription(progress));
    }

    float calculateProgressByScroll(int scrollY, int scrollX) {
        if (this.scrollView == null || scrollY == 0) {
            return 0;
        }
        float maxHeight = 0;

        if (this.scrollView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) this.scrollView;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                maxHeight += child.getHeight();
            }
        } else {
            maxHeight = this.scrollView.getHeight();
        }
        scrollY = this.scrollView.getHeight() + scrollY;
        if (maxHeight == 0) {
            return 0;
        }
        return scrollY / maxHeight;
    }

    String getProgressDescription(float progress) {
        return String.format(Locale.getDefault(), "%.1f%%", progress);
    }

    void updateViewProgress(String progressDescription) {
        this.progressView.setText(progressDescription);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.currentEpisode = getArguments().getInt(START_EPISODE);
            this.currentBook = getArguments().getString(MEDIUM);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.scrollHideHelper.showGroups(this.navigationView, null, this.appbar, null);

        Bundle bundle = new Bundle();
        bundle.putInt(START_EPISODE, currentEpisode);
        bundle.putString(MEDIUM, currentBook);
        this.setArguments(bundle);
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
        this.saveProgress(this.progress);
        this.updateContent();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.saveProgress(this.progress);
    }

    abstract float getCurrentProgress();

    abstract void saveProgress(float progress);

    @LayoutRes
    abstract int getLayoutRes();

    abstract void updateContent();
}
