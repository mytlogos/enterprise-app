package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientExternalUser;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface ExternalUserApi {

    @GET("{start}/refresh")
    Call<ClientExternalUser> refreshExternalUser(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET
    Call<ClientExternalUser> getExternalUser(@Url String url, @QueryMap Map<String, Object> body);

    @GET
    Call<List<ClientExternalUser>> getExternalUsers(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<ClientExternalUser> addExternalUser(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deleteExternalUser(@Url String url, @Body Map<String, Object> body);
}
