package com.creator.qweekdots.api;

import com.creator.qweekdots.models.SearchDropModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchDropService {
    @GET("genjitsu/search/drop.php")
    Call<SearchDropModel> getSearchedDrops(
            @Query("query") String query,
            @Query("user") String user,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
