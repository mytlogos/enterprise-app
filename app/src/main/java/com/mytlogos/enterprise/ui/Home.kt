package com.mytlogos.enterprise.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.MainActivity
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.SettingsFragment
import com.mytlogos.enterprise.model.HomeStats
import com.mytlogos.enterprise.viewmodel.UserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class Home : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home, container, false)
        this.addClickListener(view, R.id.chapter, EpisodeFragment())
        this.addClickListener(view, R.id.history, ReadHistoryFragment())
        this.addClickListener(view, R.id.list, ListsFragment())
        this.addClickListener(view, R.id.medium, MediumListFragment())
        this.addClickListener(view, R.id.mediaInWait, MediaInWaitListFragment())
        this.addClickListener(view, R.id.statistics, Statistics())
        this.addClickListener(view, R.id.notifications, NotificationFragment())
        this.addClickListener(view, R.id.external_user, ExternalUserListFragment())
        this.addClickListener(view, R.id.settings, SettingsFragment())
        this.addClickListener(view,
            R.id.logout,
            object : ClickListener {
                override fun onClick(activity: MainActivity?) {
                    activity!!.logout()
                }
            })
        val viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val unreadChapter = view.findViewById(R.id.unread_chapter) as TextView
        val unreadNews = view.findViewById(R.id.unread_news) as TextView
        val readToday = view.findViewById(R.id.read_today) as TextView
        val readTotal = view.findViewById(R.id.read_total) as TextView
        val internalLists = view.findViewById(R.id.internal_lists) as TextView
        val externalLists = view.findViewById(R.id.external_lists) as TextView
        val audioMedia = view.findViewById(R.id.audio_medium) as TextView
        val videoMedia = view.findViewById(R.id.video_medium) as TextView
        val textMedia = view.findViewById(R.id.text_medium) as TextView
        val imageMedia = view.findViewById(R.id.image_medium) as TextView
        val unusedMedia = view.findViewById(R.id.unused_media) as TextView
        val externalUser = view.findViewById(R.id.external_user_count) as TextView
        viewModel.homeStatsLiveData.observe(viewLifecycleOwner, { user: HomeStats? ->
            if (user != null && user.name.isNotEmpty()) {
                val name = user.name
                this.setTitle("Home - $name")
            } else {
                this.setTitle("Home - " + "Not logged in")
            }
            unreadChapter.text =
                getString(R.string.unread_chapter_value, user?.unreadChapterCount ?: 0)
            unreadNews.text = getString(R.string.unread_news_value, user?.unreadNewsCount ?: 0)
            readToday.text = getString(R.string.current_read, user?.readTodayCount ?: 0)
            readTotal.text = getString(R.string.total_read, user?.readTotalCount ?: 0)
            internalLists.text = getString(R.string.internal_lists, user?.internalLists ?: 0)
            externalLists.text = getString(R.string.external_lists, user?.externalLists ?: 0)
            externalUser.text = getString(R.string.external_user_count, user?.externalUser ?: 0)
            audioMedia.text = getString(R.string.audio_media, user?.audioMedia ?: 0)
            videoMedia.text = getString(R.string.video_media, user?.videoMedia ?: 0)
            textMedia.text = getString(R.string.text_media, user?.textMedia ?: 0)
            imageMedia.text = getString(R.string.image_media, user?.imageMedia ?: 0)
            unusedMedia.text = getString(R.string.unused_media, user?.unusedMedia ?: 0)
        })
        this.setTitle("Home")
        return view
    }

    private fun addClickListener(view: View, viewId: Int, activityClass: Class<*>) {
        val group: View = view.findViewById(viewId)
        group.setOnClickListener { v: View? ->
            val activity = mainActivity
            val intent = Intent(activity, activityClass)
            startActivity(intent)
        }
    }

    private fun addClickListener(view: View, viewId: Int, listener: ClickListener) {
        val group: View = view.findViewById(viewId)
        group.setOnClickListener {
            val activity = mainActivity
            listener.onClick(activity)
        }
    }

    private fun addClickListener(view: View, viewId: Int, fragment: Fragment) {
        val group: View = view.findViewById(viewId)
        group.setOnClickListener {
            val activity = mainActivity
            activity.switchWindow(fragment, true)
        }
    }

    private interface ClickListener {
        fun onClick(activity: MainActivity?)
    }
}