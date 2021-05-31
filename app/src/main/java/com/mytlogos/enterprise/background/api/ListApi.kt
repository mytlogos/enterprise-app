package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface ListApi {

    @GET
    Call<ClientListQuery> getList(@Url String url, @QueryMap Map<String, Object> body);

    @GET
    Call<ClientMultiListQuery> getLists(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<ClientMediaList> addList(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deleteList(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateList(@Url String url, @Body Map<String, Object> body);
}
