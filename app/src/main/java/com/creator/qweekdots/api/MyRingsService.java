package com.creator.qweekdots.api;

import com.creator.qweekdots.models.MyRingsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyRingsService {
    @GET("genjitsu/my_rings.php")
    Call<MyRingsModel> getRings(
            @Query("u") String user,
            @Query("page") Integer page
    );
}
