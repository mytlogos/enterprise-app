package com.mytlogos.enterprise.background.api

import retrofit2.Response
import retrofit2.http.*

internal interface ProgressApi {
    @GET
    suspend fun  getProgress(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<Float>

    @POST
    suspend fun addProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @DELETE
    suspend fun  deleteProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun  updateProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}