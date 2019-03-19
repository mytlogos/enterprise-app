package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientMedium;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface MediumApi {

    @GET
    Call<ClientMedium> getMedium(@Url String url, @QueryMap Map<String, Object> body);

    @GET
    Call<List<ClientMedium>> getMedia(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<ClientMedium> addMedia(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateMedia(@Url String url, @Body Map<String, Object> body);
}
