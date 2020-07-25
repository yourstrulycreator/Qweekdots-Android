package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.creator.qweekdots.R;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static android.content.Context.SEARCH_SERVICE;

public class SearchBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = SearchBottomSheet.class.getSimpleName();

    private ViewPager viewPager;
    private androidx.appcompat.widget.SearchView searchView;
    private SearchUserFragment userSearchFragment;
    private DropSearchFragment dropSearchFragment;

    private TextView titleTxtView;
    private BottomSheetBehavior bottomSheetBehavior;

    private View view;

    private Context context;
    private String logged;

    SearchBottomSheet(Context context, String logged) {
        this.context = context;
        this.logged = logged;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_bottom_sheet, container, false);

        if(context!=null) {
            viewPager = view.findViewById(R.id.search_vPager);
            setupViewPager(viewPager);

            TabLayout tabLayout = view.findViewById(R.id.searchtabs);
            tabLayout.setupWithViewPager(viewPager);

            // Attach the page change listener inside the activity
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                // This method will be invoked when a new page becomes selected.
                @Override
                public void onPageSelected(int position) {
                    if (searchView != null && !searchView.isIconified()) {
                        searchView.onActionViewExpanded();
                        searchView.setIconified(false);

                    }
                }

                // This method will be invoked when the current page is scrolled
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    // Code goes here

                }

                // Called when the scroll state changes:
                // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                @Override
                public void onPageScrollStateChanged(int state) {
                    // Code goes here
                }
            });

            titleTxtView = view.findViewById(R.id.optTitleSheetTxt);
            titleTxtView.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });

            searchView = view.findViewById(R.id.searchbar);
            searchView.isFocused();
            searchView.onActionViewExpanded();
            SearchManager searchManager = (SearchManager) requireActivity().getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(requireActivity().getComponentName()));
            Objects.requireNonNull(searchView).setIconifiedByDefault(false);

            EditText searchEditText;
            searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(getResources().getColor(R.color.contentTextColor));
            searchEditText.setHintTextColor(getResources().getColor(R.color.SlateGray));
            searchEditText.setHint("Search Qweekdots");

            androidx.appcompat.widget.SearchView.OnQueryTextListener queryTextListener = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    // this is your adapter that will be filtered
                    ViewPagerAdapter pagerAdapter = (ViewPagerAdapter) viewPager
                            .getAdapter();
                    for (int i = 0; i < Objects.requireNonNull(pagerAdapter).getCount(); i++) {

                        Fragment viewPagerFragment = (Fragment) viewPager
                                .getAdapter().instantiateItem(viewPager, i);
                        if (viewPagerFragment.isAdded()) {

                            if (viewPagerFragment instanceof SearchUserFragment) {
                                userSearchFragment = (SearchUserFragment) viewPagerFragment;
                                userSearchFragment.beginSearch(query);
                                Timber.tag(TAG).i("Query String for Users: %s", query);

                            } else if (viewPagerFragment instanceof DropSearchFragment) {
                                dropSearchFragment = (DropSearchFragment) viewPagerFragment;
                                dropSearchFragment.beginSearch(query);
                                Timber.tag(TAG).i("Query String for Drops: %s", query);
                            }
                        }
                    }

                    return true;

                }

                public boolean onQueryTextSubmit(String query) {
                    // Here u can get the value "query" which is entered in the
                    ViewPagerAdapter pagerAdapter = (ViewPagerAdapter) viewPager
                            .getAdapter();
                    for (int i = 0; i < Objects.requireNonNull(pagerAdapter).getCount(); i++) {

                        Fragment viewPagerFragment = (Fragment) viewPager
                                .getAdapter().instantiateItem(viewPager, i);
                        if (viewPagerFragment.isAdded()) {

                            if (viewPagerFragment instanceof SearchUserFragment) {
                                userSearchFragment = (SearchUserFragment) viewPagerFragment;
                                userSearchFragment.beginSearch(query);
                                Timber.tag(TAG).i("Query String for Users: %s", query);

                            } else if (viewPagerFragment instanceof DropSearchFragment) {
                                dropSearchFragment = (DropSearchFragment) viewPagerFragment;
                                dropSearchFragment.beginSearch(query);
                                Timber.tag(TAG).i("Query String for Drops: %s", query);
                            }
                        }
                    }

                    return true;

                }
            };

            searchView.setOnQueryTextListener(queryTextListener);
        }

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        view = View.inflate(context, R.layout.search_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);

        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(0);


        //setting min height of bottom sheet
        extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);


        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    bottomSheetBehavior.setDraggable(false);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {

                }

                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        return bottomSheet;
    }

    private void setupViewPager(ViewPager viewPager) {
        SearchBottomSheet.ViewPagerAdapter adapter = new SearchBottomSheet.ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(new SearchUserFragment(context, logged), "Users");
        adapter.addFrag(new DropSearchFragment(context, logged), "Drops");
        viewPager.setAdapter(adapter);

        Timber.tag(TAG).i("Setup ViewPager");
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(@NotNull Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            notifyDataSetChanged();
            return POSITION_NONE;
        }
    }
}
