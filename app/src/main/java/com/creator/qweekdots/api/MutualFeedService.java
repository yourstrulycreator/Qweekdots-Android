package com.creator.qweekdots.api;

import com.creator.qweekdots.models.ProfileModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MutualFeedService {
    @GET("genjitsu/mutualsfeed.php")
    Call<ProfileModel> getProfileFeed(
            @Query("u") String user,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
