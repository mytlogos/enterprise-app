package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientSimpleUser
import com.mytlogos.enterprise.background.api.model.ClientUser
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

internal interface BasicApi {
    @GET
    suspend fun  checkLogin(@Url url: String): Response<ClientSimpleUser>

    @GET("{start}/dev")
    suspend fun  checkDev(@Path(value = "start", encoded = true) url: String): Response<Boolean>

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/login")
    suspend fun login(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<ClientUser>

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/register")
    suspend fun register(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Response<ClientUser>
}