package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientMedium
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface MediumApi {
    @GET
    suspend fun getMedium(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<ClientMedium>

    @GET
    suspend fun getMedia(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<List<ClientMedium>>

    @GET("{start}/all")
    fun getAllMedia(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<Int>>

    @GET("{start}/unused")
    fun getMediumInWait(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientMediumInWait>>

    @PUT("{start}/unused")
    fun consumeMediumInWait(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @POST("{start}/create")
    fun createFromMediumInWait(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<ClientMedium>

    @POST
    fun addMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Call<ClientSimpleMedium>

    @PUT
    fun updateMedia(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}