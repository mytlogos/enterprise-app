package com.mytlogos.enterprise.background.api;


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
    Call<ClientUser> checkLogin(@Url String url);

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/login")
    Call<ClientUser> login(@Path("start") String url, @Body Map<String, Object> body);

    // start ends with an slash (/), so no need to use it again
    @POST("{start}/register")
    Call<ClientUser> register(@Path("start") String url, @Body Map<String, Object> body);


}

