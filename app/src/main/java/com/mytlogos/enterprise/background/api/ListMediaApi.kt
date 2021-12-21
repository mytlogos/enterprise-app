package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientListQuery
import retrofit2.Response
import retrofit2.http.*

internal interface ListMediaApi {
    @GET
    suspend fun  getListMedia(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientListQuery>

    @POST
    suspend fun addListMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteListMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun updateListMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}