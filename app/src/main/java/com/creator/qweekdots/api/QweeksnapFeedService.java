package com.creator.qweekdots.api;

import com.creator.qweekdots.models.NewsFeed;
import com.creator.qweekdots.models.ProfileQweekSnaps;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QweeksnapFeedService {
    @GET("genjitsu/profileqweeksnaps.php")
    Call<ProfileQweekSnaps> getNewsFeed(
            @Query("u") String user,
            @Query("p") String profile,
            @Query("page") Integer page
    );
}
