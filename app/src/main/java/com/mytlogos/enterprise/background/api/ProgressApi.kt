package com.mytlogos.enterprise.background.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface ProgressApi {

    @GET
    Call<Float> getProgress(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<Boolean> addProgress(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deleteProgress(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateProgress(@Url String url, @Body Map<String, Object> body);
}
