package com.mytlogos.enterprise.ui

import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class VideoViewerFragment : BaseFragment() {
    companion object {
        fun newInstance(episodeId: Int, path: String): Fragment {
            return VideoViewerFragment()
        }
    }
}