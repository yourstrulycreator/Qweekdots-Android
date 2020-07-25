package com.creator.qweekdots.api;

import com.creator.qweekdots.models.TrendsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TrendsService {
    @GET("genjitsu/trends.php")
    Call<TrendsModel> getTrends(
            @Query("u") String user
    );
}
