package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.tools.ScrollHideHelper
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * An abstract ViewerFragment for Media.
 *
 * Flow:
 *
 * (ViewerFragment|SubClass):onCreateView
 *  - init views and listeners
 *
 * e.g. User scrolls
 *  -> onScroll gets called
 *
 * SubClass:onScroll
 * SubClass:seekFromProgress
 * SubClass:calculateProgressByScroll
 * SubClass:getProgressDescription
 * SubClass:updateViewProgress
 */
@ExperimentalCoroutinesApi
abstract class ViewerFragment<T> : BaseFragment() {
    private val scrollHideHelper = ScrollHideHelper()
    private lateinit var navigationView: View
    private lateinit var appbar: View
    private lateinit var swipeLayout: SwipyRefreshLayout
    private lateinit var progressView: TextView
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
        swipeLayout = view.findViewById(R.id.swiper) as SwipyRefreshLayout
        inflater.inflate(layoutRes, swipeLayout, true)

        navigationView = view.findViewById(R.id.navigation)
        appbar = requireActivity().findViewById(R.id.appbar)

        swipeLayout.setOnRefreshListener(this@ViewerFragment::navigateEpisode)
        progressView = view.findViewById(R.id.progress) as TextView

        (view.findViewById(R.id.left_nav) as View).setOnClickListener { navigateEpisode(TOP) }
        (view.findViewById(R.id.right_nav) as View).setOnClickListener { navigateEpisode(BOTTOM) }

        if (scrolledViewId != View.NO_ID) {
            val localScrollView: View = view.findViewById(scrolledViewId)
            localScrollView.setOnScrollChangeListener { _: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                onScroll(scrollX,
                    scrollY,
                    oldScrollX,
                    oldScrollY
                )
            }
            scrollView = localScrollView
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.viewer_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.read) {
            this.updateProgress(1.0f)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onScroll(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        if (scrollY != oldScrollY) {
            scrollHideHelper.hideGroups(
                oldScrollX,
                scrollX,
                oldScrollY,
                scrollY,
                navigationView,
                null,
                appbar,
                null
            )
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
        swipeLayout.isRefreshing = false
    }

    @get:IdRes
    open val scrolledViewId: Int
        get() = View.NO_ID

    open fun seekFromProgress(progress: Float) { /* no-op */ }

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
        var localScrollY = scrollY
        val localScrollView = scrollView
        if (localScrollView == null || localScrollY == 0) {
            return 0.0f
        }
        var maxHeight = 0f
        if (localScrollView is ViewGroup) {
            val viewGroup = localScrollView as ViewGroup?
            for (i in 0 until viewGroup!!.childCount) {
                val child = viewGroup.getChildAt(i)
                maxHeight += child.height.toFloat()
            }
        } else {
            maxHeight = localScrollView.height.toFloat()
        }
        localScrollY += localScrollView.height
        return if (maxHeight == 0f) {
            0.0f
        } else scrollY / maxHeight
    }

    open fun getProgressDescription(progress: Float): String {
        return String.format(Locale.getDefault(), "%.1f%%", progress)
    }

    open fun updateViewProgress(progressDescription: String?) {
        progressView.text = progressDescription
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
                TOP -> index--
                BOTTOM -> index++
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