package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.resourceLoader.DependantValue;
import com.mytlogos.enterprise.background.resourceLoader.DependencyTask;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DummyDependantGenerator implements DependantGenerator {
    private final LoadData loadedData;
    private List<Integer> clearedListMedia = new ArrayList<>();
    private List<Integer> clearedExternalList = new ArrayList<>();


    public DummyDependantGenerator(LoadData loadedData) {
        this.loadedData = loadedData;
    }

    public List<Integer> getClearedListMedia() {
        return clearedListMedia;
    }

    public List<Integer> getClearedExternalList() {
        return clearedExternalList;
    }

    @Override
    public Collection<DependencyTask<?>> generateReadEpisodesDependant(LoadWorkGenerator.FilteredReadEpisodes readEpisodes) {
        Set<DependencyTask<?>> tasks = new HashSet<>();
        LoadWorker worker = LoadWorker.getWorker();

        for (LoadWorkGenerator.IntDependency<ClientReadEpisode> dependency : readEpisodes.dependencies) {
            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(dependency.dependency),
                    worker.EPISODE_LOADER
            ));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generatePartsDependant(LoadWorkGenerator.FilteredParts parts) {
        Set<DependencyTask<?>> tasks = new HashSet<>();

        LoadWorker worker = LoadWorker.getWorker();
        for (LoadWorkGenerator.IntDependency<ClientPart> dependency : parts.mediumDependencies) {
            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            dependency.dependency,
                            dependency.dependency.getId(),
                            worker.PART_LOADER
                    ),
                    worker.MEDIUM_LOADER
            ));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generateEpisodesDependant(LoadWorkGenerator.FilteredEpisodes episodes) {
        Set<DependencyTask<?>> tasks = new HashSet<>();
        LoadWorker worker = LoadWorker.getWorker();

        for (LoadWorkGenerator.IntDependency<ClientEpisode> dependency : episodes.partDependencies) {
            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            dependency.dependency,
                            dependency.dependency.getId(),
                            worker.EPISODE_LOADER
                    ),
                    worker.PART_LOADER
            ));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generateMediaDependant(LoadWorkGenerator.FilteredMedia media) {
        Set<DependencyTask<?>> tasks = new HashSet<>();

        LoadWorker worker = LoadWorker.getWorker();
        for (LoadWorkGenerator.IntDependency<ClientMedium> dependency : media.episodeDependencies) {
            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            dependency.dependency,
                            dependency.dependency.getId(),
                            worker.MEDIUM_LOADER
                    ),
                    worker.EPISODE_LOADER,
                    true
            ));
        }
        for (Integer unloadedPart : media.unloadedParts) {
            tasks.add(new DependencyTask<>(unloadedPart, null, worker.PART_LOADER));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generateMediaListsDependant(LoadWorkGenerator.FilteredMediaList mediaLists) {
        Set<DependencyTask<?>> tasks = new HashSet<>();

        LoadWorker worker = LoadWorker.getWorker();
        RoomConverter converter = new RoomConverter(this.loadedData);

        for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : mediaLists.mediumDependencies) {
            int tmpListId = 0;
            if (!dependency.dependency.isEmpty()) {
                tmpListId = dependency.dependency.get(0).listId;
            }
            int listId = tmpListId;

            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            converter.convertListJoin(dependency.dependency),
                            () -> this.clearedListMedia.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            ));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generateExternalMediaListsDependant(LoadWorkGenerator.FilteredExtMediaList externalMediaLists) {
        Set<DependencyTask<?>> tasks = new HashSet<>();

        LoadWorker worker = LoadWorker.getWorker();
        RoomConverter converter = new RoomConverter(this.loadedData);

        for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : externalMediaLists.mediumDependencies) {
            int tmpListId = 0;
            if (!dependency.dependency.isEmpty()) {
                tmpListId = dependency.dependency.get(0).listId;
            }
            int listId = tmpListId;

            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            converter.convertExListJoin(dependency.dependency),
                            () -> this.clearedExternalList.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            ));
        }
        for (LoadWorkGenerator.Dependency<String, ClientExternalMediaList> dependency : externalMediaLists.userDependencies) {
            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            converter.convert(dependency.dependency),
                            dependency.dependency.getId(),
                            worker.EXTERNAL_MEDIALIST_LOADER
                    ),
                    worker.EXTERNAL_USER_LOADER
            ));
        }
        return tasks;
    }

    @Override
    public Collection<DependencyTask<?>> generateExternalUsersDependant(LoadWorkGenerator.FilteredExternalUser externalUsers) {
        Set<DependencyTask<?>> tasks = new HashSet<>();

        LoadWorker worker = LoadWorker.getWorker();
        RoomConverter converter = new RoomConverter(this.loadedData);

        for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : externalUsers.mediumDependencies) {
            int tmpListId = 0;
            if (!dependency.dependency.isEmpty()) {
                tmpListId = dependency.dependency.get(0).listId;
            }
            int listId = tmpListId;

            tasks.add(new DependencyTask<>(
                    dependency.id,
                    new DependantValue(
                            converter.convertExListJoin(dependency.dependency),
                            () -> this.clearedExternalList.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            ));
        }
        return tasks;
    }
}
