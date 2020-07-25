package com.creator.qweekdots.api;

import com.creator.qweekdots.models.Suggestions;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SuggestionsService {
    @GET("genjitsu/suggestions.php")
    Call<Suggestions> getSuggestions(
            @Query("u") String user,
            @Query("l") String logged
    );
}
