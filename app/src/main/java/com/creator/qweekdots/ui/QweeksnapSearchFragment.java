package com.creator.qweekdots.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.creator.qweekdots.R;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.annotations.SerializedName;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class QweeksnapSearchFragment extends Fragment {
    View rootView;
    private final String TAG = QweeksnapSearchFragment.class.getSimpleName();

    private static final String BASE_URL = "http://10.0.2.2:8080/qweekdots/";
    class SnapSearch {
        @SerializedName("id")
        private Integer id;
        @SerializedName("drop_id")
        private String drop_id;
        @SerializedName("username")
        private String username;
        @SerializedName("fullname")
        private String fullname;
        @SerializedName("type")
        private String type;
        @SerializedName("drop")
        private String drop;
        @SerializedName("hasMedia")
        private Integer hasMedia;
        @SerializedName("hasLink")
        private Integer hasLink;
        @SerializedName("qweeksnap")
        private String qweeksnap;
        @SerializedName("url")
        private String url;
        @SerializedName("audio")
        private String audio;
        @SerializedName("profilePic")
        private String profilePic;
        @SerializedName("timeStamp")
        private String timeStamp;
        @SerializedName("replied")
        private String replied;
        @SerializedName("liked")
        private String liked;
        @SerializedName("upvoted")
        private String upvoted;
        @SerializedName("downvoted")
        private String downvoted;
        @SerializedName("commentNum")
        private String commentNum;
        @SerializedName("upvoteNum")
        private String upvoteNum;
        @SerializedName("downvoteNum")
        private String downvoteNum;

        public SnapSearch(Integer id, String drop_id,
                          String username, String fullname, String type,
                          String drop, Integer hasMedia, Integer hasLink,
                          String qweeksnap, String url, String audio,
                          String profilePic, String timeStamp, String replied, String liked,
                          String upvoted, String downvoted,
                          String commentNum, String upvoteNum, String downvoteNum) {
            this.id = id;
            this.drop_id = drop_id;
            this.username = username;
            this.fullname = fullname;
            this.type = type;
            this.drop = drop;
            this.hasMedia = hasMedia;
            this.hasLink = hasLink;
            this.qweeksnap = qweeksnap;
            this.url = url;
            this.audio = audio;
            this.profilePic = profilePic;
            this.timeStamp = timeStamp;
            this.replied = replied;
            this.liked = liked;
            this.upvoted = upvoted;
            this.downvoted = downvoted;
            this.commentNum = commentNum;
            this.upvoteNum = upvoteNum;
            this.downvoteNum = downvoteNum;
        }

        /*
         *GETTERS AND SETTERS
         */
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getDrop_Id() {
            return drop_id;
        }
        public String getUsername() {
            return username;
        }
        public String getFullname() {
            return fullname;
        }
        public String getType() {
            return type;
        }
        public String getDrop() {
            return drop;
        }
        public Integer getHasMedia() {
            return hasMedia;
        }
        public Integer getHasLink() {
            return hasLink;
        }
        public String getQweekSnap() {
            return qweeksnap;
        }
        public String getLink() {
            return url;
        }
        public String getAudio() {
            return audio;
        }
        public String getProfilePic() {
            return profilePic;
        }
        public String getTimeStamp() {
            return timeStamp;
        }
        public String getReplied() {
            return replied;
        }
        public String getLiked() {
            return liked;
        }
        public String getUpvoted() { return upvoted; }
        public String getDownvoted() { return downvoted; }
        public String getCommentNum() {
            return commentNum;
        }
        public String getUpvoteNum() {
            return upvoteNum;
        }
        public String getDownvoteNum() {
            return downvoteNum;
        }

        String setLiked(String liked) {
            this.liked = liked;
            return liked;
        }
        String setUpvoted(String upvoted) {
            this.upvoted = upvoted;
            return upvoted;
        }
        String setDownvoted(String downvoted) {
            this.downvoted = downvoted;
            return downvoted;
        }

        @Override
        public String toString() {
            return username;
        }
    }

    interface MyAPIService {
        @GET("/qweekdots/genjitsu/search/qweeksnap.php")
        Call<List<QweeksnapSearchFragment.SnapSearch>> searchDrops(@Query("query") String query, @Query("user") String username);
    }

    static class RetrofitClientInstance {
        private static Retrofit retrofit;

        static Retrofit getRetrofitInstance() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }
    class ListViewAdapter extends BaseAdapter {

        private List<QweeksnapSearchFragment.SnapSearch> drops;
        private Context context;

        public ListViewAdapter(Context context, List<QweeksnapSearchFragment.SnapSearch> drops) {
            this.context = context;
            this.drops = drops;
        }

        @Override
        public int getCount() {
            return drops.size();
        }

        @Override
        public Object getItem(int pos) {
            return drops.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.search_item, viewGroup, false);
            }

            TextView name;
            TextView timestamp;
            TextView statusMsg;
            TextView url;
            CardView imageCard;
            CircleImageView profilePic;
            ImageView feedImageView;
            SpinKitView mProgress;
            name = view.findViewById(R.id.name);
            timestamp = view.findViewById(R.id.timestamp);
            statusMsg = view.findViewById(R.id.txtStatusMsg);
            url = view.findViewById(R.id.txtUrl);
            imageCard = view.findViewById(R.id.feedImageCard);
            profilePic = view.findViewById(R.id.profilePic);
            feedImageView = view.findViewById(R.id.qweekSnap);
            mProgress = view.findViewById(R.id.qweekSnap_progress);

            final QweeksnapSearchFragment.SnapSearch thisDrop = drops.get(position);

            String full_name = thisDrop.getFullname() + " " + "@" + thisDrop.getUsername();

            name.setText(full_name);

            if (thisDrop.getProfilePic() != null && thisDrop.getProfilePic().length() > 0) {
                Picasso.get().load(BASE_URL + "/profiles/" + thisDrop.getProfilePic()).placeholder(R.drawable.q_users).into(profilePic);
            } else {
                Picasso.get().load(R.drawable.q_users).into(profilePic);
            }

            view.setOnClickListener(view1 -> {
                DropBottomSheet bottomSheet = new DropBottomSheet(context, username, thisDrop.getDrop_Id());
                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
            });

            return view;
        }
    }
    private QweeksnapSearchFragment.ListViewAdapter adapter;
    private ListView mListView;
    private SpinKitView mProgressBar;

    private void populateListView(List<QweeksnapSearchFragment.SnapSearch> dropList) {
        adapter = new QweeksnapSearchFragment.ListViewAdapter(getContext(), dropList);
        mListView.setAdapter(adapter);
    }

    private String username;
    private String fullname;
    private String profile_pic;

    private MyAPIService myAPIService;

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

        /*Create handle for the RetrofitInstance interface*/
        myAPIService = QweeksnapSearchFragment.RetrofitClientInstance.getRetrofitInstance().create(QweeksnapSearchFragment.MyAPIService.class);


        return rootView;
    }

    public void beginSearch(String query) {
        final Call<List<QweeksnapSearchFragment.SnapSearch>> call = myAPIService.searchDrops(query, username);
        call.enqueue(new Callback<List<QweeksnapSearchFragment.SnapSearch>>() {

            @Override
            public void onResponse(@NotNull Call<List<QweeksnapSearchFragment.SnapSearch>> call, @NotNull Response<List<QweeksnapSearchFragment.SnapSearch>> response) {
                mProgressBar.setVisibility(View.GONE);
                populateListView(response.body());
            }
            @Override
            public void onFailure(@NotNull Call<List<QweeksnapSearchFragment.SnapSearch>> call, @NotNull Throwable throwable) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
