package com.mytlogos.enterprise.background.api

import com.google.gson.GsonBuilder
import com.mytlogos.enterprise.background.api.GsonAdapter.DateTimeAdapter
import com.mytlogos.enterprise.background.api.model.*
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class Client(private val identificator: NetworkIdentificator) {
    companion object {
        private val retrofitMap: MutableMap<Class<*>, Retrofit?> = HashMap()
        private val fullClassPathMap: MutableMap<Class<*>?, String> = HashMap()
        private fun buildPathMap() {
            val parentClassMap: MutableMap<Class<*>, Class<*>> = HashMap()
            val classPathMap: MutableMap<Class<*>, String> = HashMap()

            // set up the path pieces between each api
            classPathMap[BasicApi::class.java] = "api"
            classPathMap[UserApi::class.java] = "user"
            classPathMap[ExternalUserApi::class.java] = "externalUser"
            classPathMap[ListApi::class.java] = "list"
            classPathMap[ListMediaApi::class.java] = "medium"
            classPathMap[MediumApi::class.java] = "medium"
            classPathMap[PartApi::class.java] = "part"
            classPathMap[EpisodeApi::class.java] = "episode"
            classPathMap[ProgressApi::class.java] = "progress"
            parentClassMap[UserApi::class.java] = BasicApi::class.java
            parentClassMap[ExternalUserApi::class.java] = UserApi::class.java
            parentClassMap[ListApi::class.java] = UserApi::class.java
            parentClassMap[ListMediaApi::class.java] = ListApi::class.java
            parentClassMap[MediumApi::class.java] = UserApi::class.java
            parentClassMap[PartApi::class.java] = MediumApi::class.java
            parentClassMap[EpisodeApi::class.java] = PartApi::class.java
            parentClassMap[ProgressApi::class.java] = MediumApi::class.java

            for (apiClass in classPathMap.keys) {
                val builder = StringBuilder()
                var parent: Class<*>? = apiClass

                while (parent != null) {
                    val pathPiece = classPathMap[parent]
                    if (parent != apiClass) {
                        builder.insert(0, "/")
                    }
                    if (pathPiece == null) {
                        val canonicalName = apiClass.canonicalName
                        throw IllegalStateException("Api has no path piece: $canonicalName")
                    }
                    builder.insert(0, pathPiece)
                    parent = parentClassMap[parent]
                }
                fullClassPathMap[apiClass] = builder.toString()
            }
        }

        init {
            buildPathMap()
        }
    }

    private var authentication: Authentication? = null
    private var server: Server? = null
    private var lastNetworkSSID: String? = null
    private var disconnectedSince: DateTime? = null
    private val disconnectedListeners = Collections.synchronizedSet(HashSet<DisconnectedListener>())

    fun interface DisconnectedListener {
        fun handle(timeDisconnected: DateTime?)
    }

    fun setAuthentication(uuid: String?, session: String?) {
        if (uuid == null || uuid.isEmpty() || session == null || session.isEmpty()) {
            return
        }
        authentication = Authentication(uuid, session)
    }

    val isClientAuthenticated: Boolean
        get() = authentication != null

    fun clearAuthentication() {
        authentication = null
    }

    fun addDisconnectedListener(listener: DisconnectedListener) {
        disconnectedListeners.add(listener)
    }

    fun removeDisconnectedListener(listener: DisconnectedListener) {
        disconnectedListeners.remove(listener)
    }

    @Throws(IOException::class)
    fun checkLogin(): Response<ClientSimpleUser> {
        return query(BasicApi::class.java) { apiImpl: BasicApi, url: String -> apiImpl.checkLogin(url) }
    }

    /**
     * Login as User.
     * API: POST /api/login
     */
    @Throws(IOException::class)
    fun login(mailName: String, password: String): Response<ClientUser> {
        return query(BasicApi::class.java) { apiImpl: BasicApi, url: String ->
            apiImpl.login(
                    url,
                    userVerificationMap(mailName, password)
            )
        }
    }

    /**
     * Register as User.
     * API: POST /api/register
     */
    @Throws(IOException::class)
    fun register(mailName: String, password: String): Response<ClientUser> {
        return query(BasicApi::class.java) { apiImpl: BasicApi, url: String ->
            apiImpl.register(
                    url,
                    userVerificationMap(mailName, password)
            )
        }
    }

    private fun userAuthenticationMap(): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = HashMap()
        checkNotNull(authentication) { "user not authenticated" }
        body["uuid"] = authentication!!.getUuid()
        body["session"] = authentication!!.getSession()
        return body
    }

    private fun userVerificationMap(mailName: String, password: String): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = HashMap()
        body["userName"] = mailName
        body["pw"] = password
        return body
    }

    /**
     * Get current User.
     * API: GET /api/user
     */
    @get:Throws(IOException::class)
    val user: Response<ClientUser>
        get() {
            val body: MutableMap<String, Any?> = userAuthenticationMap()
            return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getUser(url, body) }
        }

    /**
     * Update current User.
     * API: PUT /api/user
     */
    @Throws(IOException::class)
    fun updateUser(updateUser: ClientUpdateUser?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["user"] = updateUser
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.updateUser(url, body) }
    }

    /**
     * Logout current User.
     * API: POST /api/user/logout
     */
    @Throws(IOException::class)
    fun logout(): Response<Boolean> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.logout(url, body) }
    }

    /**
     * Get Lists of current User.
     * API: GET /api/user/lists
     */
    @get:Throws(IOException::class)
    val lists: Response<List<ClientMediaList>>
        get() {
            val body: MutableMap<String, Any?> = userAuthenticationMap()
            return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getLists(url, body) }
        }

    /**
     * Request adding a toc.
     * API: POST /api/user/toc
     */
    @Throws(IOException::class)
    fun addToc(mediumId: Int, link: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["toc"] = link
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.addToc(url, body) }
    }

    /**
     * Get TOCs of multiple Media.
     * API: GET /api/user/toc
     */
    @Throws(IOException::class)
    fun getMediumTocs(mediumIds: Collection<Int?>?): Response<List<ClientToc>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumIds
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getToc(url, body) }
    }

    /**
     * Delete a Toc.
     * API: DELETE /api/user/toc
     */
    @Throws(IOException::class)
    fun removeToc(mediumId: Int, link: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["link"] = link
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.removeToc(url, body) }
    }

    /**
     * Get Stats about current User Data.
     * API: GET /api/user/stats
     */
    @get:Throws(IOException::class)
    val stats: Response<ClientStat>
        get() {
            val body: MutableMap<String, Any?> = userAuthenticationMap()
            return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getStats(url, body) }
        }

    /**
     * Get New Data since lastSync.
     * API: GET /api/user/new
     */
    @Throws(IOException::class)
    fun getNew(lastSync: DateTime?): Response<ClientChangedEntities> {
        val body = userAuthenticationMap()
        body["date"] = lastSync
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNew(url, body) }
    }

    /**
     * Download Episodes.
     * API: GET /api/user/download
     */
    @Throws(IOException::class)
    fun downloadEpisodes(episodeIds: Collection<Int?>?): Response<List<ClientDownloadedEpisode>> {
        val body = userAuthenticationMap()
        body["episode"] = episodeIds
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.downloadEpisodes(url, body) }
    }

    /**
     * Get News by Date.
     * API: GET /api/user/news
     */
    @Throws(IOException::class)
    fun getNews(from: DateTime?, to: DateTime?): Response<List<ClientNews>> {
        val body = userAuthenticationMap()
        if (from != null) {
            body["from"] = from
        }
        if (to != null) {
            body["to"] = to
        }
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNews(url, body) }
    }

    /**
     * Get News by Id.
     * API: GET /api/user/news
     */
    @Throws(IOException::class)
    fun getNews(newsIds: Collection<Int?>?): Response<List<ClientNews>> {
        val body = userAuthenticationMap()
        if (newsIds != null) {
            body["newsId"] = newsIds
        }
        return query(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNews(url, body) }
    }

    /**
     * Web API was removed.
     * Remove this method and all connected code.
     */
    @get:Throws(IOException::class)
    val invalidated: Response<List<InvalidatedData>>
        get() {
            throw IllegalAccessError("API was removed")
        }

    /**
     * Get a single ExternalUser.
     * API: GET /api/user/externalUser
     */
    @Throws(IOException::class)
    fun getExternalUser(externalUuid: String?): Response<ClientExternalUser> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return query(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.getExternalUser(url, body) }
    }

    /**
     * Get multiple ExternalUser.
     * API: GET /api/user/externalUser
     */
    @Throws(IOException::class)
    fun getExternalUser(externalUuid: Collection<String?>?): Response<List<ClientExternalUser>> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return query(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.getExternalUsers(url, body) }
    }

    /**
     * Add an ExternalUser.
     * API: POST /api/user/externalUser
     */
    @Throws(IOException::class)
    fun addExternalUser(externalUser: AddClientExternalUser?): Response<ClientExternalUser> {
        val body = userAuthenticationMap()
        body["externalUser"] = externalUser
        return query(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.addExternalUser(url, body) }
    }

    /**
     * Delete an ExternalUser.
     * API: DELETE /api/user/externalUser
     */
    @Throws(IOException::class)
    fun deleteExternalUser(externalUuid: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return query(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.deleteExternalUser(url, body) }
    }

    /**
     * Get List and its Media.
     * API: GET /api/user/new
     */
    @Throws(IOException::class)
    fun getListMedia(loadedMedia: Collection<Int?>?, listId: Int): Response<ClientListQuery> {
        val body = userAuthenticationMap()
        body["media"] = loadedMedia
        body["listId"] = listId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.getListMedia(url, body) }
    }

    /**
     * Add Medium as Item to List.
     * API: POST /api/user/list/medium
     */
    @Throws(IOException::class)
    fun addListMedia(listId: Int, mediumId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.addListMedia(url, body) }
    }

    /**
     * Add multiple Media as Items to List.
     * API: POST /api/user/list/medium
     */
    @Throws(IOException::class)
    fun addListMedia(listId: Int, mediumId: Collection<Int?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.addListMedia(url, body) }
    }

    /**
     * Move Medium as Item from one List to another.
     * API: PUT /api/user/list/medium
     */
    @Throws(IOException::class)
    fun updateListMedia(oldListId: Int, newListId: Int, mediumId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["oldListId"] = oldListId
        body["newListId"] = newListId
        body["mediumId"] = mediumId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.updateListMedia(url, body) }
    }

    /**
     * Remove Medium as Item from List.
     * API: DELETE /api/user/list/medium
     */
    @Throws(IOException::class)
    fun deleteListMedia(listId: Int, mediumId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.deleteListMedia(url, body) }
    }

    /**
     * Remove multiple Media as Items from List.
     * API: DELETE /api/user/list/medium
     */
    @Throws(IOException::class)
    fun deleteListMedia(listId: Int, mediumId: Collection<Int?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return query(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.deleteListMedia(url, body) }
    }

    /**
     * Get List and its Media Items.
     * API: GET /api/user/list
     */
    @Throws(IOException::class)
    fun getList(listId: Int): Response<ClientListQuery> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        return query(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.getList(url, body) }
    }

    /**
     * Get multiple Lists and their Media Items.
     * API: GET /api/user/list
     */
    @Throws(IOException::class)
    fun getLists(listIds: Collection<Int?>?): Response<ClientMultiListQuery> {
        val body = userAuthenticationMap()
        body["listId"] = listIds
        return query(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.getLists(url, body) }
    }

    /**
     * Create List.
     * API: POST /api/user/list
     */
    @Throws(IOException::class)
    fun addList(mediaList: ClientMinList?): Response<ClientMediaList> {
        val body = userAuthenticationMap()
        body["list"] = mediaList
        return query(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.addList(url, body) }
    }

    /**
     * Create List. Currently alias for addList.
     * API: PUT /api/user/list
     */
    @Throws(IOException::class)
    fun updateList(mediaList: ClientMinList?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["list"] = mediaList
        return query(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.updateList(url, body) }
    }

    /**
     * Delete a List and its Item Mappings.
     * API: DELETE /api/user/list
     */
    @Throws(IOException::class)
    fun deleteList(listId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        return query(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.deleteList(url, body) }
    }

    /**
     * Get MediumInWaits.
     * API: GET /api/user/medium/unused
     */
    @get:Throws(IOException::class)
    val mediumInWait: Response<List<ClientMediumInWait>>
        get() {
            val body: MutableMap<String, Any?> = userAuthenticationMap()
            return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getMediumInWait(url, body) }
        }

    /**
     * Get all Medium Ids.
     * API: GET /api/user/medium
     */
    @get:Throws(IOException::class)
    val allMedia: Response<List<Int>>
        get() {
            val body: MutableMap<String, Any?> = userAuthenticationMap()
            return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getAllMedia(url, body) }
        }

    /**
     * Consume Tocs from MediumInWaits to existing Medium.
     * API: PUT /api/user/medium/unused
     */
    @Throws(IOException::class)
    fun consumeMediumInWait(mediumId: Int, others: Collection<ClientMediumInWait?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["tocsMedia"] = others
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.consumeMediumInWait(url, body) }
    }

    /**
     * Create Medium from MediumInWaits and add it to List.
     * API: POST /api/user/medium/create
     */
    @Throws(IOException::class)
    fun createFromMediumInWait(main: ClientMediumInWait?, others: Collection<ClientMediumInWait?>?, listId: Int?): Response<ClientMedium> {
        val body = userAuthenticationMap()
        body["createMedium"] = main
        body["tocsMedia"] = others
        body["listId"] = listId
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.createFromMediumInWait(url, body) }
    }

    /**
     * Get multiple Media by Id.
     * API: GET /api/user/medium
     */
    @Throws(IOException::class)
    fun getMedia(mediumIds: Collection<Int?>?): Response<List<ClientMedium>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumIds
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getMedia(url, body) }
    }

    /**
     * Get single Medium by Id.
     * API: GET /api/user/medium
     */
    @Throws(IOException::class)
    fun getMedium(mediumId: Int): Response<ClientMedium> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getMedium(url, body) }
    }

    /**
     * Create Medium.
     * API: POST /api/user/medium
     */
    @Throws(IOException::class)
    fun addMedia(clientMedium: ClientSimpleMedium?): Response<ClientSimpleMedium> {
        val body = userAuthenticationMap()
        body["medium"] = clientMedium
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.addMedia(url, body) }
    }

    /**
     * Update Medium.
     * TODO: change parameter to UpdateMedium?
     * API: PUT /api/user/medium
     */
    @Throws(IOException::class)
    fun updateMedia(medium: ClientMedium?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["medium"] = medium
        return query(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.updateMedia(url, body) }
    }

    /**
     * Get Progress.
     * API: GET /api/user/medium/progress
     */
    @Throws(IOException::class)
    fun getProgress(episodeId: Int): Response<Float> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return query(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.getProgress(url, body) }
    }

    /**
     * Update Progress of episodes.
     * API: POST /api/user/medium/progress
     */
    @Throws(IOException::class)
    fun addProgress(episodeId: Collection<Int?>?, progress: Float): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        body["progress"] = progress
        return query(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.addProgress(url, body) }
    }

    /**
     * Update Progress of episodes. Alias of addProgress.
     * API: PUT /api/user/medium/progress
     */
    @Throws(IOException::class)
    fun updateProgress(episodeId: Int, progress: Float): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        body["progress"] = progress
        return query(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.updateProgress(url, body) }
    }

    /**
     * Delete Progress.
     * API: DELETE /api/user/medium/progress
     */
    @Throws(IOException::class)
    fun deleteProgress(episodeId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return query(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.deleteProgress(url, body) }
    }

    /**
     * Get Parts by mediumId.
     * API: GET /api/user/medium/part
     */
    @Throws(IOException::class)
    fun getParts(mediumId: Int): Response<List<ClientPart>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPart(url, body) }
    }

    /**
     * Get Parts by partId.
     * API: GET /api/user/medium/part
     */
    @Throws(IOException::class)
    fun getParts(partIds: Collection<Int?>?): Response<List<ClientPart>> {
        val body = userAuthenticationMap()
        body["partId"] = partIds
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPart(url, body) }
    }

    /**
     * Create Part.
     * TODO: change ClientPart to AddPart
     * API: POST /api/user/medium/part
     */
    @Throws(IOException::class)
    fun addPart(part: ClientPart): Response<ClientPart> {
        val body = userAuthenticationMap()
        body["part"] = part
        body["mediumId"] = part.mediumId
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.addPart(url, body) }
    }

    /**
     * Update Part.
     * API: PUT /api/user/medium/part
     */
    @Throws(IOException::class)
    fun updatePart(part: ClientPart?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["part"] = part
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.updatePart(url, body) }
    }

    /**
     * Delete Part.
     * API: DELETE /api/user/medium/part
     */
    @Throws(IOException::class)
    fun deletePart(partId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["partId"] = partId
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.deletePart(url, body) }
    }

    /**
     * Get Part Ids and their EpisodeIds.
     * API: GET /api/user/medium/part/items
     */
    @Throws(IOException::class)
    fun getPartEpisodes(partIds: Collection<Int?>?): Response<Map<String, List<Int>>> {
        val body = userAuthenticationMap()
        body["part"] = partIds
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPartItems(url, body) }
    }

    /**
     * Get Part Ids and their Releases.
     * API: GET /api/user/medium/part/releases
     */
    @Throws(IOException::class)
    fun getPartReleases(partIds: Collection<Int?>?): Response<Map<String, List<ClientSimpleRelease>>> {
        val body = userAuthenticationMap()
        body["part"] = partIds
        return query(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPartReleases(url, body) }
    }

    /**
     * Get Episode by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    fun getEpisode(episodeId: Int): Response<ClientEpisode> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return query(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.getEpisode(url, body) }
    }

    /**
     * Get Episodes by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    fun getEpisodes(episodeIds: Collection<Int?>?): Response<List<ClientEpisode>> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeIds
        return query(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.getEpisodes(url, body) }
    }

    /**
     * Add Episode.
     * API: POST /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    fun addEpisode(partId: Int, episode: ClientEpisode?): Response<ClientEpisode> {
        val body = userAuthenticationMap()
        body["partId"] = partId
        body["episode"] = episode
        return query(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.addEpisode(url, body) }
    }

    /**
     * Update Episode.
     * API: PUT /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    fun updateEpisode(episode: ClientSimpleEpisode?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episode"] = listOf(episode)
        return query(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.updateEpisode(url, body) }
    }

    /**
     * Delete Episode by Id.
     * API: DELETE /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    fun deleteEpisode(episodeId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return query(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.deleteEpisode(url, body) }
    }

    private fun setConnected() {
        println("connected")
        if (disconnectedSince != null) {
            for (listener in disconnectedListeners) {
                listener.handle(disconnectedSince)
            }
            disconnectedSince = null
        }
    }

    private fun setDisconnected() {
        if (disconnectedSince == null) {
            println("disconnected")
            disconnectedSince = DateTime.now()
        }
    }

    @Throws(IOException::class)
    private fun <T, R> query(api: Class<T>, buildCall: BuildCall<T, Call<R>>): Response<R> {
        return try {
            val call = this.build(api, buildCall) ?: throw NullPointerException()

            val result = call.execute()
            setConnected()
            result
        } catch (e: NotConnectedException) {
            setDisconnected()
            throw NotConnectedException(e)
        }
    }

    @Throws(IOException::class)
    private fun <T, R> build(api: Class<T>, buildCall: BuildCall<T, Call<R>?>): Call<R>? {
        var retrofit = retrofitMap[api]
        val path = fullClassPathMap[api]
                ?: throw IllegalArgumentException("Unknown api class: " + api.canonicalName)
        server = getServer()

        // FIXME: 29.07.2019 sometimes does not find server even though it is online
        if (server == null) {
            throw NotConnectedException("No Server in reach")
        }
        if (retrofit == null) {
            val gson = GsonBuilder()
                    .registerTypeHierarchyAdapter(DateTime::class.java, DateTimeAdapter())
                    .create()
            val client = OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            retrofit = Retrofit.Builder()
                    .baseUrl(server!!.address)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            retrofitMap[api] = retrofit
        }
        if (retrofit == null) {
            throw NullPointerException()
        }

        val apiImpl = retrofit.create(api)

        return buildCall(apiImpl, path)
    }

    val isClientOnline: Boolean
        get() {
            try {
                server = getServer()
                if (server != null) {
                    setConnected()
                    return true
                }
            } catch (ignored: NotConnectedException) {
            }
            setDisconnected()
            return false
        }

    @Synchronized
    @Throws(NotConnectedException::class)
    private fun getServer(): Server? {
        val ssid = identificator.sSID
        if (ssid.isEmpty()) {
            throw NotConnectedException("Not connected to any network")
        }
        val discovery = ServerDiscovery()
        if (ssid == lastNetworkSSID) {
            if (server == null) {
                return discovery.discover(identificator.broadcastAddress)
            } else if (server!!.isReachable) {
                return server
            }
        } else {
            lastNetworkSSID = ssid
        }
        return discovery.discover(identificator.broadcastAddress)
    }

}

typealias BuildCall<T, R> = (apiImpl: T, url: String) -> R
