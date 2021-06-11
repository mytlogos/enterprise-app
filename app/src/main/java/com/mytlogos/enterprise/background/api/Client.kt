package com.mytlogos.enterprise.background.api

import com.google.gson.GsonBuilder
import com.mytlogos.enterprise.background.api.GsonAdapter.DateTimeAdapter
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.tools.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

typealias BuildCall<T, R> = (apiImpl: T, url: String) -> R
typealias QuerySuspend<T, R> = suspend (apiImpl: T, url: String) -> Response<R>

class Client private constructor(private val identificator: NetworkIdentificator) {
    companion object: SingletonHolder<Client, NetworkIdentificator>(::Client)

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
    suspend fun checkLogin(): Response<ClientSimpleUser> {
        return querySuspend(BasicApi::class.java) { apiImpl: BasicApi, url: String -> apiImpl.checkLogin(url) }
    }

    /**
     * Login as User.
     * API: POST /api/login
     */
    @Throws(IOException::class)
    suspend fun login(mailName: String, password: String): Response<ClientUser> {
        return querySuspend(BasicApi::class.java) { apiImpl: BasicApi, url: String ->
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
    suspend fun register(mailName: String, password: String): Response<ClientUser> {
        return querySuspend(BasicApi::class.java) { apiImpl: BasicApi, url: String ->
            apiImpl.register(
                    url,
                    userVerificationMap(mailName, password)
            )
        }
    }

    private fun userAuthenticationMap(): MutableMap<String, Any?> {
        val body: MutableMap<String, Any?> = HashMap()
        val authentication = this.authentication
        checkNotNull(authentication) { "user not authenticated" }
        body["uuid"] = authentication.getUuid()
        body["session"] = authentication.getSession()
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
    @Throws(IOException::class)
    suspend fun getUser(): Response<ClientUser> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String ->
            apiImpl.getUser(url, body)
        }
    }

    /**
     * Update current User.
     * API: PUT /api/user
     */
    @Throws(IOException::class)
    suspend fun updateUser(updateUser: ClientUpdateUser?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["user"] = updateUser
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.updateUser(url, body) }
    }

