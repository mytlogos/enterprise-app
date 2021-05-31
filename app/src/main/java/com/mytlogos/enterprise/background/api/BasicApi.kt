package com.mytlogos.enterprise.background.api

import com.mytlogos.enterprise.background.api.model.ClientSimpleUser
import com.mytlogos.enterprise.background.api.model.ClientUser
import retrofit2.Call
import retrofit2.http.*

internal interface BasicApi {
    @GET
    fun checkLogin(@Url url: String): Call<ClientSimpleUser>

    @GET("{start}/dev")
    fun checkDev(@Path(value = "start", encoded = true) url: String): Call<Boolean>

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/login")
    fun login(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<ClientUser>

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/register")
    fun register(@Path(value = "start", encoded = true) url: String, @Body body: MutableMap<String, Any?>): Call<ClientUser>
}