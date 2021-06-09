package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface UserApi {
    @POST("{start}/logout")
    fun logout(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @PUT
    suspend fun updateUser(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @GET
    fun getUser(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientUser>

    @GET("{start}/news")
    fun getNews(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientNews>>

    @GET("{start}/lists")
    fun getLists(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientMediaList>>

    @GET("{start}/download")
    suspend fun downloadEpisodes(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientDownloadedEpisode>>

    @GET("{start}/stats")
    fun getStats(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientStat>

    @GET("{start}/new")
    fun getNew(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientChangedEntities>

    @GET("{start}/toc")
    fun getToc(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientToc>>

    @DELETE("{start}/toc")
    fun removeToc(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<Boolean>

    @POST("{start}/toc")
    fun addToc(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}