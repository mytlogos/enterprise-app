package com.mytlogos.enterprise.ui;

import androidx.fragment.app.Fragment;

public class VideoViewerFragment extends BaseFragment {
    public static Fragment newInstance(int episodeId, String path) {
        return new VideoViewerFragment();
    }
}
