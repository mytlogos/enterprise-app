package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientNews;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface NewsApi {

    @GET()
    Call<List<ClientNews>> getNews(@Url String url, @QueryMap Map<String, Object> body);
}
