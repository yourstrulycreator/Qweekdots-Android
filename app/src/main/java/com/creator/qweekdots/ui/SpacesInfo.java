package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.HotSpacesAdapter;
import com.creator.qweekdots.adapter.MySpacesAdapter;
import com.creator.qweekdots.adapter.NewMySpacesAdapter;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SpaceService;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.models.SpaceItem;
import com.creator.qweekdots.models.SpaceProfileModel;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SpacesInfo extends Fragment {
    private final String TAG = SpacesInfo.class.getSimpleName();
    private ImageView spaceImage;
    private CircularProgressButton pinBtn;

    private String pinned;

    private SpaceService spaceService;
    private List<SpaceItem> spaceItem;
    private SpaceItem space;

    private NewMySpacesAdapter spacesAdapter;
    private SpinKitView spacesProgress;
    private LinearLayout spacesErrorLayout, spacesEmptyLayout;
    private ArrayList<ChatRoom> chatRoomArrayList;

    private String username;
    private String chatRoomId;

    private TextView spaceTitle;
    private TextView membersNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.spaces_info, container, false);

        if (new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        Bundle bundle = this.getArguments();

        username = bundle.getString("username");
        chatRoomId = bundle.getString("chat_room_id");
        String title = bundle.getString("title");
        String selfUserId = bundle.getString("self_user_id");


        // Follow Implementation for Spaces
        pinBtn = rootView.findViewById(R.id.pinActionButton);

        pinBtn.setOnClickListener(v -> {
            pinBtn.startAnimation();
            if(pinned.equals("yes")) {
                doPin(username, "unpin", chatRoomId);
                space.setPinned("no");
            } else if(pinned.equals("no")) {
                doPin(username, "pin", chatRoomId);
                space.setPinned("yes");
            }
        });

        spaceImage = rootView.findViewById(R.id.spaceImage);
        //init service and load data
        spaceService = QweekdotsApi.getClient(requireContext()).create(SpaceService.class);

        RecyclerView spacesV = rootView.findViewById(R.id.spacesRecyclerView);
        spacesProgress = rootView.findViewById(R.id.spacesProgress);
        spacesErrorLayout = rootView.findViewById(R.id.spaces_error_layout);
        spacesEmptyLayout = rootView.findViewById(R.id.spaces_empty_layout);
        Button spacesBtnRetry = rootView.findViewById(R.id.spaces_error_btn_retry);

        //
        spaceTitle = rootView.findViewById(R.id.spaceTitle);
        membersNum = rootView.findViewById(R.id.spaceMembersNum);

        //Similar
        chatRoomArrayList = new ArrayList<>();
        //LinearLayoutManager myRingsLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        spacesV.setLayoutManager(new GridLayoutManager(getContext(), 3));
        spacesAdapter = new NewMySpacesAdapter(getActivity(), this, username, chatRoomArrayList);
        spacesV.setAdapter(spacesAdapter);
        spacesV.setItemAnimator(new DefaultItemAnimator());


        if(isAdded()) {
            if (isNetworkConnected()) {
                loadSpace();
                loadSimilar();
                Timber.tag(TAG).d("Loading First Pages");
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                Timber.tag(TAG).d("No internet connection available");
            }
        }

        // Retry Buttons
        spacesBtnRetry.setOnClickListener(view-> {
            if(isNetworkConnected()) {
                loadSimilar();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                spacesProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });

        return rootView;
    }

    private void loadSpace() {
        callSpaceProfileApi().enqueue(new Callback<SpaceProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<SpaceProfileModel> call, @NotNull Response<SpaceProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                spaceItem = fetchSpaceProfile(response);
                space = spaceItem.get(0);

                spaceTitle.setText(space.getSpacename());

                membersNum.setText(space.getFollowed_count());

                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565);
                Glide
                        .with(requireContext())
                        .load(space.getSpaceArt())
                        .thumbnail(0.7f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(requestOptions)
                        .into(spaceImage);

                pinned = space.getPinned();

                if(pinned.equals("yes")) {
                    pinBtn.setText("Pinned");
                } else {
                    pinBtn.setText("Pin");
                }
            }

            @Override
            public void onFailure(@NotNull Call<SpaceProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * @param response extracts List<{@link com.creator.qweekdots.models.SpaceProfileModel >} from response
     *
     */
    private List<SpaceItem> fetchSpaceProfile(Response<SpaceProfileModel> response) {
        SpaceProfileModel profileModel = response.body();
        assert profileModel != null;
        return profileModel.getSpaceItems();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     *
     */
    private Call<SpaceProfileModel> callSpaceProfileApi() {
        return  spaceService.getSpaceData(
                username,
                chatRoomId
        );
    }

    /**
     * Load Hot Spaces
     */
    private void loadSimilar() {
        spacesAdapter.clear();
        Timber.tag(TAG).d("loadHotSpaces: ");
        // To ensure list is visible when retry button in error view is clicked
        hideSpacesErrorView();
        hideSpacesEmptyView();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                "https://qweek.fun/genjitsu/similar_spaces?u="+username+"&id="+chatRoomId+"", response -> {
            Timber.tag(TAG).d("response: %s", response);
            try {
                JSONObject obj = new JSONObject(response);
                // check for error flag
                JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                if(chatRoomsArray.length() < 1) {
                    spacesEmptyLayout.setVisibility(View.VISIBLE);
                    spacesErrorLayout.setVisibility(View.GONE);
                    spacesProgress.setVisibility(View.GONE);
                } else {
                    spacesEmptyLayout.setVisibility(View.GONE);
                    spacesErrorLayout.setVisibility(View.GONE);
                    spacesProgress.setVisibility(View.GONE);

                    for (int i = 0; i < chatRoomsArray.length(); i++) {
                        JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                        ChatRoom cr = new ChatRoom();
                        cr.setId(chatRoomsObj.getString("chat_room_id"));
                        cr.setName(chatRoomsObj.getString("name"));
                        cr.setSpace_art(chatRoomsObj.getString("art"));

                        chatRoomArrayList.add(cr);
                    }
                }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                spacesErrorLayout.setVisibility(View.VISIBLE);
                spacesEmptyLayout.setVisibility(View.GONE);
                spacesProgress.setVisibility(View.GONE);
            }

            spacesAdapter.notifyDataSetChanged();
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            spacesErrorLayout.setVisibility(View.VISIBLE);
            spacesEmptyLayout.setVisibility(View.GONE);
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    @SuppressLint("SetTextI18n")
    private void doPin(String pinner, String type, String pinned) {
        // Tag used to cancel the request
        String tag_string_req = "req_pin";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/parse/space_pin.php", response -> {
            Timber.tag(TAG).d("Pin Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    stopButtonAnimation();

                    if(type.equals("pin")) {
                        pinBtn.setText("Pinned");
                        pinBtn.saveInitialState();
                    } else {
                        pinBtn.setText("Pin");
                        pinBtn.saveInitialState();
                    }

                    //String sent = "Pinned to MySpaces";
                    //Toasty.success(getApplicationContext(), sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Pin Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("pinner", pinner);
                params.put("type", type);
                params.put("pinned", pinned);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void stopButtonAnimation(){
        pinBtn.revertAnimation();
    }

    // Helpers -------------------------------------------------------------------------------------

    private void hideSpacesErrorView() {
        if (spacesErrorLayout.getVisibility() == View.VISIBLE) {
            spacesErrorLayout.setVisibility(View.GONE);
        }
    }

    private void hideSpacesEmptyView() {
        if (spacesEmptyLayout.getVisibility() == View.VISIBLE) {
            spacesEmptyLayout.setVisibility(View.GONE);
        }
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
