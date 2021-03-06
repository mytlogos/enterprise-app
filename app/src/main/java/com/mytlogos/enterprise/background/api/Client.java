package com.mytlogos.enterprise.background.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mytlogos.enterprise.background.api.model.AddClientExternalUser;
import com.mytlogos.enterprise.background.api.model.Authentication;
import com.mytlogos.enterprise.background.api.model.ClientChangedEntities;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait;
import com.mytlogos.enterprise.background.api.model.ClientMinList;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientSimpleEpisode;
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientToc;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.api.model.InvalidatedData;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    private static final Map<Class<?>, Retrofit> retrofitMap = new HashMap<>();
    private static final Map<Class<?>, String> fullClassPathMap = new HashMap<>();

    static {
        buildPathMap();
    }

    private Authentication authentication;
    private Server server;
    private String lastNetworkSSID;
    private final NetworkIdentificator identificator;
    private DateTime disconnectedSince;
    private final Set<DisconnectedListener> disconnectedListeners = Collections.synchronizedSet(new HashSet<>());

    @FunctionalInterface
    public interface DisconnectedListener {
        void handle(DateTime timeDisconnected);
    }


    public Client(NetworkIdentificator identificator) {
        this.identificator = identificator;
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

            assert apiClass != null;
            fullClassPathMap.put(apiClass, builder.toString());
        }
    }

    public void setAuthentication(String uuid, String session) {
        if (uuid == null || uuid.isEmpty() || session == null || session.isEmpty()) {
            return;
        }
        this.authentication = new Authentication(uuid, session);
    }

    public boolean isAuthenticated() {
        return this.authentication != null;
    }

    public void clearAuthentication() {
        this.authentication = null;
    }

    public void addDisconnectedListener(DisconnectedListener listener) {
        this.disconnectedListeners.add(listener);
    }

    public void removeDisconnectedListener(DisconnectedListener listener) {
        this.disconnectedListeners.remove(listener);
    }

    public Response<ClientSimpleUser> checkLogin() throws IOException {
        return this.query(BasicApi.class, BasicApi::checkLogin);
    }

    /**
     * Login as User.
     * API: POST /api/login
     */
    public Response<ClientUser> login(String mailName, String password) throws IOException {
        return this.query(BasicApi.class, (apiImpl, url) ->
                apiImpl.login(
                        url,
                        this.userVerificationMap(mailName, password)
                ));
    }

    /**
     * Register as User.
     * API: POST /api/register
     */
    public Response<ClientUser> register(String mailName, String password) throws IOException {
        return this.query(BasicApi.class, (apiImpl, url) ->
                apiImpl.register(
                        url,
                        this.userVerificationMap(mailName, password)
                ));
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

    /**
     * Get current User.
     * API: GET /api/user
     */
    public Response<ClientUser> getUser() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getUser(url, body));
    }

    /**
     * Update current User.
     * API: PUT /api/user
     */
    public Response<Boolean> updateUser(ClientUpdateUser updateUser) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("user", updateUser);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.updateUser(url, body));
    }

    /**
     * Logout current User.
     * API: POST /api/user/logout
     */
    public Response<Boolean> logout() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.logout(url, body));
    }

    /**
     * Get Lists of current User.
     * API: GET /api/user/lists
     */
    public Response<List<ClientMediaList>> getLists() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getLists(url, body));
    }

    /**
     * Request adding a toc.
     * API: POST /api/user/toc
     */
    public Response<Boolean> addToc(int mediumId, String link) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        body.put("toc", link);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.addToc(url, body));
    }

    /**
     * Get TOCs of multiple Media.
     * API: GET /api/user/toc
     */
    public Response<List<ClientToc>> getMediumTocs(Collection<Integer> mediumIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumIds);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getToc(url, body));
    }

    /**
     * Delete a Toc.
     * API: DELETE /api/user/toc
     */
    public Response<Boolean> removeToc(int mediumId, String link) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        body.put("link", link);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.removeToc(url, body));
    }

    /**
     * Get Stats about current User Data.
     * API: GET /api/user/stats
     */
    public Response<ClientStat> getStats() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getStats(url, body));
    }

    /**
     * Get New Data since lastSync.
     * API: GET /api/user/new
     */
    public Response<ClientChangedEntities> getNew(DateTime lastSync) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("date", lastSync);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getNew(url, body));
    }

    /**
     * Download Episodes.
     * API: GET /api/user/download
     */
    public Response<List<ClientDownloadedEpisode>> downloadEpisodes(Collection<Integer> episodeIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episode", episodeIds);
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.downloadEpisodes(url, body));
    }

    /**
     * Get News by Date.
     * API: GET /api/user/news
     */
    public Response<List<ClientNews>> getNews(DateTime from, DateTime to) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        if (from != null) {
            body.put("from", from);
        }
        if (to != null) {
            body.put("to", to);
        }
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getNews(url, body));
    }

    /**
     * Get News by Id.
     * API: GET /api/user/news
     */
    public Response<List<ClientNews>> getNews(Collection<Integer> newsIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        if (newsIds != null) {
            body.put("newsId", newsIds);
        }
        return this.query(UserApi.class, (apiImpl, url) -> apiImpl.getNews(url, body));
    }

    /**
     * Web API was removed.
     * Remove this method and all connected code.
     */
    public Response<List<InvalidatedData>> getInvalidated() throws IOException {
        throw new IllegalAccessError("API was removed");
    }

    /**
     * Get a single ExternalUser.
     * API: GET /api/user/externalUser
     */
    public Response<ClientExternalUser> getExternalUser(String externalUuid) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return this.query(ExternalUserApi.class, (apiImpl, url) -> apiImpl.getExternalUser(url, body));
    }

    /**
     * Get multiple ExternalUser.
     * API: GET /api/user/externalUser
     */
    public Response<List<ClientExternalUser>> getExternalUser(Collection<String> externalUuid) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return this.query(ExternalUserApi.class, (apiImpl, url) -> apiImpl.getExternalUsers(url, body));
    }

    /**
     * Add an ExternalUser.
     * API: POST /api/user/externalUser
     */
    public Response<ClientExternalUser> addExternalUser(AddClientExternalUser externalUser) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUser", externalUser);
        return this.query(ExternalUserApi.class, (apiImpl, url) -> apiImpl.addExternalUser(url, body));
    }

    /**
     * Delete an ExternalUser.
     * API: DELETE /api/user/externalUser
     */
    public Response<Boolean> deleteExternalUser(String externalUuid) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("externalUuid", externalUuid);
        return this.query(ExternalUserApi.class, (apiImpl, url) -> apiImpl.deleteExternalUser(url, body));
    }

    /**
     * Get List and its Media.
     * API: GET /api/user/new
     */
    public Response<ClientListQuery> getListMedia(Collection<Integer> loadedMedia, int listId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("media", loadedMedia);
        body.put("listId", listId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.getListMedia(url, body));
    }

    /**
     * Add Medium as Item to List.
     * API: POST /api/user/list/medium
     */
    public Response<Boolean> addListMedia(int listId, int mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.addListMedia(url, body));
    }

    /**
     * Add multiple Media as Items to List.
     * API: POST /api/user/list/medium
     */
    public Response<Boolean> addListMedia(int listId, Collection<Integer> mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.addListMedia(url, body));
    }

    /**
     * Move Medium as Item from one List to another.
     * API: PUT /api/user/list/medium
     */
    public Response<Boolean> updateListMedia(int oldListId, int newListId, int mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("oldListId", oldListId);
        body.put("newListId", newListId);
        body.put("mediumId", mediumId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.updateListMedia(url, body));
    }

    /**
     * Remove Medium as Item from List.
     * API: DELETE /api/user/list/medium
     */
    public Response<Boolean> deleteListMedia(int listId, int mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.deleteListMedia(url, body));
    }

    /**
     * Remove multiple Media as Items from List.
     * API: DELETE /api/user/list/medium
     */
    public Response<Boolean> deleteListMedia(int listId, Collection<Integer> mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        body.put("mediumId", mediumId);
        return this.query(ListMediaApi.class, (apiImpl, url) -> apiImpl.deleteListMedia(url, body));
    }

    /**
     * Get List and its Media Items.
     * API: GET /api/user/list
     */
    public Response<ClientListQuery> getList(int listId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        return this.query(ListApi.class, (apiImpl, url) -> apiImpl.getList(url, body));
    }

    /**
     * Get multiple Lists and their Media Items.
     * API: GET /api/user/list
     */
    public Response<ClientMultiListQuery> getLists(Collection<Integer> listIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listIds);
        return this.query(ListApi.class, (apiImpl, url) -> apiImpl.getLists(url, body));
    }

    /**
     * Create List.
     * API: POST /api/user/list
     */
    public Response<ClientMediaList> addList(ClientMinList mediaList) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("list", mediaList);
        return this.query(ListApi.class, (apiImpl, url) -> apiImpl.addList(url, body));
    }

    /**
     * Create List. Currently alias for addList.
     * API: PUT /api/user/list
     */
    public Response<Boolean> updateList(ClientMinList mediaList) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("list", mediaList);
        return this.query(ListApi.class, (apiImpl, url) -> apiImpl.updateList(url, body));
    }

    /**
     * Delete a List and its Item Mappings.
     * API: DELETE /api/user/list
     */
    public Response<Boolean> deleteList(int listId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("listId", listId);
        return this.query(ListApi.class, (apiImpl, url) -> apiImpl.deleteList(url, body));
    }

    /**
     * Get MediumInWaits.
     * API: GET /api/user/medium/unused
     */
    public Response<List<ClientMediumInWait>> getMediumInWait() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.getMediumInWait(url, body));
    }

    /**
     * Get all Medium Ids.
     * API: GET /api/user/medium
     */
    public Response<List<Integer>> getAllMedia() throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.getAllMedia(url, body));
    }

    /**
     * Consume Tocs from MediumInWaits to existing Medium.
     * API: PUT /api/user/medium/unused
     */
    public Response<Boolean> consumeMediumInWait(int mediumId, Collection<ClientMediumInWait> others) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        body.put("tocsMedia", others);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.consumeMediumInWait(url, body));
    }

    /**
     * Create Medium from MediumInWaits and add it to List.
     * API: POST /api/user/medium/create
     */
    public Response<ClientMedium> createFromMediumInWait(ClientMediumInWait main, Collection<ClientMediumInWait> others, Integer listId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("createMedium", main);
        body.put("tocsMedia", others);
        body.put("listId", listId);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.createFromMediumInWait(url, body));
    }

    /**
     * Get multiple Media by Id.
     * API: GET /api/user/medium
     */
    public Response<List<ClientMedium>> getMedia(Collection<Integer> mediumIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumIds);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.getMedia(url, body));
    }

    /**
     * Get single Medium by Id.
     * API: GET /api/user/medium
     */
    public Response<ClientMedium> getMedium(int mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.getMedium(url, body));
    }

    /**
     * Create Medium.
     * API: POST /api/user/medium
     */
    public Response<ClientSimpleMedium> addMedia(ClientSimpleMedium clientMedium) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("medium", clientMedium);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.addMedia(url, body));
    }

    /**
     * Update Medium.
     * TODO: change parameter to UpdateMedium?
     * API: PUT /api/user/medium
     */
    public Response<Boolean> updateMedia(ClientMedium medium) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("medium", medium);
        return this.query(MediumApi.class, (apiImpl, url) -> apiImpl.updateMedia(url, body));
    }

    /**
     * Get Progress.
     * API: GET /api/user/medium/progress
     */
    public Response<Float> getProgress(int episodeId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return this.query(ProgressApi.class, (apiImpl, url) -> apiImpl.getProgress(url, body));
    }

    /**
     * Update Progress of episodes.
     * API: POST /api/user/medium/progress
     */
    public Response<Boolean> addProgress(Collection<Integer> episodeId, float progress) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        body.put("progress", progress);
        return this.query(ProgressApi.class, (apiImpl, url) -> apiImpl.addProgress(url, body));
    }

    /**
     * Update Progress of episodes. Alias of addProgress.
     * API: PUT /api/user/medium/progress
     */
    public Response<Boolean> updateProgress(int episodeId, float progress) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        body.put("progress", progress);
        return this.query(ProgressApi.class, (apiImpl, url) -> apiImpl.updateProgress(url, body));
    }

    /**
     * Delete Progress.
     * API: DELETE /api/user/medium/progress
     */
    public Response<Boolean> deleteProgress(int episodeId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return this.query(ProgressApi.class, (apiImpl, url) -> apiImpl.deleteProgress(url, body));
    }

    /**
     * Get Parts by mediumId.
     * API: GET /api/user/medium/part
     */
    public Response<List<ClientPart>> getParts(int mediumId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("mediumId", mediumId);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.getPart(url, body));
    }

    /**
     * Get Parts by partId.
     * API: GET /api/user/medium/part
     */
    public Response<List<ClientPart>> getParts(Collection<Integer> partIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partIds);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.getPart(url, body));
    }

    /**
     * Create Part.
     * TODO: change ClientPart to AddPart
     * API: POST /api/user/medium/part
     */
    public Response<ClientPart> addPart(ClientPart part) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", part);
        body.put("mediumId", part.getMediumId());
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.addPart(url, body));
    }

    /**
     * Update Part.
     * API: PUT /api/user/medium/part
     */
    public Response<Boolean> updatePart(ClientPart part) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", part);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.updatePart(url, body));
    }

    /**
     * Delete Part.
     * API: DELETE /api/user/medium/part
     */
    public Response<Boolean> deletePart(int partId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partId);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.deletePart(url, body));
    }

    /**
     * Get Part Ids and their EpisodeIds.
     * API: GET /api/user/medium/part/items
     */
    public Response<Map<String, List<Integer>>> getPartEpisodes(Collection<Integer> partIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", partIds);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.getPartItems(url, body));
    }

    /**
     * Get Part Ids and their Releases.
     * API: GET /api/user/medium/part/releases
     */
    public Response<Map<String, List<ClientSimpleRelease>>> getPartReleases(Collection<Integer> partIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("part", partIds);
        return this.query(PartApi.class, (apiImpl, url) -> apiImpl.getPartReleases(url, body));
    }

    /**
     * Get Episode by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    public Response<ClientEpisode> getEpisode(int episodeId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return this.query(EpisodeApi.class, (apiImpl, url) -> apiImpl.getEpisode(url, body));
    }

    /**
     * Get Episodes by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    public Response<List<ClientEpisode>> getEpisodes(Collection<Integer> episodeIds) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeIds);
        return this.query(EpisodeApi.class, (apiImpl, url) -> apiImpl.getEpisodes(url, body));
    }

    /**
     * Add Episode.
     * API: POST /api/user/medium/part/episode
     */
    public Response<ClientEpisode> addEpisode(int partId, ClientEpisode episode) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("partId", partId);
        body.put("episode", episode);
        return this.query(EpisodeApi.class, (apiImpl, url) -> apiImpl.addEpisode(url, body));
    }

    /**
     * Update Episode.
     * API: PUT /api/user/medium/part/episode
     */
    public Response<Boolean> updateEpisode(ClientSimpleEpisode episode) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episode", Collections.singletonList(episode));
        return this.query(EpisodeApi.class, (apiImpl, url) -> apiImpl.updateEpisode(url, body));
    }

    /**
     * Delete Episode by Id.
     * API: DELETE /api/user/medium/part/episode
     */
    public Response<Boolean> deleteEpisode(int episodeId) throws IOException {
        Map<String, Object> body = this.userAuthenticationMap();
        body.put("episodeId", episodeId);
        return this.query(EpisodeApi.class, (apiImpl, url) -> apiImpl.deleteEpisode(url, body));
    }

    private void setConnected() {
        System.out.println("connected");
        if (this.disconnectedSince != null) {
            for (DisconnectedListener listener : this.disconnectedListeners) {
                listener.handle(this.disconnectedSince);
            }
            this.disconnectedSince = null;
        }
    }

    private void setDisconnected() {
        if (this.disconnectedSince == null) {
            System.out.println("disconnected");
            this.disconnectedSince = DateTime.now();
        }
    }

    private <T, R> Response<R> query(Class<T> api, BuildCall<T, Call<R>> buildCall) throws IOException {
        try {
            Response<R> result = build(api, buildCall).execute();
            this.setConnected();
            return result;
        } catch (NotConnectedException e) {
            this.setDisconnected();
            throw new NotConnectedException(e);
        }
    }

    private <T, R> Call<R> build(Class<T> api, BuildCall<T, Call<R>> buildCall) throws IOException {
        Retrofit retrofit = Client.retrofitMap.get(api);
        String path = Client.fullClassPathMap.get(api);

        if (path == null) {
            throw new IllegalArgumentException("Unknown api class: " + api.getCanonicalName());
        }

        this.server = this.getServer();

        // FIXME: 29.07.2019 sometimes does not find server even though it is online
        if (this.server == null) {
            throw new NotConnectedException("No Server in reach");
        }

        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(DateTime.class, new GsonAdapter.DateTimeAdapter())
                    .create();
            OkHttpClient client = new OkHttpClient
                    .Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(this.server.getAddress())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            Client.retrofitMap.put(api, retrofit);
        }


        T apiImpl = retrofit.create(api);
        return buildCall.call(apiImpl, path);
    }

    public boolean isOnline() {
        try {
            this.server = getServer();

            if (this.server != null) {
                this.setConnected();
                return true;
            }
        } catch (NotConnectedException ignored) {
        }
        this.setDisconnected();
        return false;
    }

    private synchronized Server getServer() throws NotConnectedException {
        String ssid = this.identificator.getSSID();

        if (ssid.isEmpty()) {
            throw new NotConnectedException("Not connected to any network");
        }
        ServerDiscovery discovery = new ServerDiscovery();

        if (ssid.equals(this.lastNetworkSSID)) {
            if (this.server == null) {
                return discovery.discover(this.identificator.getBroadcastAddress());
            } else if (this.server.isReachable()) {
                return this.server;
            }
        } else {
            this.lastNetworkSSID = ssid;
        }
        return discovery.discover(this.identificator.getBroadcastAddress());
    }

    private interface BuildCall<T, R> {
        R call(T apiImpl, String url);
    }
}