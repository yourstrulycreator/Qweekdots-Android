package com.creator.qweekdots.api;

import com.creator.qweekdots.models.ExplorePhotos;
import com.creator.qweekdots.models.NewsFeed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExplorePhotosService {
    @GET("genjitsu/hot_photos.php")
    Call<NewsFeed> getNewsFeed(
            @Query("u") String user
    );
}
