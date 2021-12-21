package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientListQuery
import com.mytlogos.enterprise.background.api.model.ClientMediaList
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery
import retrofit2.Response
import retrofit2.http.*

internal interface ListApi {
    @GET
    suspend fun getList(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientListQuery>

    @GET
    suspend fun  getLists(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientMultiListQuery>

    @POST
    suspend fun addList(@Url url: String, @Body body: MutableMap<String, Any?>): Response<ClientMediaList>

    @DELETE
    suspend fun  deleteList(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun updateList(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}