package com.creator.qweekdots.api;

import com.creator.qweekdots.models.ExplorePhotos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExplorePhotosService {
    @GET("genjitsu/hot_photos.php")
    Call<ExplorePhotos> getExplorePhotos(
            @Query("u") String user,
            @Query("page") Integer page
    );
}
