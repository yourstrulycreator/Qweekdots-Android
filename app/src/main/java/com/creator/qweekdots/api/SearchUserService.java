package com.creator.qweekdots.api;

import com.creator.qweekdots.models.SearchUserModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchUserService {
    @GET("genjitsu/search/user.php")
    Call<SearchUserModel> getSearchedUsers(
            @Query("query") String query,
            @Query("user") String user,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
