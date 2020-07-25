package com.creator.qweekdots.api;

import com.creator.qweekdots.models.NewsFeed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProfileFeedService {

    @GET("genjitsu/profiledrops.php")
    Call<NewsFeed> getNewsFeed(
            @Query("u") String user,
            @Query("p") String profile,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
