package com.creator.qweekdots.api;

import com.creator.qweekdots.models.passServerRequest;
import com.creator.qweekdots.models.passServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface passRequestInterface {

    @POST("parse/password_reset.php")
    Call<passServerResponse> operation(@Body passServerRequest request);

}
