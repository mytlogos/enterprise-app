package com.mytlogos.enterprise.background.resourceLoader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DependantGenerator;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;

import java.util.Objects;

public abstract class LoadWorker {
    public final NewsLoader NEWS_LOADER;
    public final EpisodeLoader EPISODE_LOADER;
    public final PartLoader PART_LOADER;
    public final MediumLoader MEDIUM_LOADER;
    public final MediaListLoader MEDIALIST_LOADER;
    public final ExtMediaListLoader EXTERNAL_MEDIALIST_LOADER;
    public final ExtUserLoader EXTERNAL_USER_LOADER;
    private static LoadWorker worker;
    final Repository repository;
    final ClientModelPersister persister;
    final LoadData loadedData;
    final DependantGenerator generator;

    public LoadWorker(Repository repository, ClientModelPersister persister, LoadData loadedData, DependantGenerator generator) {
        this.repository = repository;
        this.persister = persister;
        this.loadedData = loadedData;
        this.generator = generator;
        this.EXTERNAL_USER_LOADER = new ExtUserLoader(this);
        this.EXTERNAL_MEDIALIST_LOADER = new ExtMediaListLoader(this);
        this.MEDIALIST_LOADER = new MediaListLoader(this);
        this.MEDIUM_LOADER = new MediumLoader(this);
        this.PART_LOADER = new PartLoader(this);
        this.EPISODE_LOADER = new EpisodeLoader(this);
        this.NEWS_LOADER = new NewsLoader(this);
        LoadWorker.worker = this;
    }

    LoadWorker(Repository repository, ClientModelPersister persister, LoadData loadedData, ExtUserLoader extUserLoader, ExtMediaListLoader external_medialist_loader, MediaListLoader medialist_loader, MediumLoader medium_loader, PartLoader part_loader, EpisodeLoader episode_loader, NewsLoader news_loader, DependantGenerator generator) {
        this.repository = repository;
        this.persister = persister;
        this.loadedData = loadedData;
        this.EXTERNAL_USER_LOADER = extUserLoader;
        this.EXTERNAL_MEDIALIST_LOADER = external_medialist_loader;
        this.MEDIALIST_LOADER = medialist_loader;
        this.MEDIUM_LOADER = medium_loader;
        this.PART_LOADER = part_loader;
        this.EPISODE_LOADER = episode_loader;
        this.NEWS_LOADER = news_loader;
        this.generator = generator;
        LoadWorker.worker = this;
    }

    public static LoadWorker getWorker() {
        return worker;
    }

    public void addIntegerIdTask(int id, @Nullable DependantValue value, NetworkLoader<Integer> loader) {
        this.addIntegerIdTask(id, value, loader, false);
    }

    public void addIntegerIdTask(int id, @Nullable DependantValue value, NetworkLoader<Integer> loader, boolean optional) {
    }

    public void addStringIdTask(String id, @Nullable DependantValue value, NetworkLoader<String> loader) {
        this.addStringIdTask(id, value, loader, false);
    }

    public void addStringIdTask(String id, @Nullable DependantValue value, NetworkLoader<String> loader, boolean optional) {

    }

    @Deprecated
    public abstract void addIntegerIdTask(int id, @Nullable Object dependantValue, NetworkLoader<Integer> loader, Runnable runnable, boolean optional);

    @Deprecated
    public void addIntegerIdTask(int id, @Nullable Object dependantValue, NetworkLoader<Integer> loader, Runnable runnable) {
        checkIdLoader(id, loader);
        this.addIntegerIdTask(id, dependantValue, loader, runnable, false);
    }

    @Deprecated
    public void addIntegerIdTask(int id, @Nullable Object dependantValue, NetworkLoader<Integer> loader, boolean optional) {
        checkIdLoader(id, loader);
        this.addIntegerIdTask(id, dependantValue, loader, null, optional);
    }

    @Deprecated
    public void addIntegerIdTask(int id, @NonNull Object dependantValue, NetworkLoader<Integer> loader) {
        Objects.requireNonNull(dependantValue);
        checkIdLoader(id, loader);
        this.addIntegerIdTask(id, dependantValue, loader, null, false);
    }

    @Deprecated
    public void addIntegerIdTask(int id, NetworkLoader<Integer> loader) {
        this.addIntegerIdTask(id, null, loader, null, false);
    }

    @Deprecated
    public abstract void addStringIdTask(String id, @Nullable Object dependantValue, NetworkLoader<String> loader, Runnable runnable, boolean optional);

    @Deprecated
    public void addStringIdTask(String id, @Nullable Object dependantValue, NetworkLoader<String> loader, Runnable runnable) {
        checkIdLoader(id, loader);
        this.addStringIdTask(id, dependantValue, loader, runnable, false);
    }

    @Deprecated
    public void addStringIdTask(String id, @Nullable Object dependantValue, NetworkLoader<String> loader, boolean optional) {
        checkIdLoader(id, loader);
        this.addStringIdTask(id, dependantValue, loader, null, optional);
    }

    @Deprecated
    public void addStringIdTask(String id, @NonNull Object dependantValue, NetworkLoader<String> loader) {
        Objects.requireNonNull(dependantValue);
        checkIdLoader(id, loader);
        this.addStringIdTask(id, dependantValue, loader, null, false);
    }

    @Deprecated
    public void addStringIdTask(String id, NetworkLoader<String> loader) {
        checkIdLoader(id, loader);
        this.addStringIdTask(id, null, loader, null, false);
    }

    private void checkIdLoader(String id, NetworkLoader<String> loader) {
        Objects.requireNonNull(loader);
        Objects.requireNonNull(id);

        if (id.isEmpty()) {
            throw new IllegalArgumentException("empty id is invalid");
        }
    }

    private void checkIdLoader(int id, NetworkLoader<Integer> loader) {
        Objects.requireNonNull(loader);

        if (id <= 0) {
            throw new IllegalArgumentException("invalid id: " + id);
        }
    }

    public abstract boolean isEpisodeLoading(int id);

    public abstract boolean isPartLoading(int id);

    public abstract boolean isMediumLoading(int id);

    public abstract boolean isMediaListLoading(int id);

    public abstract boolean isExternalMediaListLoading(int id);

    public abstract boolean isExternalUserLoading(String uuid);

    public abstract boolean isNewsLoading(Integer id);

    abstract void doWork();

    public abstract void work();
}
