package com.creator.qweekdots.api;

import com.creator.qweekdots.models.CommentsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CommentFeedService {

    @GET("genjitsu/comments.php")
    Call<CommentsModel> getComments(
            @Query("u") String user,
            @Query("id") String drop_id,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );
}
