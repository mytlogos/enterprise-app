package com.mytlogos.enterprise.ui

import androidx.fragment.app.Fragment

class VideoViewerFragment : BaseFragment() {
    companion object {
        fun newInstance(episodeId: Int, path: String?): Fragment {
            return VideoViewerFragment()
        }
    }
}