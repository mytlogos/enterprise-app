package com.mytlogos.enterprise.background;

import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.UnreadEpisode;
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Repository {
    boolean isClientOnline();

    boolean isClientAuthenticated();

    LoadWorker getLoadWorker();

    LiveData<User> getUser();

    void updateUser(UpdateUser updateUser);

    void deleteAllUser();

    void login(String email, String password) throws IOException;

    void register(String email, String password) throws IOException;

    void logout();

    CompletableFuture<List<ClientEpisode>> loadEpisodeAsync(Collection<Integer> episodeIds);

    List<ClientEpisode> loadEpisodeSync(Collection<Integer> episodeIds);

    CompletableFuture<List<ClientMedium>> loadMediaAsync(Collection<Integer> mediaIds);

    List<ClientMedium> loadMediaSync(Collection<Integer> mediaIds);

    CompletableFuture<List<ClientPart>> loadPartAsync(Collection<Integer> partIds);

    List<ClientPart> loadPartSync(Collection<Integer> partIds);

    CompletableFuture<ClientMultiListQuery> loadMediaListAsync(Collection<Integer> listIds);

    ClientMultiListQuery loadMediaListSync(Collection<Integer> listIds);

    CompletableFuture<List<ClientExternalMediaList>> loadExternalMediaListAsync(Collection<Integer> externalListIds);

    List<ClientExternalMediaList> loadExternalMediaListSync(Collection<Integer> externalListIds);

    CompletableFuture<List<ClientExternalUser>> loadExternalUserAsync(Collection<String> externalUuids);

    List<ClientExternalUser> loadExternalUserSync(Collection<String> externalUuids);

    CompletableFuture<List<ClientNews>> loadNewsAsync(Collection<Integer> newsIds);

    List<ClientNews> loadNewsSync(Collection<Integer> newsIds);

    LiveData<List<News>> getNews();

    void setNewsInterval(DateTime from, DateTime to);

    void removeOldNews();

    boolean isLoading();

    void refreshNews(DateTime latest) throws IOException;

    void loadInvalidated() throws IOException;

    List<Integer> getSavedEpisodes();

    void updateSaved(int episodeId, boolean saved);

    void updateSaved(Collection<Integer> episodeIds, boolean saved);

    List<Integer> getToDeleteEpisodes();

    List<ClientDownloadedEpisode> downloadedEpisodes(Collection<Integer> episodeIds) throws IOException;

    List<ToDownload> getToDownload();

    void addToDownload(ToDownload toDownload);

    void removeToDownloads(Collection<ToDownload> toDownloads);

    Collection<Integer> getExternalListItems(Integer externalListId);

    Collection<Integer> getListItems(Integer listId);

    List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds);

    List<Integer> getDownloadableEpisodes(Integer mediumId);

    LiveData<List<UnreadEpisode>> getUnReadEpisodes();

    LiveData<List<MediaList>> getLists();

    LiveData<? extends MediaListSetting> getListSettings(int id, boolean isExternal);

    CompletableFuture<String> updateListName(MediaListSetting listSetting, String text);

    CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType);

    void updateToDownload(boolean add, ToDownload toDownload);

    LiveData<List<MediumItem>> getAllMedia();

    LiveData<MediumSetting> getMediumSettings(int mediumId);

    CompletableFuture<String> updateMediumType(MediumSetting mediumSettings);
}
