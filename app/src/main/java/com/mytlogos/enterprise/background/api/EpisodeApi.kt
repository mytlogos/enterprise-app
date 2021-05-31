package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;

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

interface EpisodeApi {

    @GET
    Call<ClientEpisode> getEpisode(@Url String url, @QueryMap Map<String, Object> body);

    @GET
    Call<List<ClientEpisode>> getEpisodes(@Url String url, @QueryMap Map<String, Object> body);

    @POST
    Call<ClientEpisode> addEpisode(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deleteEpisode(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateEpisode(@Url String url, @Body Map<String, Object> body);
}
