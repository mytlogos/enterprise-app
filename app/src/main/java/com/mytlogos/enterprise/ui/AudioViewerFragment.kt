package com.mytlogos.enterprise.ui

import androidx.fragment.app.Fragment

class AudioViewerFragment : BaseFragment() {
    companion object {
        fun newInstance(episodeId: Int, path: String?): Fragment {
            return AudioViewerFragment()
        }
    }
}