package com.mytlogos.enterprise

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout

/**
 * TODO: document your custom view class.
 */
class BaseLayout : DrawerLayout {
    // fixme this could lead to problems if multiple custom views have unknowingly the same id
    private var container: ViewGroup? = null
    private var containerContent: View? = null
    private var appBar: AppBarLayout? = null
    private var tabLayout: TabLayout? = null
    private var progressBar: ProgressBar? = null
    private var progressContainer: ViewGroup? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle) {
        init()
    }

    private fun init() {
        this.id = R.id.BASE_ID
        LayoutInflater.from(context).inflate(R.layout.base_layout, this, true)
        appBar = findViewById(R.id.appbar) as AppBarLayout?
        progressBar = findViewById(R.id.load_progress) as ProgressBar?
        progressContainer = findViewById(R.id.progress_container) as ViewGroup?
    }

    fun activateTabs(): TabLayout {
        if (tabLayout != null) {
            tabLayout!!.removeAllTabs()
            tabLayout!!.tabMode = TabLayout.MODE_FIXED
        } else {
            tabLayout = TabLayout(this.context)
        }
        if (tabLayout!!.parent == null) {
            appBar!!.addView(tabLayout)
        }
        return tabLayout!!
    }

    fun deactivateTabs() {
        if (tabLayout != null && tabLayout!!.parent != null) {
            appBar!!.removeView(tabLayout)
        }
    }

    private fun callSuperAddView(child: View): Boolean {
        if (child.id == R.id.nav_view) {
            return true
        }
        if (child.id == R.id.base_content) {
            container = child as ViewGroup
            if (containerContent != null) {
                container!!.addView(containerContent)
            }
            return true
        }
        if (container != null) {
            containerContent = child
            container!!.addView(child)
        }
        return false
    }

    /**
     * Shows the progress UI and hides the main content.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showLoading(showLoading: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        progressContainer!!.visibility = if (showLoading) VISIBLE else GONE
        progressBar!!.animate().setDuration(shortAnimTime.toLong()).alpha(
            if (showLoading) 1.0f else 0.0f).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                println("animation start")
                super.onAnimationStart(animation)
            }

            override fun onAnimationEnd(animation: Animator) {
                println("animation ended")
                progressBar!!.visibility = if (showLoading) VISIBLE else GONE
            }
        })
    }

    /* @Override
    public void addView(View child) {
        if (this.callSuperAddView(child)) {
            super.addView(child);
        }
    }*/
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (callSuperAddView(child)) {
            super.addView(child, index, params)
        }
    }

    override fun addView(child: View, index: Int) {
        if (callSuperAddView(child)) {
            super.addView(child, index)
        }
    }

    override fun addView(child: View, width: Int, height: Int) {
        if (callSuperAddView(child)) {
            super.addView(child, width, height)
        }
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        if (callSuperAddView(child)) {
            super.addView(child, params)
        }
    }
}