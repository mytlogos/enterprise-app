package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientChangedEntities;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientUser;
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
    Call<Boolean> logout(@Path(value = "start", encoded = true) String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updateUser(@Url String url, @Body Map<String, Object> body);

    @GET
    Call<ClientUser> getUser(@Url String url, @QueryMap Map<String, Object> body);

    @GET("{start}/news")
    Call<List<ClientNews>> getNews(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/lists")
    Call<List<ClientMediaList>> getLists(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/invalidated")
    Call<List<InvalidatedData>> getInvalidated(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/download")
    Call<List<ClientDownloadedEpisode>> downloadEpisodes(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/stats")
    Call<ClientStat> getStats(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/new")
    Call<ClientChangedEntities> getNew(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    // TODO: 22.07.2019 add toc {uuid, toc: string, mediumId} ?
}
