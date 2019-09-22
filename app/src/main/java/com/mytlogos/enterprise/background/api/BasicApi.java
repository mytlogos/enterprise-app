package com.mytlogos.enterprise.background.api;


import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

interface BasicApi {

    @GET
    Call<ClientSimpleUser> checkLogin(@Url String url);

    @GET("{start}/dev")
    Call<Boolean> checkDev(@Path(value = "start", encoded = true) String url);

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/login")
    Call<ClientUser> login(@Path(value = "start", encoded = true) String url, @Body Map<String, Object> body);

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/register")
    Call<ClientUser> register(@Path(value = "start", encoded = true) String url, @Body Map<String, Object> body);
}

