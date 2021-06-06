package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientEpisode
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface EpisodeApi {
    @GET
    fun getEpisode(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientEpisode>

    @GET
    suspend fun getEpisodes(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<MutableList<ClientEpisode>>

    @POST
    fun addEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Call<ClientEpisode>

    @DELETE
    fun deleteEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @PUT
    fun updateEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}