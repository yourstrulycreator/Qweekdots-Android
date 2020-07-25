package com.creator.qweekdots.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabsAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public int getCenterPosition(int position) {
        return mFragmentList.size() / 2 + position;
    }

    public Fragment getValueAt(int position) {
        if (mFragmentList.size() == 0) {
            return null;
        }
        return mFragmentList.get(position % mFragmentList.size());
    }
}
