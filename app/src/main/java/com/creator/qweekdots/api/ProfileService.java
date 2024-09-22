package com.creator.qweekdots.api;

import com.creator.qweekdots.models.ProfileModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProfileService {
    @GET("genjitsu/profile.php")
    Call<ProfileModel> getProfileData(
            @Query("u") String user,
            @Query("p") String profile
    );
}
