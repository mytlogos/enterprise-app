package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mytlogos.enterprise.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 *
 */
@ExperimentalCoroutinesApi
class Statistics : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.setTitle("Statistics")
        val pager = inflater.inflate(R.layout.statistics, container, false) as ViewPager
        val tabLayout = this.mainActivity.tabLayout

        pager.adapter = SectionsPagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(pager)
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        return pager
    }

    private inner class SectionsPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments: Array<Fragment?>

        override fun getItem(position: Int): Fragment {
            var fragment = fragments[position]
            if (fragment == null) {
                if (position == 0) {
                    fragment = SpaceViewFragment()
                    fragments[0] = fragment
                } else {
                    throw IndexOutOfBoundsException("Only a single Item available but tried to access $position")
                }
            }
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, fragment: Any) {
            super.destroyItem(container, position, fragment)
            fragments[position] = null
        }

        override fun getCount(): Int {
            return 1
        }

        override fun getPageTitle(position: Int): CharSequence {
            return "Space Usage"
        }

        init {
            fragments = arrayOfNulls(count)
        }
    }
}