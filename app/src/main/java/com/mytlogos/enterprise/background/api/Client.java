package com.mytlogos.enterprise.background.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mytlogos.enterprise.background.api.model.AddClientExternalUser;
import com.mytlogos.enterprise.background.api.model.Authentication;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.api.model.InvalidatedData;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    private static Map<Class<?>, Retrofit> retrofitMap = new HashMap<>();
    private static Map<Class<?>, String> fullClassPathMap = new HashMap<>();

    static {
        buildPathMap();
    }

    private Authentication authentication;

    public Client() {
//        InetAddress.getByName("").isReachable(100);
    }

    private static void buildPathMap() {
        Map<Class<?>, Class<?>> parentClassMap = new HashMap<>();
        Map<Class<?>, String> classPathMap = new HashMap<>();

        // set up the path pieces between each api
        classPathMap.put(BasicApi.class, "api");
        classPathMap.put(UserApi.class, "user");
        classPathMap.put(ExternalUserApi.class, "externalUser");
        classPathMap.put(ListApi.class, "list");
        classPathMap.put(ListMediaApi.class, "medium");
        classPathMap.put(MediumApi.class, "medium");
        classPathMap.put(PartApi.class, "part");
        classPathMap.put(EpisodeApi.class, "episode");
        classPathMap.put(ProgressApi.class, "progress");

        parentClassMap.put(UserApi.class, BasicApi.class);
        parentClassMap.put(ExternalUserApi.class, UserApi.class);
        parentClassMap.put(ListApi.class, UserApi.class);
        parentClassMap.put(ListMediaApi.class, ListApi.class);
        parentClassMap.put(MediumApi.class, UserApi.class);
        parentClassMap.put(PartApi.class, MediumApi.class);
        parentClassMap.put(EpisodeApi.class, PartApi.class);
        parentClassMap.put(ProgressApi.class, MediumApi.class);

        for (Class<?> apiClass : classPathMap.keySet()) {
            StringBuilder builder = new StringBuilder();

            for (Class<?> parent = apiClass; parent != null; parent = parentClassMap.get(parent)) {
                String pathPiece = classPathMap.get(parent);

                if (parent != apiClass) {
                    builder.insert(0, "/");
                }

                if (pathPiece == null) {
                    String canonicalName = apiClass.getCanonicalName();
                    throw new IllegalStateException("Api has no path piece: " + canonicalName);
                }
                builder.insert(0, pathPiece);
            }

            fullClassPathMap.put(apiClass, builder.toString());
        }
    }

    public void setAuthentication(String uuid, String session) {
        this.authentication = new Authentication(uuid, session);
    }

    public Call<ClientUser> checkLogin() {
        return build(BasicApi.class, BasicApi::checkLogin);
    }

    private Map<String, Object> userAuthenticationMap() {
        Map<String, Object> body = new HashMap<>();

        if (this.authentication == null) {
            throw new IllegalStateException("user not authenticated");
        }

        body.put("uuid", this.authentication.getUuid());
        body.put("session", this.authentication.getSession());
        return body;
    }

    private Map<String, Object> userVerificationMap(String mailName, String password) {
        Map<String, Object> body = new HashMap<>();

        body.put("userName", mailName);
        body.put("pw", password);
        return body;
    }

    public Call<ClientUser> login(String mailName, String password) {
        return build(BasicApi.class, (apiImpl, url) ->
                apiImpl.login(
                        url,
                        this.userVerificationMap(mailName, password)
                ));
    }

    public Call<ClientUser> register(String mailName, String password) {
        return build(BasicApi.class, (apiImpl, url) ->
                apiImpl.register(
                        url,
                        this.userVerificationMap(mailName, password)
                ));
    }

    public Call<Boolean> logout() {
        Map<String, Object> body = this.userAuthenticationMap();
        return build(UserApi.class, (apiImpl, url) -> apiImpl.logout(url, body));
    }

    public Call<Boolean> updateUser(ClientUpdateUser updateUser) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("user", updateUser);
        return build(UserApi.class, (apiImpl, url) -> apiImpl.updateUser(url, body));
    }

    public Call<List<ClientNews>> getNews(DateTime from, DateTime to) {
        Map<String, Object> body = this.userAuthenticationMap();
        if (from != null) {
            body.put("from", from);
        }
        if (to != null) {
            body.put("to", to);
        }
        return build(UserApi.class, (apiImpl, url) -> apiImpl.getNews(url, body));
    }

    public Call<List<ClientNews>> getNews(Collection<Integer> newsIds) {
        Map<String, Object> body = this.userAuthenticationMap();
        if (newsIds != null) {
            body.put("newsId", newsIds);
        }
        return build(UserApi.class, (apiImpl, url) -> apiImpl.getNews(url, body));
    }

    public Call<List<ClientMediaList>> getLists() {
        Map<String, Object> body = this.userAuthenticationMap();
        return build(UserApi.class, (apiImpl, url) -> apiImpl.getLists(url, body));
    }

    public Call<List<InvalidatedData>> getInvalidated() {
        Map<String, Object> body = this.userAuthenticationMap();
        return build(UserApi.class, (apiImpl, url) -> apiImpl.getInvalidated(url, body));
    }

    public Call<ClientExternalUser> getExternalUser(String externalUuid) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return build(ExternalUserApi.class, (apiImpl, url) -> apiImpl.getExternalUser(url, body));
    }

    public Call<List<ClientExternalUser>> getExternalUser(Collection<String> externalUuid) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return build(ExternalUserApi.class, (apiImpl, url) -> apiImpl.getExternalUsers(url, body));
    }

    public Call<ClientExternalUser> addExternalUser(AddClientExternalUser externalUser) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUser", externalUser);
        return build(ExternalUserApi.class, (apiImpl, url) -> apiImpl.addExternalUser(url, body));
    }

    public Call<Boolean> deleteExternalUser(String externalUuid) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return build(ExternalUserApi.class, (apiImpl, url) -> apiImpl.deleteExternalUser(url, body));
    }

    public Call<ClientListQuery> getList(int listId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        return build(ListApi.class, (apiImpl, url) -> apiImpl.getList(url, body));
    }

    public Call<ClientMultiListQuery> getLists(Collection<Integer> listIds) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listIds);
        return build(ListApi.class, (apiImpl, url) -> apiImpl.getLists(url, body));
    }

    public Call<ClientMediaList> addList(ClientMediaList mediaList) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("list", mediaList);
        return build(ListApi.class, (apiImpl, url) -> apiImpl.addList(url, body));
    }

    public Call<Boolean> deleteList(int listId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        return build(ListApi.class, (apiImpl, url) -> apiImpl.deleteList(url, body));
    }

    public Call<Boolean> updateList(ClientMediaList mediaList) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("list", mediaList);
        return build(ListApi.class, (apiImpl, url) -> apiImpl.updateList(url, body));
    }

    public Call<List<ClientMedium>> getListMedia(Collection<Integer> loadedMedia, int listId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("media", loadedMedia);
        body.put("listId", listId);
        return build(ListMediaApi.class, (apiImpl, url) -> apiImpl.getListMedia(url, body));
    }

    public Call<Boolean> addListMedia(int listId, int mediumId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return build(ListMediaApi.class, (apiImpl, url) -> apiImpl.addListMedia(url, body));
    }

    public Call<Boolean> deleteListMedia(int listId, int mediumId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return build(ListMediaApi.class, (apiImpl, url) -> apiImpl.deleteListMedia(url, body));
    }

    public Call<Boolean> updateListMedia(int oldListId, int newListId, int mediumId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("oldListId", oldListId);
        body.put("newListId", newListId);
        body.put("mediumId", mediumId);
        return build(ListMediaApi.class, (apiImpl, url) -> apiImpl.updateListMedia(url, body));
    }

    public Call<List<ClientMedium>> getMedia(Collection<Integer> mediumIds) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumIds);
        return build(MediumApi.class, (apiImpl, url) -> apiImpl.getMedia(url, body));
    }

    public Call<ClientMedium> addMedia(ClientMedium clientMedium) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("medium", clientMedium);
        return build(MediumApi.class, (apiImpl, url) -> apiImpl.addMedia(url, body));
    }

    public Call<Boolean> updateMedia(ClientMedium medium) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("medium", medium);
        return build(MediumApi.class, (apiImpl, url) -> apiImpl.updateMedia(url, body));
    }

    public Call<List<ClientPart>> getParts(int mediumId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        return build(PartApi.class, (apiImpl, url) -> apiImpl.getPart(url, body));
    }

    public Call<List<ClientPart>> getParts(Collection<Integer> partIds) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partIds);
        return build(PartApi.class, (apiImpl, url) -> apiImpl.getPart(url, body));
    }

    public Call<ClientPart> addPart(ClientPart part) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", part);
        return build(PartApi.class, (apiImpl, url) -> apiImpl.addPart(url, body));
    }

    public Call<Boolean> deletePart(int partId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partId);
        return build(PartApi.class, (apiImpl, url) -> apiImpl.deletePart(url, body));
    }

    public Call<Boolean> updatePart(ClientPart part) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", part);
        return build(PartApi.class, (apiImpl, url) -> apiImpl.updatePart(url, body));
    }

    public Call<ClientEpisode> getEpisode(int episodeId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return build(EpisodeApi.class, (apiImpl, url) -> apiImpl.getEpisode(url, body));
    }

    public Call<List<ClientEpisode>> getEpisodes(Collection<Integer> episodeIds) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeIds);
        return build(EpisodeApi.class, (apiImpl, url) -> apiImpl.getEpisodes(url, body));
    }

    public Call<ClientEpisode> addEpisode(int partId, ClientEpisode episode) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partId);
        body.put("episode", episode);
        return build(EpisodeApi.class, (apiImpl, url) -> apiImpl.addEpisode(url, body));
    }

    public Call<Boolean> deleteEpisode(int episodeId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return build(EpisodeApi.class, (apiImpl, url) -> apiImpl.deleteEpisode(url, body));
    }

    public Call<Boolean> updateEpisode(ClientEpisode episode) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episode", episode);
        return build(EpisodeApi.class, (apiImpl, url) -> apiImpl.updateEpisode(url, body));
    }

    public Call<Float> getProgress(int episodeId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return build(ProgressApi.class, (apiImpl, url) -> apiImpl.getProgress(url, body));
    }

    public Call<Boolean> addProgress(int episodeId, float progress) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        body.put("progress", progress);
        return build(ProgressApi.class, (apiImpl, url) -> apiImpl.addProgress(url, body));
    }

    public Call<Boolean> deleteProgress(int episodeId) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return build(ProgressApi.class, (apiImpl, url) -> apiImpl.deleteProgress(url, body));
    }

    public Call<Boolean> updateProgress(int episodeId, float progress) {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        body.put("progress", progress);
        return build(ProgressApi.class, (apiImpl, url) -> apiImpl.updateProgress(url, body));
    }

    private <T, R> R build(Class<T> api, BuildCall<T, R> buildCall) {
        Retrofit retrofit = Client.retrofitMap.get(api);
        String path = Client.fullClassPathMap.get(api);

        if (path == null) {
            throw new IllegalArgumentException("Unknown api class: " + api.getCanonicalName());
        }

        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(DateTime.class, new GsonAdapter.DateTimeAdapter())
                    .create();
            retrofit = new Retrofit.Builder()
                    // todo: change url to online server url
                    .baseUrl("http://192.168.1.5:3000/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            Client.retrofitMap.put(api, retrofit);
        }


        T apiImpl = retrofit.create(api);
        return buildCall.call(apiImpl, path);
    }

    private interface BuildCall<T, R> {
        R call(T apiImpl, String url);
    }
}
// todo check if server is online or not