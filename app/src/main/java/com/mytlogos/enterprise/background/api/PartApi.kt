package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientPart
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface PartApi {
    @GET
    suspend fun getPart(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Response<MutableList<ClientPart>>

    @GET("{start}/items")
    suspend fun  getPartItems(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<Map<String, List<Int>>>

    @GET("{start}/releases")
    suspend fun  getPartReleases(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Response<Map<String, List<ClientSimpleRelease>>>

    @POST
    suspend fun  addPart(@Url url: String, @Body body: MutableMap<String, Any?>): Response<ClientPart>

    @DELETE
    suspend fun  deletePart(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>

    @PUT
    suspend fun  updatePart(@Url url: String, @Body body: MutableMap<String, Any?>): Response<Boolean>
}