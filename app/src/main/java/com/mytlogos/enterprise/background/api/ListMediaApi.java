package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientMedium;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface ListMediaApi {

    @GET
    Call<List<ClientMedium>> getListMedia(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<Boolean> addListMedia(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deleteListMedia(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateListMedia(@Url String url, @Body Map<String, Object> body);
}
