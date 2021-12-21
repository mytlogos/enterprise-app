package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientEpisode
import retrofit2.Response
import retrofit2.http.*

internal interface EpisodeApi {
    @GET
    suspend fun  getEpisode(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientEpisode>

    @GET
    suspend fun getEpisodes(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<MutableList<ClientEpisode>>

    @POST
    suspend fun  addEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Response<ClientEpisode>

    @DELETE
    suspend fun  deleteEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun  updateEpisode(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}