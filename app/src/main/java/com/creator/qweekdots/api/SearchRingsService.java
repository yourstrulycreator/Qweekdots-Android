package com.creator.qweekdots.api;

import com.creator.qweekdots.models.SearchRingsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchRingsService {
    @GET("genjitsu/search/rings.php")
    Call<SearchRingsModel> getSearchedRings(
            @Query("query") String query,
            @Query("user") String user,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
