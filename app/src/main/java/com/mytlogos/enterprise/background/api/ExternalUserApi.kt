package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientExternalUser
import retrofit2.Response
import retrofit2.http.*

internal interface ExternalUserApi {
    @GET("{start}/refresh")
    suspend fun  refreshExternalUser(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientExternalUser>

    @GET
    suspend fun  getExternalUser(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientExternalUser>

    @GET
    suspend fun  getExternalUsers(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientExternalUser>>

    @POST
    suspend fun  addExternalUser(@Url url: String, @Body body: MutableMap<String, Any?>): Response<ClientExternalUser>

    @DELETE
    suspend fun  deleteExternalUser(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}