    /**
     * Logout current User.
     * API: POST /api/user/logout
     */
    @Throws(IOException::class)
    suspend fun logout(): Response<Boolean> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.logout(url, body) }
    }

    /**
     * Get Lists of current User.
     * API: GET /api/user/lists
     */
    @Throws(IOException::class)
    suspend fun getLists(): Response<List<ClientMediaList>> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String ->
            apiImpl.getLists(url,  body)
        }
    }

    /**
     * Request adding a toc.
     * API: POST /api/user/toc
     */
    @Throws(IOException::class)
    suspend fun addToc(mediumId: Int, link: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["toc"] = link
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.addToc(url, body) }
    }

    /**
     * Get TOCs of multiple Media.
     * API: GET /api/user/toc
     */
    @Throws(IOException::class)
    suspend fun getMediumTocs(mediumIds: Collection<Int?>?): Response<List<ClientToc>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumIds
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getToc(url, body) }
    }

    /**
     * Delete a Toc.
     * API: DELETE /api/user/toc
     */
    @Throws(IOException::class)
    suspend fun removeToc(mediumId: Int, link: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["link"] = link
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.removeToc(url, body) }
    }

    /**
     * Get Stats about current User Data.
     * API: GET /api/user/stats
     */
    @Throws(IOException::class)
    suspend fun getStats(): Response<ClientStat> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String ->
            apiImpl.getStats(url, body)
        }
    }

    /**
     * Get New Data since lastSync.
     * API: GET /api/user/new
     */
    @Throws(IOException::class)
    suspend fun getNew(lastSync: DateTime?): Response<ClientChangedEntities> {
        val body = userAuthenticationMap()
        body["date"] = lastSync
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNew(url, body) }
    }

    /**
     * Download Episodes.
     * API: GET /api/user/download
     */
    suspend fun downloadEpisodes(episodeIds: Collection<Int?>?): Response<List<ClientDownloadedEpisode>> {
        return withContext(Dispatchers.IO) {
            val body = userAuthenticationMap()
            body["episode"] = episodeIds
            return@withContext querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String ->
                apiImpl.downloadEpisodes(url, body)
            }
        }
    }

    /**
     * Get News by Date.
     * API: GET /api/user/news
     */
    @Throws(IOException::class)
    suspend fun getNews(from: DateTime?, to: DateTime?): Response<List<ClientNews>> {
        val body = userAuthenticationMap()
        if (from != null) {
            body["from"] = from
        }
        if (to != null) {
            body["to"] = to
        }
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNews(url, body) }
    }

    /**
     * Get News by Id.
     * API: GET /api/user/news
     */
    @Throws(IOException::class)
    suspend fun getNews(newsIds: Collection<Int?>?): Response<List<ClientNews>> {
        val body = userAuthenticationMap()
        if (newsIds != null) {
            body["newsId"] = newsIds
        }
        return querySuspend(UserApi::class.java) { apiImpl: UserApi, url: String -> apiImpl.getNews(url, body) }
    }

    /**
     * Get a single ExternalUser.
     * API: GET /api/user/externalUser
     */
    @Throws(IOException::class)
    suspend fun getExternalUser(externalUuid: String?): Response<ClientExternalUser> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return querySuspend(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.getExternalUser(url, body) }
    }

    /**
     * Get multiple ExternalUser.
     * API: GET /api/user/externalUser
     */
    @Throws(IOException::class)
    suspend fun getExternalUser(externalUuid: Collection<String?>?): Response<List<ClientExternalUser>> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return querySuspend(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.getExternalUsers(url, body) }
    }

    /**
     * Add an ExternalUser.
     * API: POST /api/user/externalUser
     */
    @Throws(IOException::class)
    suspend fun addExternalUser(externalUser: AddClientExternalUser?): Response<ClientExternalUser> {
        val body = userAuthenticationMap()
        body["externalUser"] = externalUser
        return querySuspend(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.addExternalUser(url, body) }
    }

    /**
     * Delete an ExternalUser.
     * API: DELETE /api/user/externalUser
     */
    @Throws(IOException::class)
    suspend fun deleteExternalUser(externalUuid: String?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["externalUuid"] = externalUuid
        return querySuspend(ExternalUserApi::class.java) { apiImpl: ExternalUserApi, url: String -> apiImpl.deleteExternalUser(url, body) }
    }

    /**
     * Get List and its Media.
     * API: GET /api/user/new
     */
    @Throws(IOException::class)
    suspend fun getListMedia(loadedMedia: Collection<Int?>?, listId: Int): Response<ClientListQuery> {
        val body = userAuthenticationMap()
        body["media"] = loadedMedia
        body["listId"] = listId
        return querySuspend(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.getListMedia(url, body) }
    }

    /**
     * Add Medium as Item to List.
     * API: POST /api/user/list/medium
     */
    @Throws(IOException::class)
    suspend fun addListMedia(listId: Int, mediumId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return querySuspend(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.addListMedia(url, body) }
    }

    /**
     * Add multiple Media as Items to List.
     * API: POST /api/user/list/medium
     */
    @Throws(IOException::class)
    suspend fun addListMedia(listId: Int, mediumId: Collection<Int?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return querySuspend(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.addListMedia(url, body) }
    }

    /**
     * Move Medium as Item from one List to another.
     * API: PUT /api/user/list/medium
     */
    @Throws(IOException::class)
    suspend fun updateListMedia(oldListId: Int, newListId: Int, mediumId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["oldListId"] = oldListId
        body["newListId"] = newListId
        body["mediumId"] = mediumId
        return querySuspend(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.updateListMedia(url, body) }
    }

    /**
     * Remove multiple Media as Items from List.
     * API: DELETE /api/user/list/medium
     */
    @Throws(IOException::class)
    suspend fun deleteListMedia(listId: Int, mediumId: Collection<Int?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        body["mediumId"] = mediumId
        return querySuspend(ListMediaApi::class.java) { apiImpl: ListMediaApi, url: String -> apiImpl.deleteListMedia(url, body) }
    }

    /**
     * Get List and its Media Items.
     * API: GET /api/user/list
     */
    @Throws(IOException::class)
    suspend fun getList(listId: Int): Response<ClientListQuery> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        return querySuspend(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.getList(url, body) }
    }

    /**
     * Get multiple Lists and their Media Items.
     * API: GET /api/user/list
     */
    @Throws(IOException::class)
    suspend fun getLists(listIds: Collection<Int>?): Response<ClientMultiListQuery> {
        val body = userAuthenticationMap()
        body["listId"] = listIds
        return querySuspend(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.getLists(url, body) }
    }

    /**
     * Create List.
     * API: POST /api/user/list
     */
    @Throws(IOException::class)
    suspend fun addList(mediaList: ClientMinList?): Response<ClientMediaList> {
        val body = userAuthenticationMap()
        body["list"] = mediaList
        return querySuspend(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.addList(url, body) }
    }

    /**
     * Create List. Currently alias for addList.
     * API: PUT /api/user/list
     */
    @Throws(IOException::class)
    suspend fun updateList(mediaList: ClientMinList?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["list"] = mediaList
        return querySuspend(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.updateList(url, body) }
    }

    /**
     * Delete a List and its Item Mappings.
     * API: DELETE /api/user/list
     */
    @Throws(IOException::class)
    suspend fun deleteList(listId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["listId"] = listId
        return querySuspend(ListApi::class.java) { apiImpl: ListApi, url: String -> apiImpl.deleteList(url, body) }
    }

    /**
     * Get MediumInWaits.
     * API: GET /api/user/medium/unused
     */
    @Throws(IOException::class)
    suspend fun getMediumInWait(): Response<List<ClientMediumInWait>> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String ->
            apiImpl.getMediumInWait(url, body)
        }
    }

    /**
     * Get all Medium Ids.
     * API: GET /api/user/medium
     */
    @Throws(IOException::class)
    suspend fun getAllMedia(): Response<List<Int>> {
        val body: MutableMap<String, Any?> = userAuthenticationMap()
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String ->
            apiImpl.getAllMedia(url, body)
        }
    }

    /**
     * Consume Tocs from MediumInWaits to existing Medium.
     * API: PUT /api/user/medium/unused
     */
    @Throws(IOException::class)
    suspend fun consumeMediumInWait(mediumId: Int, others: Collection<ClientMediumInWait?>?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        body["tocsMedia"] = others
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.consumeMediumInWait(url, body) }
    }

    /**
     * Create Medium from MediumInWaits and add it to List.
     * API: POST /api/user/medium/create
     */
    @Throws(IOException::class)
    suspend fun createFromMediumInWait(main: ClientMediumInWait?, others: Collection<ClientMediumInWait?>?, listId: Int?): Response<ClientMedium> {
        val body = userAuthenticationMap()
        body["createMedium"] = main
        body["tocsMedia"] = others
        body["listId"] = listId
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.createFromMediumInWait(url, body) }
    }

    /**
     * Get multiple Media by Id.
     * API: GET /api/user/medium
     */
    @Throws(IOException::class)
    suspend fun getMedia(mediumIds: Collection<Int?>?): Response<List<ClientMedium>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumIds
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getMedia(url, body) }
    }

    /**
     * Get single Medium by Id.
     * API: GET /api/user/medium
     */
    @Throws(IOException::class)
    suspend fun getMedium(mediumId: Int): Response<ClientMedium> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.getMedium(url, body) }
    }

    /**
     * Create Medium.
     * API: POST /api/user/medium
     */
    @Throws(IOException::class)
    suspend fun addMedia(clientMedium: ClientSimpleMedium?): Response<ClientSimpleMedium> {
        val body = userAuthenticationMap()
        body["medium"] = clientMedium
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.addMedia(url, body) }
    }

    /**
     * Update Medium.
     * TODO: change parameter to UpdateMedium?
     * API: PUT /api/user/medium
     */
    @Throws(IOException::class)
    suspend fun updateMedia(medium: ClientMedium?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["medium"] = medium
        return querySuspend(MediumApi::class.java) { apiImpl: MediumApi, url: String -> apiImpl.updateMedia(url, body) }
    }

    /**
     * Get Progress.
     * API: GET /api/user/medium/progress
     */
    @Throws(IOException::class)
    suspend fun getProgress(episodeId: Int): Response<Float> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return querySuspend(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.getProgress(url, body) }
    }

    /**
     * Update Progress of episodes.
     * API: POST /api/user/medium/progress
     */
    @Throws(IOException::class)
    suspend fun addProgress(episodeId: Collection<Int?>?, progress: Float): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        body["progress"] = progress
        return querySuspend(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.addProgress(url, body) }
    }

    /**
     * Update Progress of episodes. Alias of addProgress.
     * API: PUT /api/user/medium/progress
     */
    @Throws(IOException::class)
    suspend fun updateProgress(episodeId: Int, progress: Float): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        body["progress"] = progress
        return querySuspend(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.updateProgress(url, body) }
    }

    /**
     * Delete Progress.
     * API: DELETE /api/user/medium/progress
     */
    @Throws(IOException::class)
    suspend fun deleteProgress(episodeId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return querySuspend(ProgressApi::class.java) { apiImpl: ProgressApi, url: String -> apiImpl.deleteProgress(url, body) }
    }

    /**
     * Get Parts by mediumId.
     * API: GET /api/user/medium/part
     */
    @Throws(IOException::class)
    suspend fun getParts(mediumId: Int): Response<MutableList<ClientPart>> {
        val body = userAuthenticationMap()
        body["mediumId"] = mediumId
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPart(url, body) }
    }

    /**
     * Get Parts by partId.
     * API: GET /api/user/medium/part
     */
    @Throws(IOException::class)
    suspend fun getParts(partIds: Collection<Int>): Response<MutableList<ClientPart>> {
        val body = userAuthenticationMap()
        body["partId"] = partIds
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPart(url, body) }
    }

    /**
     * Create Part.
     * TODO: change ClientPart to AddPart
     * API: POST /api/user/medium/part
     */
    @Throws(IOException::class)
    suspend fun addPart(part: ClientPart): Response<ClientPart> {
        val body = userAuthenticationMap()
        body["part"] = part
        body["mediumId"] = part.mediumId
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.addPart(url, body) }
    }

    /**
     * Update Part.
     * API: PUT /api/user/medium/part
     */
    @Throws(IOException::class)
    suspend fun updatePart(part: ClientPart?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["part"] = part
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.updatePart(url, body) }
    }

    /**
     * Delete Part.
     * API: DELETE /api/user/medium/part
     */
    @Throws(IOException::class)
    suspend fun deletePart(partId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["partId"] = partId
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.deletePart(url, body) }
    }

    /**
     * Get Part Ids and their EpisodeIds.
     * API: GET /api/user/medium/part/items
     */
    @Throws(IOException::class)
    suspend fun getPartEpisodes(partIds: Collection<Int?>?): Response<Map<String, List<Int>>> {
        val body = userAuthenticationMap()
        body["part"] = partIds
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPartItems(url, body) }
    }

    /**
     * Get Part Ids and their Releases.
     * API: GET /api/user/medium/part/releases
     */
    @Throws(IOException::class)
    suspend fun getPartReleases(partIds: Collection<Int?>?): Response<Map<String, List<ClientSimpleRelease>>> {
        val body = userAuthenticationMap()
        body["part"] = partIds
        return querySuspend(PartApi::class.java) { apiImpl: PartApi, url: String -> apiImpl.getPartReleases(url, body) }
    }

    /**
     * Get Episode by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    suspend fun getEpisode(episodeId: Int): Response<ClientEpisode> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return querySuspend(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.getEpisode(url, body) }
    }

    /**
     * Get Episodes by episodeId.
     * API: GET /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    suspend fun getEpisodes(episodeIds: Collection<Int>): Response<MutableList<ClientEpisode>> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeIds
        return querySuspend(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.getEpisodes(url, body) }
    }

    /**
     * Add Episode.
     * API: POST /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    suspend fun addEpisode(partId: Int, episode: ClientEpisode?): Response<ClientEpisode> {
        val body = userAuthenticationMap()
        body["partId"] = partId
        body["episode"] = episode
        return querySuspend(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.addEpisode(url, body) }
    }

    /**
     * Update Episode.
     * API: PUT /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    suspend fun updateEpisode(episode: ClientSimpleEpisode?): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episode"] = listOf(episode)
        return querySuspend(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.updateEpisode(url, body) }
    }

    /**
     * Delete Episode by Id.
     * API: DELETE /api/user/medium/part/episode
     */
    @Throws(IOException::class)
    suspend fun deleteEpisode(episodeId: Int): Response<Boolean> {
        val body = userAuthenticationMap()
        body["episodeId"] = episodeId
        return querySuspend(EpisodeApi::class.java) { apiImpl: EpisodeApi, url: String -> apiImpl.deleteEpisode(url, body) }
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

    private suspend fun <T, R> querySuspend(api: Class<T>, buildCall: QuerySuspend<T, R>): Response<R> {
        return try {
            this.buildSuspend(api, buildCall).also { setConnected() }
        } catch (e: NotConnectedException) {
            setDisconnected()
            throw NotConnectedException(e)
        }
    }

    private suspend fun <T, R> buildSuspend(api: Class<T>, buildCall: QuerySuspend<T, R>): Response<R> {
        var retrofit = retrofitMap[api]
        val path = fullClassPathMap[api]
            ?: throw IllegalArgumentException("Unknown api class: ${api.canonicalName}")
        @Suppress("BlockingMethodInNonBlockingContext")
        val localServer = getServer()
        server = localServer

        // FIXME: 29.07.2019 sometimes does not find server even though it is online
        if (localServer == null) {
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
                .baseUrl(localServer.address)
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
                runBlocking {
                    server = getServer()
                }
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
    private suspend fun getServer(): Server? = withContext(Dispatchers.IO) {
        val ssid = identificator.sSID

        if (ssid.isEmpty()) {
            throw NotConnectedException("Not connected to any network")
        }
        val discovery = ServerDiscovery()

        if (ssid == lastNetworkSSID) {
            if (server == null) {
                return@withContext discovery.discover(identificator.broadcastAddress)
            } else if (server!!.isReachable) {
                return@withContext server
            }
        } else {
            lastNetworkSSID = ssid
        }
        return@withContext discovery.discover(identificator.broadcastAddress)
    }

}