package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.NoTextTabAdapter;
import com.creator.qweekdots.adapter.PagerAdapter;
import com.creator.qweekdots.adapter.TabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.iammert.library.AnimatedTabLayout;

import static android.content.Context.MODE_PRIVATE;

public class Feeds extends Fragment {
    //
    SharedPreferences prefs = null;

    private TabLayout tabLayout;
    //private AnimatedTabLayout tabLayout0;
    private int[] tabIcons = {
            R.drawable.feed_home,
            R.drawable.feed_popular,
            R.drawable.feed_spaces
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feeds, container, false);

        prefs = requireActivity().getSharedPreferences("com.creator.qweekdots", MODE_PRIVATE);

        // Set ViewPager pages
        ViewPager viewPager = rootView.findViewById(R.id.feeds_viewpager);
        setupViewPager(viewPager);
        tabLayout = rootView.findViewById(R.id.feeds_tabs);
        tabLayout.setupWithViewPager(viewPager);
        //setCustomTabs();

        /*
        tabLayout0 = rootView.findViewById(R.id.feeds_tabs0);

        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        adapter.addFragment(new Spacesfeed());
        adapter.addFragment(new Newsfeed());
        adapter.addFragment(new Popularfeed());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout0.setupViewPager(viewPager);
        tabLayout0.setTabChangeListener(i -> {

        });

        if (prefs.getBoolean("firstrun", true)) {
            viewPager.setCurrentItem(2);
            prefs.edit().putBoolean("firstrun", false).apply();
        } else {
            viewPager.setCurrentItem(0);
        }

         */

        return rootView;
    }

    private void setCustomTabs() {
        for (int i = 0; i < tabIcons.length; i++) {
            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.hometab,null);
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            view.findViewById(R.id.icon).setBackgroundResource(tabIcons[i]);
            if(tab !=null) tab.setCustomView(view);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(getChildFragmentManager());
        adapter.addFragment(new Spacesfeed(), "Home");
        adapter.addFragment(new Newsfeed(), "Following");
        adapter.addFragment(new Popularfeed(), "For You");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        if (prefs.getBoolean("firstrun", true)) {
            viewPager.setCurrentItem(2);
            prefs.edit().putBoolean("firstrun", false).apply();
        } else {
            viewPager.setCurrentItem(0);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    //dropBtn.setVisibility(View.VISIBLE);
                } else {
                    //dropBtn.setVisibility(View.GONE);
                }

                if(position == 2) {
                    //resetNotificationStatus();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
