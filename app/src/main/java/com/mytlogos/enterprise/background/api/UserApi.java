package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.InvalidatedData;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface UserApi {

    @POST("{start}/logout")
    Call<Boolean> logout(@Path("start") String url, @QueryMap Map<String, Object> body);

    @PUT
    Call<Boolean> updateUser(@Url String url, @Body Map<String, Object> body);

    @GET("{start}/news")
    Call<List<ClientNews>> getNews(@Path("start") String url, @QueryMap Map<String, Object> body);

    @GET("{start}/lists")
    Call<List<ClientMediaList>> getLists(@Path("start") String url, @QueryMap Map<String, Object> body);

    @GET("{start}/invalidated")
    Call<List<InvalidatedData>> getInvalidated(@Path("start") String url, @QueryMap Map<String, Object> body);
}
