package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mytlogos.enterprise.R;

/**
 *
 */
public class Statistics extends BaseFragment {

    public Statistics() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.setTitle("Statistics");
        ViewPager pager = (ViewPager) inflater.inflate(R.layout.statistics, container, false);

        TabLayout tabLayout = this.getMainActivity().getTabLayout();
        pager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        return pager;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment[] fragments;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[getCount()];
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments[position];

            if (fragment == null) {
                if (position == 0) {
                    fragments[0] = fragment = new SpaceViewFragment();
                }
            }
            return fragment;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            this.fragments[position] = null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Space Usage";
        }
    }
}
