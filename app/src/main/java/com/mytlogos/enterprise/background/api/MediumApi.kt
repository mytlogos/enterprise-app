package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientMedium
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import retrofit2.Response
import retrofit2.http.*

internal interface MediumApi {
    @GET
    suspend fun getMedium(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientMedium>

    @GET
    suspend fun getMedia(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientMedium>>

    @GET("{start}/all")
    suspend fun  getAllMedia(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<Int>>

    @GET("{start}/unused")
    suspend fun  getMediumInWait(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientMediumInWait>>

    @PUT("{start}/unused")
    suspend fun consumeMediumInWait(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @POST("{start}/create")
    suspend fun createFromMediumInWait(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<ClientMedium>

    @POST
    suspend fun  addMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Response<ClientSimpleMedium>

    @PUT
    suspend fun updateMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}