package com.creator.qweekdots.api;

import com.creator.qweekdots.models.RingsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RingsService {
    @GET("genjitsu/chat/chat_rooms")
    Call<RingsModel> getRings(
    );
}
