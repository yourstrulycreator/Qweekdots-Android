package com.creator.qweekdots.api;

import com.creator.qweekdots.models.DropModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MessageService {

    @GET("genjitsu/chat/message_post.php")
    Call<DropModel> getDropData(
            @Query("drop_id") String drop_id,
            @Query("u") String user
    );
}
