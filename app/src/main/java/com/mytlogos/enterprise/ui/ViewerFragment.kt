package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.tools.ScrollHideHelper
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

abstract class ViewerFragment<T> : BaseFragment() {
    private val scrollHideHelper = ScrollHideHelper()
    private var navigationView: View? = null
    private var appbar: View? = null
    private var swipeLayout: SwipyRefreshLayout? = null
    private var progressView: TextView? = null
    private var scrollView: View? = null
    private var progress = 0f
    private var maxScrolledY = 0
    var currentEpisode = 0
    var currentBook: String? = null
    var readableEpisodes: MutableList<T> = ArrayList()
    var currentlyReading: T? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.viewer_layout, container, false)
        swipeLayout = view.findViewById(R.id.swiper) as SwipyRefreshLayout?
        inflater.inflate(layoutRes, swipeLayout, true)
        navigationView = view.findViewById(R.id.navigation)
        appbar = requireActivity().findViewById(R.id.appbar)
        val localSwipeLayout = swipeLayout
        localSwipeLayout!!.setOnRefreshListener { direction: SwipyRefreshLayoutDirection ->
            navigateEpisode(direction)
        }
        progressView = view.findViewById(R.id.progress) as TextView?
        (view.findViewById(R.id.left_nav) as View)
            .setOnClickListener { navigateEpisode(SwipyRefreshLayoutDirection.TOP) }
        (view.findViewById(R.id.right_nav) as View)
            .setOnClickListener { navigateEpisode(SwipyRefreshLayoutDirection.BOTTOM) }
        val scrolledViewId = scrolledViewId
        if (scrolledViewId != View.NO_ID) {
            scrollView = view.findViewById(scrolledViewId)
            val localScrollView = scrollView
            localScrollView!!.setOnScrollChangeListener { _: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                onScroll(scrollX,
                    scrollY,
                    oldScrollX,
                    oldScrollY)
            }
        }
        return view
    }

    open fun onScroll(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (scrollY != oldScrollY) {
            scrollHideHelper.hideGroups(oldScrollX,
                scrollX,
                oldScrollY,
                scrollY,
                navigationView,
                null,
                appbar,
                null)
        }
        if (scrollY > maxScrolledY) {
            maxScrolledY = scrollY
            val progress = calculateProgressByScroll(scrollY, scrollX)
            updateProgress(progress)
        }
    }

    fun toggleReadingMode() {
        scrollHideHelper.toggleGroups(navigationView, null, appbar, null)
    }

    fun onLoadFinished() {
        maxScrolledY = 0
        updateProgress(currentProgress)
        seekFromProgress(progress)
        swipeLayout!!.isRefreshing = false
    }

    @get:IdRes
    open val scrolledViewId: Int
        get() = View.NO_ID

    fun seekFromProgress(progress: Float) {}

    /**
     * Progress with value of 0 to 1.
     *
     * @param progress newProgress
     */
    fun updateProgress(progress: Float) {
        var newProgress = progress
        if (newProgress > 1) {
            newProgress = 1f
        }
        newProgress = BigDecimal
            .valueOf(newProgress.toDouble())
            .setScale(3, RoundingMode.CEILING)
            .toFloat()
        if (newProgress > this.progress) {
            this.progress = newProgress
        } else {
            return
        }
        newProgress *= 100
        updateViewProgress(getProgressDescription(newProgress))
    }

    open fun calculateProgressByScroll(scrollY: Int, scrollX: Int): Float {
        var scrollY = scrollY
        if (scrollView == null || scrollY == 0) {
            return 0.0f
        }
        var maxHeight = 0f
        if (scrollView is ViewGroup) {
            val viewGroup = scrollView as ViewGroup?
            for (i in 0 until viewGroup!!.childCount) {
                val child = viewGroup.getChildAt(i)
                maxHeight += child.height.toFloat()
            }
        } else {
            maxHeight = scrollView!!.height.toFloat()
        }
        scrollY += scrollView!!.height
        return if (maxHeight == 0f) {
            0.0f
        } else scrollY / maxHeight
    }

    fun getProgressDescription(progress: Float): String {
        return String.format(Locale.getDefault(), "%.1f%%", progress)
    }

    fun updateViewProgress(progressDescription: String?) {
        progressView!!.text = progressDescription
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            currentEpisode = requireArguments().getInt(START_EPISODE)
            currentBook = requireArguments().getString(MEDIUM)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollHideHelper.showGroups(navigationView, null, appbar, null)
        val bundle = Bundle()
        bundle.putInt(START_EPISODE, currentEpisode)
        bundle.putString(MEDIUM, currentBook)
        this.arguments = bundle
    }

    private fun navigateEpisode(direction: SwipyRefreshLayoutDirection) {
        if (currentlyReading == null) {
            if (readableEpisodes.isEmpty()) {
                return
            } else {
                currentlyReading = readableEpisodes[0]
            }
        } else {
            var index = readableEpisodes.indexOf(currentlyReading)
            when (direction) {
                SwipyRefreshLayoutDirection.TOP -> index--
                SwipyRefreshLayoutDirection.BOTTOM -> index++
                else -> {
                    println("Unknown swipe direction in TextViewerFragment, neither top or bottom")
                    return
                }
            }
            if (index >= readableEpisodes.size) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the last saved episode")
                return
            } else if (index < 0) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the first saved episode")
                return
            }
            currentlyReading = readableEpisodes[index]
        }
        saveProgress(progress)
        updateContent()
    }

    override fun onStop() {
        super.onStop()
        saveProgress(progress)
    }

    abstract val currentProgress: Float
    abstract fun saveProgress(progress: Float)

    @get:LayoutRes
    abstract val layoutRes: Int
    abstract fun updateContent()

    companion object {
        const val MEDIUM = "MEDIUM_FILE"
        const val START_EPISODE = "START_EPISODE"
    }
}