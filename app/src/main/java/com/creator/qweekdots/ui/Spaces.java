package com.creator.qweekdots.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.HotSpacesAdapter;
import com.creator.qweekdots.adapter.MySpacesAdapter;
import com.creator.qweekdots.adapter.SpacesAdapter;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.view.StaggeredGridView;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class Spaces extends Fragment {
    private final String TAG = Spaces.class.getSimpleName();
    private MySpacesAdapter myRingsAdapter;
    private HotSpacesAdapter hotRingsAdapter;
    private SpinKitView ringsProgress, myRingsProgress;
    private LinearLayout myRingsErrorLayout, ringsErrorLayout;
    private LinearLayout myRingsEmptyLayout, ringsEmptyLayout;
    private ArrayList<ChatRoom> chatRoomArrayList, hotRoomsArrayList;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rings, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(requireActivity().getApplicationContext());
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        // User Identifiers
        username = user.get("username");

        // Init Views
        myRingsProgress = rootView.findViewById(R.id.myRingsProgress);
        RecyclerView myRingsV = rootView.findViewById(R.id.myRingsRecyclerView);

        RecyclerView hotRingsV = rootView.findViewById(R.id.hot_rings_rv);
        ringsProgress = rootView.findViewById(R.id.ringsProgress);
        myRingsErrorLayout = rootView.findViewById(R.id.my_rings_error_layout);
        ringsErrorLayout = rootView.findViewById(R.id.rings_error_layout);
        myRingsEmptyLayout = rootView.findViewById(R.id.my_rings_empty_layout);
        ringsEmptyLayout = rootView.findViewById(R.id.rings_empty_layout);
        Button myRingsBtnRetry = rootView.findViewById(R.id.my_rings_error_btn_retry);
        Button ringsBtnRetry = rootView.findViewById(R.id.rings_error_btn_retry);

        ImageView searchRingsBtn = rootView.findViewById(R.id.searchRings);
        searchRingsBtn.setOnClickListener(v->{
            SearchRingsBottomSheet bottomSheet = new SearchRingsBottomSheet(getActivity(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        EditText searchRingsBar = rootView.findViewById(R.id.searchRingsBar);
        searchRingsBar.setOnClickListener(v->{
            SearchRingsBottomSheet bottomSheet = new SearchRingsBottomSheet(getActivity(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        // My Spaces
        chatRoomArrayList = new ArrayList<>();
        LinearLayoutManager myRingsLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myRingsV.setLayoutManager(myRingsLinearLayoutManager);
        myRingsAdapter = new MySpacesAdapter(getActivity(), this, username, chatRoomArrayList);
        myRingsV.setAdapter(myRingsAdapter);
        myRingsV.setItemAnimator(new DefaultItemAnimator());

        // Spaces GridView
        hotRoomsArrayList = new ArrayList<>();
        //LinearLayoutManager hotRingsLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        hotRingsV.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        hotRingsAdapter = new HotSpacesAdapter(getActivity(), this, username, hotRoomsArrayList);
        hotRingsV.setAdapter(hotRingsAdapter);
        hotRingsV.setItemAnimator(new DefaultItemAnimator());

        if(isAdded()) {
            if(isNetworkConnected()) {
                //loadHotRings();
                //loadMyRings();
                Timber.tag(TAG).d("Loading First Spaces Pages and My Spaces");
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                ringsProgress.setVisibility(View.GONE);
                //myRingsProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        }

        // Retry Buttons
        myRingsBtnRetry.setOnClickListener(view -> {
            if(isNetworkConnected()) {
                loadMyRings();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                myRingsProgress.setVisibility(View.GONE);

                Timber.tag(TAG).d("No internet connection available");
            }
        });
        ringsBtnRetry.setOnClickListener(view-> {
            if(isNetworkConnected()) {
                loadHotRings();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                ringsProgress.setVisibility(View.GONE);

                Timber.tag(TAG).d("No internet connection available");
            }
        });

        return rootView;
    }

    /**
     * Load User MyRings
     */
    private void loadMyRings() {
        myRingsAdapter.clear();
        Timber.tag(TAG).d("loadMyRings: ");
        // To ensure list is visible when retry button in error view is clicked
        hideMyRingsErrorView();
        hideMyRingsEmptyView();

        String endPoint = EndPoints.MY_CHAT_ROOMS.replace("_ID_", username);
        Timber.tag(TAG).e("endPoint: %s", endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, response -> {
            Timber.tag(TAG).d("response: %s", response);
            try {
                JSONObject obj = new JSONObject(response);
                // check for error flag
                if (!obj.getBoolean("error")) {
                    JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                    if(chatRoomsArray.length() < 1) {
                        myRingsEmptyLayout.setVisibility(View.VISIBLE);
                        myRingsErrorLayout.setVisibility(View.GONE);
                        myRingsProgress.setVisibility(View.GONE);
                    } else {
                        myRingsEmptyLayout.setVisibility(View.GONE);
                        myRingsErrorLayout.setVisibility(View.GONE);
                        myRingsProgress.setVisibility(View.GONE);

                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setSpace_art(chatRoomsObj.getString("art"));

                            chatRoomArrayList.add(cr);
                        }
                    }

                } else {
                    // error in fetching chat rooms
                    myRingsErrorLayout.setVisibility(View.VISIBLE);
                    myRingsEmptyLayout.setVisibility(View.GONE);
                    myRingsProgress.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                myRingsErrorLayout.setVisibility(View.VISIBLE);
                myRingsEmptyLayout.setVisibility(View.GONE);
                myRingsProgress.setVisibility(View.GONE);
            }

            myRingsAdapter.notifyDataSetChanged();
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            myRingsErrorLayout.setVisibility(View.VISIBLE);
            myRingsEmptyLayout.setVisibility(View.GONE);
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Load Spaces
     */
    private void loadHotRings() {
        Timber.tag(TAG).d("loadRings: ");
        // To ensure list is visible when retry button in error view is clicked
        hideRingsErrorView();
        hideRingsEmptyView();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.HOT_CHAT_ROOMS, response -> {
            Timber.tag(TAG).e("Hot Rings response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);
                // check for error flag
                if (!obj.getBoolean("error")) {
                    JSONArray hRoomsArray = obj.getJSONArray("chat_rooms");
                    if(hRoomsArray.length() < 1) {
                        showRingsEmptyView();
                    } else {
                        ringsEmptyLayout.setVisibility(View.GONE);
                        ringsErrorLayout.setVisibility(View.GONE);
                        ringsProgress.setVisibility(View.GONE);

                        for (int i = 0; i < hRoomsArray.length(); i++) {
                            JSONObject hRoomsObj = (JSONObject) hRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(hRoomsObj.getString("chat_room_id"));
                            cr.setName(hRoomsObj.getString("name"));
                            cr.setSpace_art(hRoomsObj.getString("art"));

                            hotRoomsArrayList.add(cr);
                        }
                    }

                } else {
                    // error in fetching chat rooms
                    //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    ringsErrorLayout.setVisibility(View.VISIBLE);
                    ringsEmptyLayout.setVisibility(View.GONE);
                    ringsProgress.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                //Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                ringsErrorLayout.setVisibility(View.VISIBLE);
                ringsEmptyLayout.setVisibility(View.GONE);
                ringsProgress.setVisibility(View.GONE);
            }

            hotRingsAdapter.notifyDataSetChanged();
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            ringsErrorLayout.setVisibility(View.VISIBLE);
            ringsEmptyLayout.setVisibility(View.GONE);
            ringsProgress.setVisibility(View.GONE);
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private void showRingsEmptyView() {
        if (ringsEmptyLayout.getVisibility() == View.GONE) {
            ringsEmptyLayout.setVisibility(View.VISIBLE);
            ringsProgress.setVisibility(View.GONE);
        }
    }

    // Helpers -------------------------------------------------------------------------------------
    private void hideMyRingsErrorView() {
        if (myRingsErrorLayout.getVisibility() == View.VISIBLE) {
            myRingsErrorLayout.setVisibility(View.GONE);
            myRingsProgress.setVisibility(View.VISIBLE);
        }
    }

    private void hideMyRingsEmptyView() {
        if (myRingsEmptyLayout.getVisibility() == View.VISIBLE) {
            myRingsEmptyLayout.setVisibility(View.GONE);
        }
    }

    private void hideRingsErrorView() {
        if (ringsErrorLayout.getVisibility() == View.VISIBLE) {
            ringsErrorLayout.setVisibility(View.GONE);
            ringsProgress.setVisibility(View.VISIBLE);
        }
    }

    private void hideRingsEmptyView() {
        if (ringsEmptyLayout.getVisibility() == View.VISIBLE) {
            ringsEmptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyRings();
        loadHotRings();
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }
}
