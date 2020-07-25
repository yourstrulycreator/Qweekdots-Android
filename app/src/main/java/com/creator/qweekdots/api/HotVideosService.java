package com.creator.qweekdots.api;

import com.creator.qweekdots.models.HotVideos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HotVideosService {

    @GET("genjitsu/qweekfeed.php")
    Call<HotVideos> getHotVideos(
            @Query("u") String user
    );
}