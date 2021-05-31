package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientPart
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease
import retrofit2.Call
import retrofit2.http.*

internal interface PartApi {
    @GET
    fun getPart(@Url url: String, @QueryMap body: MutableMap<String, Any?>): Call<List<ClientPart>>

    @GET("{start}/items")
    fun getPartItems(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<Map<String, List<Int>>>

    @GET("{start}/releases")
    fun getPartReleases(@Path(value = "start", encoded = true) url: String, @QueryMap body: MutableMap<String, Any?>): Call<Map<String, List<ClientSimpleRelease>>>

    @POST
    fun addPart(@Url url: String, @Body body: MutableMap<String, Any?>): Call<ClientPart>

    @DELETE
    fun deletePart(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>

    @PUT
    fun updatePart(@Url url: String, @Body body: MutableMap<String, Any?>): Call<Boolean>
}