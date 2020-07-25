package com.creator.qweekdots.api;

import com.creator.qweekdots.models.NotificationsFeed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationService {

    @GET("genjitsu/notifications.php")
    Call<NotificationsFeed> getNotificationsFeed(
            @Query("u") String user,
            @Query("max_id") String max_id,
            @Query("since_id") String since_id
    );

}
