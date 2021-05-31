package com.mytlogos.enterprise.background.api;

import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

interface PartApi {

    @GET
    Call<List<ClientPart>> getPart(@Url String url, @QueryMap Map<String, Object> body);

    @GET("{start}/items")
    Call<Map<String, List<Integer>>> getPartItems(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @GET("{start}/releases")
    Call<Map<String, List<ClientSimpleRelease>>> getPartReleases(@Path(value = "start", encoded = true) String url, @QueryMap Map<String, Object> body);

    @POST
    Call<ClientPart> addPart(@Url String url, @Body Map<String, Object> body);

    @DELETE
    Call<Boolean> deletePart(@Url String url, @Body Map<String, Object> body);

    @PUT
    Call<Boolean> updatePart(@Url String url, @Body Map<String, Object> body);
}
