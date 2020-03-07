package com.mytlogos.enterprise.background;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;
import androidx.work.Worker;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.model.DisplayEpisode;
import com.mytlogos.enterprise.model.DisplayRelease;
import com.mytlogos.enterprise.model.Episode;
import com.mytlogos.enterprise.model.ExternalUser;
import com.mytlogos.enterprise.model.FailedEpisode;
import com.mytlogos.enterprise.model.HomeStats;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.NotificationItem;
import com.mytlogos.enterprise.model.ReadEpisode;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.model.SimpleMedium;
import com.mytlogos.enterprise.model.SpaceMedium;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class DummyRepository implements Repository {

    private LoadWorker loadWorker;

    public void setLoadWorker(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public boolean isClientOnline() {
        return true;
    }

    @Override
    public boolean isClientAuthenticated() {
        return true;
    }

    @Override
    public LoadWorker getLoadWorker() {
        return loadWorker;
    }

    @Override
    public LiveData<HomeStats> getHomeStats() {
        return null;
    }

    @Override
    public LiveData<User> getUser() {
        return null;
    }

    @Override
    public void updateUser(UpdateUser updateUser) {

    }

    @Override
    public void deleteAllUser() {

    }

    @Override
    public void login(String email, String password) throws IOException {

    }

    @Override
    public void register(String email, String password) throws IOException {

    }

    @Override
    public void logout() {

    }

    @Override
    public void loadAllMedia() {

    }

    @Override
    public CompletableFuture<List<ClientEpisode>> loadEpisodeAsync(Collection<Integer> episodeIds) {
        return null;
    }

    @Override
    public List<ClientEpisode> loadEpisodeSync(Collection<Integer> episodeIds) {
        return null;
    }

    @Override
    public CompletableFuture<List<ClientMedium>> loadMediaAsync(Collection<Integer> mediaIds) {
        return null;
    }

    @Override
    public List<ClientMedium> loadMediaSync(Collection<Integer> mediaIds) {
        return null;
    }

    @Override
    public CompletableFuture<List<ClientPart>> loadPartAsync(Collection<Integer> partIds) {
        return null;
    }

    @Override
    public List<ClientPart> loadPartSync(Collection<Integer> partIds) {
        return null;
    }

    @Override
    public CompletableFuture<ClientMultiListQuery> loadMediaListAsync(Collection<Integer> listIds) {
        return null;
    }

    @Override
    public ClientMultiListQuery loadMediaListSync(Collection<Integer> listIds) {
        return null;
    }

    @Override
    public CompletableFuture<List<ClientExternalMediaList>> loadExternalMediaListAsync(Collection<Integer> externalListIds) {
        return null;
    }

    @Override
    public List<ClientExternalMediaList> loadExternalMediaListSync(Collection<Integer> externalListIds) {
        return null;
    }

    @Override
    public CompletableFuture<List<ClientExternalUser>> loadExternalUserAsync(Collection<String> externalUuids) {
        return null;
    }

    @Override
    public List<ClientExternalUser> loadExternalUserSync(Collection<String> externalUuids) {
        return null;
    }

    @Override
    public CompletableFuture<List<ClientNews>> loadNewsAsync(Collection<Integer> newsIds) {
        return null;
    }

    @Override
    public List<ClientNews> loadNewsSync(Collection<Integer> newsIds) {
        return null;
    }

    @Override
    public LiveData<PagedList<News>> getNews() {
        return null;
    }

    @Override
    public void removeOldNews() {

    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void refreshNews(DateTime latest) throws IOException {

    }

    @Override
    public void loadInvalidated() throws IOException {

    }

    @Override
    public List<Integer> getSavedEpisodes() {
        return null;
    }

    @Override
    public void updateSaved(int episodeId, boolean saved) {

    }

    @Override
    public void updateSaved(Collection<Integer> episodeIds, boolean saved) {

    }

    @Override
    public List<Integer> getToDeleteEpisodes() {
        return null;
    }

    @Override
    public List<ClientDownloadedEpisode> downloadEpisodes(Collection<Integer> episodeIds) throws IOException {
        return null;
    }

    @Override
    public List<ToDownload> getToDownload() {
        return null;
    }

    @Override
    public void addToDownload(ToDownload toDownload) {

    }

    @Override
    public void removeToDownloads(Collection<ToDownload> toDownloads) {

    }

    @Override
    public Collection<Integer> getExternalListItems(Integer externalListId) {
        return null;
    }

    @Override
    public Collection<Integer> getListItems(Integer listId) {
        return null;
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds) {
        return null;
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Integer mediumId, int limit) {
        return null;
    }

    @Override
    public LiveData<PagedList<DisplayRelease>> getDisplayEpisodes(EpisodeViewModel.Filter filter) {
        return null;
    }

    @Override
    public LiveData<PagedList<DisplayEpisode>> getDisplayEpisodesGrouped(int saved, int medium) {
        return null;
    }

    @Override
    public LiveData<List<MediaList>> getLists() {
        return null;
    }

    @Override
    public LiveData<? extends MediaListSetting> getListSettings(int id, boolean isExternal) {
        return null;
    }

    @Override
    public CompletableFuture<String> updateListName(MediaListSetting listSetting, String text) {
        return null;
    }

    @Override
    public CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType) {
        return null;
    }

    @Override
    public void updateToDownload(boolean add, ToDownload toDownload) {

    }

    @Override
    public LiveData<PagedList<MediumItem>> getAllMedia(Sortings sortings, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes) {
        return null;
    }

    @Override
    public LiveData<MediumSetting> getMediumSettings(int mediumId) {
        return null;
    }

    @Override
    public CompletableFuture<String> updateMedium(MediumSetting mediumSettings) {
        return null;
    }

    @Override
    public LiveData<PagedList<TocEpisode>> getToc(int mediumId, Sortings sortings, byte read, byte saved) {
        return new MutableLiveData<>();
    }

    @Override
    public LiveData<List<MediumItem>> getMediumItems(int listId, boolean isExternal) {
        return new MutableLiveData<>();
    }

    @Override
    public void loadMediaInWaitSync() {

    }

    @Override
    public void addList(MediaList list, boolean autoDownload) {

    }

    @Override
    public boolean listExists(String listName) {
        return false;
    }

    @Override
    public int countSavedUnreadEpisodes(Integer mediumId) {
        return 0;
    }

    @Override
    public List<Integer> getSavedEpisodes(int mediumId) {
        return null;
    }

    @Override
    public Episode getEpisode(int episodeId) {
        return null;
    }

    @Override
    public void updateRead(Collection<Integer> episodeIds, boolean read) {

    }

    @Override
    public LiveData<PagedList<ReadEpisode>> getReadTodayEpisodes() {
        return null;
    }

    @Override
    public LiveData<PagedList<MediumInWait>> getMediaInWaitBy(String filter, int mediumFilter, String hostFilter, Sortings sortings) {
        return null;
    }

    @Override
    public LiveData<List<MediaList>> getInternLists() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> moveMediaToList(int oldListId, int listId, Collection<Integer> ids) {
        return null;
    }

    @Override
    public LiveData<List<MediumInWait>> getSimilarMediaInWait(MediumInWait mediumInWait) {
        return null;
    }

    @Override
    public LiveData<List<SimpleMedium>> getMediaSuggestions(String title, int medium) {
        return null;
    }

    @Override
    public LiveData<List<MediumInWait>> getMediaInWaitSuggestions(String input, int medium) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> consumeMediumInWait(SimpleMedium selectedMedium, List<MediumInWait> mediumInWaits) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> createMedium(MediumInWait mediumInWait, List<MediumInWait> mediumInWaits, MediaList list) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> removeItemFromList(int listId, int mediumId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> removeItemFromList(int listId, Collection<Integer> mediumId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> moveItemFromList(int oldListId, int newListId, int mediumId) {
        return null;
    }

    @Override
    public LiveData<List<MediaList>> getListSuggestion(String name) {
        return null;
    }

    @Override
    public LiveData<Boolean> onDownloadable() {
        return null;
    }

    @Override
    public void removeDanglingMedia(Collection<Integer> mediaIds) {

    }

    @Override
    public LiveData<List<MediumItem>> getAllDanglingMedia() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> addMediumToList(int listId, Collection<Integer> ids) {
        return null;
    }

    @Override
    public LiveData<PagedList<ExternalUser>> getExternalUser() {
        return null;
    }

    @Override
    public SpaceMedium getSpaceMedium(int mediumId) {
        return null;
    }

    @Override
    public int getMediumType(Integer mediumId) {
        return 0;
    }

    @Override
    public List<String> getReleaseLinks(int episodeId) {
        return null;
    }

    @Override
    public void syncUser() {

    }

    @Override
    public void updateReadWithLowerIndex(double episodeId, boolean read, int mediumId) {
    }

    @Override
    public void clearLocalMediaData(Context context) {

    }

    @Override
    public LiveData<PagedList<NotificationItem>> getNotifications() {
        return null;
    }

    @Override
    public void updateFailedDownloads(int episodeId) {

    }

    @Override
    public List<FailedEpisode> getFailedEpisodes(Collection<Integer> episodeIds) {
        return null;
    }

    @Override
    public void addNotification(NotificationItem notification) {

    }

    @Override
    public SimpleEpisode getSimpleEpisode(int episodeId) {
        return null;
    }

    @Override
    public SimpleMedium getSimpleMedium(Integer mediumId) {
        return null;
    }

    @Override
    public void clearNotifications() {

    }

    @Override
    public void clearFailEpisodes() {

    }

    @Override
    public void updateAllRead(int episodeId, boolean read) {

    }

    @Override
    public void deleteAllLocalEpisodes(int mediumId, Application application) throws IOException {

    }

    @Override
    public void deleteLocalEpisodesWithLowerIndex(double episodeId, int mediumId, Application application) throws IOException {

    }

    @Override
    public void deleteLocalEpisodesWithHigherIndex(double combiIndex, int mediumId, Application application) {

    }

    @Override
    public void deleteLocalEpisodes(Set<Integer> episodeId, int mediumId, Application application) throws IOException {

    }

    @Override
    public void addProgressListener(Consumer<Integer> consumer) {

    }

    @Override
    public void removeProgressListener(Consumer<Integer> consumer) {

    }

    @Override
    public void addTotalWorkListener(Consumer<Integer> consumer) {

    }

    @Override
    public void removeTotalWorkListener(Consumer<Integer> consumer) {

    }

    @Override
    public int getLoadWorkerProgress() {
        return 0;
    }

    @Override
    public int getLoadWorkerTotalWork() {
        return 0;
    }

    @Override
    public void syncProgress() {

    }

    @Override
    public void updateDataStructure(List<Integer> mediaIds, List<Integer> partIds) {

    }

    @Override
    public void reloadLowerIndex(double combiIndex, int mediumId) {

    }

    @Override
    public void reloadHigherIndex(double combiIndex, int mediumId) {

    }

    @Override
    public void reload(Set<Integer> episodeId) {

    }

    @Override
    public void reloadAll(int mediumId) {

    }

    @Override
    public void downloadLowerIndex(double combiIndex, int mediumId, Context context) {

    }

    @Override
    public void downloadHigherIndex(double combiIndex, int mediumId, Context context) {

    }

    @Override
    public void download(Set<Integer> episodeId, int mediumId, Context context) {

    }

    @Override
    public void downloadAll(int mediumId, Context context) {

    }

    @Override
    public void updateProgress(int episodeId, float progress) {

    }

    @Override
    public Client getClient(Worker worker) {
        return null;
    }

    @Override
    public ClientModelPersister getPersister(Worker worker) {
        return null;
    }

    @Override
    public boolean isMediumLoaded(int mediumId) {
        return false;
    }

    @Override
    public boolean isPartLoaded(int partId) {
        return false;
    }

    @Override
    public boolean isEpisodeLoaded(int episodeId) {
        return false;
    }

    @Override
    public boolean isExternalUserLoaded(String uuid) {
        return false;
    }

    @Override
    public ReloadPart checkReload(ClientStat.ParsedStat stat) {
        return null;
    }

    @Override
    public void updateReadWithHigherIndex(double episodeId, boolean read, int mediumId) {

    }

    @Override
    public List<SimpleEpisode> getSimpleEpisodes(Collection<Integer> ids) {
        return null;
    }

    @Override
    public void updateRead(int episodeId, boolean read) throws IOException {

    }
}
