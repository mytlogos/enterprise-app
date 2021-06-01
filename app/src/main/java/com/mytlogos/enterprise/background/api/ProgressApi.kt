package com.mytlogos.enterprise.background.api

import retrofit2.Call
import retrofit2.http.*

internal interface ProgressApi {
    @GET
    fun getProgress(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<Float>

    @POST
    fun addProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @DELETE
    fun deleteProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @PUT
    fun updateProgress(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}