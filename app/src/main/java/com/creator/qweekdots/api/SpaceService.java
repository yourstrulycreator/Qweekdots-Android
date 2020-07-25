package com.creator.qweekdots.api;

import com.creator.qweekdots.models.SpaceProfileModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpaceService {
    @GET("genjitsu/space.php")
    Call<SpaceProfileModel> getSpaceData(
            @Query("u") String user,
            @Query("space") String space
    );
}
