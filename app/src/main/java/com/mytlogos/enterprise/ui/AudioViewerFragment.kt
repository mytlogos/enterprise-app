package com.mytlogos.enterprise.ui;

import androidx.fragment.app.Fragment;

public class AudioViewerFragment extends BaseFragment {

    public static Fragment newInstance(int episodeId, String path) {
        return new AudioViewerFragment();
    }
}
