package com.creator.qweekdots.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;

import java.util.HashMap;
import java.util.Objects;

public class SearchFragment extends Fragment {
    View rootView;
    private final String TAG = SearchFragment.class.getSimpleName();

    private String username;
    private String fullname;
    private String profile_pic;

    RecyclerView recyclerView;
    EditText editTextSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.search, container, false);

        if(new DarkModePrefManager(getActivity()).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(requireActivity().getApplicationContext());
        // session manager
        SessionManager session = new SessionManager(getActivity().getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");
        fullname = user.get("fullname");
        profile_pic = user.get("profile_pic");

        return rootView;
    }
}