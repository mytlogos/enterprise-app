package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientExternalUser
import retrofit2.Call
import retrofit2.http.*

internal interface ExternalUserApi {
    @GET("{start}/refresh")
    fun refreshExternalUser(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientExternalUser>

    @GET
    fun getExternalUser(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<ClientExternalUser>

    @GET
    fun getExternalUsers(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientExternalUser>>

    @POST
    fun addExternalUser(@Url url: String, @Body body: MutableMap<String, Any?>): Call<ClientExternalUser>

    @DELETE
    fun deleteExternalUser(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}