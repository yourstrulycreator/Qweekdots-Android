package com.creator.qweekdots.api;

import com.creator.qweekdots.models.FollowModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FollowingFeedService {
    @GET("genjitsu/following.php")
    Call<FollowModel> getFollowing(
            @Query("u") String user,
            @Query("l") String logged,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
