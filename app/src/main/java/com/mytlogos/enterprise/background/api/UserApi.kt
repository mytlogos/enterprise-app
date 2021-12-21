package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.*
import retrofit2.Response
import retrofit2.http.*

internal interface UserApi {
    @POST("{start}/logout")
    suspend fun  logout(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun updateUser(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @GET
    suspend fun  getUser(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientUser>

    @GET("{start}/news")
    suspend fun  getNews(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientNews>>

    @GET("{start}/lists")
    suspend fun  getLists(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientMediaList>>

    @GET("{start}/download")
    suspend fun downloadEpisodes(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientDownloadedEpisode>>

    @GET("{start}/stats")
    suspend fun  getStats(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientStat>

    @GET("{start}/new")
    suspend fun  getNew(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientChangedEntities>

    @GET("{start}/toc")
    suspend fun  getToc(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientToc>>

    @DELETE("{start}/toc")
    suspend fun  removeToc(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<Boolean>

    @POST("{start}/toc")
    suspend fun  addToc(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}