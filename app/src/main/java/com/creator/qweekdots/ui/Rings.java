package com.creator.qweekdots.ui;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.service.FCMIntentService;
import com.creator.qweekdots.view.StaggeredGridView;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class Rings extends Fragment {
    private final String TAG = Rings.class.getSimpleName();
    private MyRingsAdapter myRingsAdapter;
    private SpinKitView ringsProgress, myRingsProgress;
    private LinearLayout myRingsErrorLayout, ringsErrorLayout;
    private TextView myRingsTxtError, ringsTxtError;
    private LinearLayout myRingsEmptyLayout, ringsEmptyLayout;
    private StaggeredGridView mStaggeredView;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private String username, selfUserId;

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
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        myRingsProgress = rootView.findViewById(R.id.myRingsProgress);
        RecyclerView myRingsV = rootView.findViewById(R.id.myRingsRecyclerView);
        ringsProgress = rootView.findViewById(R.id.ringsProgress);

        myRingsErrorLayout = rootView.findViewById(R.id.my_rings_error_layout);
        ringsErrorLayout = rootView.findViewById(R.id.rings_error_layout);

        myRingsEmptyLayout = rootView.findViewById(R.id.my_rings_empty_layout);
        ringsEmptyLayout = rootView.findViewById(R.id.rings_empty_layout);

        Button myRingsBtnRetry = rootView.findViewById(R.id.my_rings_error_btn_retry);
        Button ringsBtnRetry = rootView.findViewById(R.id.rings_error_btn_retry);

        myRingsTxtError = rootView.findViewById(R.id.my_rings_error_txt_cause);
        ringsTxtError = rootView.findViewById(R.id.rings_error_txt_cause);

        // My Rings
        chatRoomArrayList = new ArrayList<>();
        LinearLayoutManager myRingsLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myRingsV.setLayoutManager(myRingsLinearLayoutManager);
        myRingsAdapter = new MyRingsAdapter(getActivity(), this, username, chatRoomArrayList);
        myRingsV.setAdapter(myRingsAdapter);
        myRingsV.setItemAnimator(new DefaultItemAnimator());

        // Rings GridView
        StaggeredGridView.OnScrollListener scrollListener = new StaggeredGridView.OnScrollListener() {
            public void onTop() {
            }

            public void onScroll() {
            }

            public void onBottom() {
            }
        };
        mStaggeredView = rootView.findViewById(R.id.rings_grid);
        // Be sure before calling initialize that you haven't initialised from XML
        mStaggeredView.setOnScrollListener(scrollListener);

        if(isAdded()) {
            if(isNetworkConnected()) {
                loadRings();
                //loadMyRings();

                Timber.tag(TAG).d("Loading First Rings Pages and My Rings");
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
                loadRings();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                ringsProgress.setVisibility(View.GONE);

                Timber.tag(TAG).d("No internet connection available");
            }
        });

        return rootView;
    }

    //Function to load my Rings
    private void loadMyRings() {
        myRingsAdapter.clear();
        Timber.tag(TAG).d("loadMyRings: ");

        // To ensure list is visible when retry button in error view is clicked
        hideMyRingsErrorView();
        hideMyRingsEmptyView();

        String endPoint = EndPoints.MY_CHAT_ROOMS.replace("_ID_", username);
        Log.e(TAG, "endPoint: " + endPoint);

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

    private void loadRings() {
        Timber.tag(TAG).d("loadRings: ");

        // To ensure list is visible when retry button in error view is clicked
        hideRingsErrorView();
        hideRingsEmptyView();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.CHAT_ROOMS, response -> {
            Timber.tag(TAG).d("response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                // check for error flag
                if (!obj.getBoolean("error")) {
                    JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                    if(chatRoomsArray.length() < 1) {
                        showRingsEmptyView();
                    } else {
                        ringsEmptyLayout.setVisibility(View.GONE);
                        ringsErrorLayout.setVisibility(View.GONE);
                        ringsProgress.setVisibility(View.GONE);

                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setLastMessage("");
                            cr.setUnreadCount(0);
                            cr.setTimestamp(chatRoomsObj.getString("created_at"));

                            StaggeredGridViewItem item;

                            item = new RingsAdapter(getActivity(), cr, username, i);
                            mStaggeredView.addItem(item);
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

            // subscribing to all chat room topics
            subscribeToAllTopics();
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

    // Subscribing to all chat room topics
    // each topic name starts with `topic_` followed by the ID of the chat room
    // Ex: topic_1, topic_2
    private void subscribeToAllTopics() {
        for (ChatRoom cr : chatRoomArrayList) {

            Intent intent = new Intent(getActivity(), FCMIntentService.class);
            intent.putExtra(FCMIntentService.KEY, FCMIntentService.SUBSCRIBE);
            intent.putExtra(FCMIntentService.TOPIC, "topic_" + cr.getId());
            requireActivity().startService(intent);
        }
    }

    /**
     */
    private void showMyRingsErrorView() {

        if (myRingsErrorLayout.getVisibility() == View.GONE) {
            myRingsErrorLayout.setVisibility(View.VISIBLE);
            myRingsProgress.setVisibility(View.GONE);

            myRingsTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showMyRingsEmptyView() {
        if (myRingsEmptyLayout.getVisibility() == View.GONE) {
            myRingsEmptyLayout.setVisibility(View.VISIBLE);
            myRingsProgress.setVisibility(View.GONE);
        }
    }

    private void showRingsErrorView() {

        if (ringsErrorLayout.getVisibility() == View.GONE) {
            ringsErrorLayout.setVisibility(View.VISIBLE);
            ringsProgress.setVisibility(View.GONE);

            ringsTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showRingsEmptyView() {
        if (ringsEmptyLayout.getVisibility() == View.GONE) {
            ringsEmptyLayout.setVisibility(View.VISIBLE);
            ringsProgress.setVisibility(View.GONE);
        }
    }

    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
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
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }
}
