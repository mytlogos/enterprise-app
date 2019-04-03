package com.mytlogos.enterprise.background;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class LoadWorker {
    private final LoadData loadedData;
    private final ConcurrentMap<Object, Dependant> valueDependants = new ConcurrentHashMap<>();
    private final Repository repository;
    private final ClientModelPersister persister;
    private final ConcurrentMap<Class<?>, Loader<?>> loaderMap = new ConcurrentHashMap<>();

    LoadWorker(LoadData loadedData, Repository repository, ClientModelPersister persister) {
        this.loadedData = loadedData;
        this.repository = repository;
        this.persister = persister;
    }

    public void addEpisodeTask(int id, Object dependantValue) {
        this.addEpisodeTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addEpisodeTask(int id, Object dependantValue, Runnable runnable) {
        EpisodeLoader loader = (EpisodeLoader) this.loaderMap.computeIfAbsent(EpisodeLoader.class, c -> new EpisodeLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addPartTask(int id, Object dependantValue) {
        this.addPartTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addPartTask(int id, Object dependantValue, Runnable runnable) {
        PartLoader loader = (PartLoader) this.loaderMap.computeIfAbsent(PartLoader.class, c -> new PartLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addMediumTask(int id, Object dependantValue) {
        this.addMediumTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addMediumTask(int id, Object dependantValue, Runnable runnable) {
        MediumLoader loader = (MediumLoader) this.loaderMap.computeIfAbsent(MediumLoader.class, c -> new MediumLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addMediaListTask(int id, Object dependantValue) {
        this.addMediaListTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addMediaListTask(int id, Object dependantValue, Runnable runnable) {
        MediaListLoader loader = (MediaListLoader) this.loaderMap.computeIfAbsent(MediaListLoader.class, c -> new MediaListLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addExtMediaListTask(int id, Object dependantValue) {
        this.addExtMediaListTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addExtMediaListTask(int id, Object dependantValue, Runnable runnable) {
        ExtMediaListLoader loader = (ExtMediaListLoader) this.loaderMap.computeIfAbsent(ExtMediaListLoader.class, c -> new ExtMediaListLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addExtUserTask(String id, Object dependantValue) {
        this.addExtUserTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addExtUserTask(String id, Object dependantValue, Runnable runnable) {
        ExtUserLoader loader = (ExtUserLoader) this.loaderMap.computeIfAbsent(ExtUserLoader.class, c -> new ExtUserLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    public void addNewsTask(int id, Object dependantValue) {
        this.addNewsTask(id, dependantValue, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void addNewsTask(int id, Object dependantValue, Runnable runnable) {
        NewsLoader loader = (NewsLoader) this.loaderMap.computeIfAbsent(NewsLoader.class, c -> new NewsLoader());
        this.addLoaderDependant(loader, id, dependantValue, runnable);
    }

    private <T> void addLoaderDependant(Loader<T> loader, T id, Object dependantValue, Runnable runnable) {
        Dependant dependant = valueDependants.computeIfAbsent(dependantValue, o -> new Dependant(o, runnable));

        if (id == null) {
            throw new IllegalArgumentException("an id of null is not valid");
        }
        if (id == (Integer) 0) {
            throw new IllegalArgumentException("an id of zero is not valid");
        }
        if (id instanceof String && ((String) id).isEmpty()) {
            throw new IllegalArgumentException("an empty id string is not valid");
        }
        loader.addDependant(id, dependant);
    }

    public boolean isEpisodeLoading(int id) {
        return this.checkIsLoading(EpisodeLoader.class, id);
    }

    public boolean isPartLoading(int id) {
        return this.checkIsLoading(PartLoader.class, id);
    }

    public boolean isMediumLoading(int id) {
        return this.checkIsLoading(MediumLoader.class, id);
    }

    public boolean isMediaListLoading(int id) {
        return this.checkIsLoading(MediaListLoader.class, id);
    }

    public boolean isExternalMediaListLoading(int id) {
        return this.checkIsLoading(ExtMediaListLoader.class, id);
    }

    public boolean isExternalUserLoading(String uuid) {
        return this.checkIsLoading(ExtUserLoader.class, uuid);
    }

    public boolean isNewsLoading(Integer id) {
        return this.checkIsLoading(NewsLoader.class, id);
    }

    private <T> boolean checkIsLoading(Class<?> loaderClass, T value) {
        //noinspection unchecked
        Loader<T> loader = (Loader<T>) this.loaderMap.get(loaderClass);

        if (loader == null) {
            return false;
        }
        return loader.isLoading(value);
    }

    public void work() {
        Map<Loader<?>, CompletableFuture<Void>> loaderFutures = new HashMap<>();
        Set<Dependant> currentDependants = new HashSet<>();

        for (Map.Entry<Class<?>, Loader<?>> entry : this.loaderMap.entrySet()) {
            Loader<?> loader = entry.getValue();
            currentDependants.addAll(loader.getCurrentDependants());
            loaderFutures.put(loader, loader.load());
        }

        // this should be a partition of the values of valueDependants
        Map<Set<Loader<?>>, Set<Dependant>> loaderCombinations = new HashMap<>();

        for (Dependant dependant : currentDependants) {
            Set<Loader<?>> keySet = dependant.dependencies.keySet();

            if (keySet.isEmpty()) {
                continue;
            }
            Set<Dependant> dependants = loaderCombinations.get(keySet);

            if (dependants == null) {
                dependants = Collections.synchronizedSet(new HashSet<>());
                Set<Loader<?>> key = Collections.unmodifiableSet(keySet);
                loaderCombinations.put(key, dependants);
            }
            dependants.add(dependant);
        }

        Map<CompletableFuture<Void>, Set<Dependant>> futureCombinations = new HashMap<>();

        loaderCombinations.forEach((loaders, dependants) -> {
            CompletableFuture<Void> combinedFuture = null;

            for (Loader<?> loader : loaders) {
                CompletableFuture<Void> future = loaderFutures.get(loader);

                if (future == null) {
                    throw new IllegalStateException(String.format("loader '%s' has no future", loader.getClass().getSimpleName()));
                }
                if (combinedFuture == null) {
                    combinedFuture = future;
                } else {
                    combinedFuture = combinedFuture.thenCompose(a -> future);
                }
            }
            if (combinedFuture != null) {
                futureCombinations.put(combinedFuture, dependants);
            }
        });
        List<Future<Void>> futures = new ArrayList<>();

        futureCombinations.forEach((future, dependants) -> processFutures(futures, future, dependants));

        // fixme async loading leads to deadlocks or sth. similar, debugger does not give thread dump
        // wait for all futures to finish before returning
        /*for (Future<Void> future : futures) {
            try {
                // wait at most 10s for one future, everything above 10s should be exceptional
//                future.get(30, TimeUnit.SECONDS);
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }*/
    }


    private void processFutures(List<Future<Void>> futures, CompletableFuture<Void> future, Set<Dependant> dependants) {
        Map<ClientConsumer<?>, Set<Dependant>> consumerMap = mapDependantsToConsumer(dependants);

        for (ClientConsumer<?> consumer : consumerMap.keySet()) {
            List<Dependant> withBeforeRun = new ArrayList<>();
            List<Dependant> withoutBeforeRun = new ArrayList<>();

            Set<Dependant> dependantSet = consumerMap.get(consumer);

            for (Dependant dependant : dependantSet) {
                if (dependant.runBefore != null) {
                    withBeforeRun.add(dependant);
                    continue;
                }
                withoutBeforeRun.add(dependant);
            }
            // fixme this cast could be a bug
            //noinspection unchecked
            ClientConsumer<Object> clientConsumer = (ClientConsumer<Object>) consumer;

            futures.add(future
                    .thenRun(() -> {
                        System.out.println("running after loading in:" + Thread.currentThread());
                        Collection<Object> dependantsValues = new HashSet<>();

                        for (Dependant dependant : withoutBeforeRun) {
                            // skip dependants which are not ready yet
                            if (!dependant.isReadyToBeConsumed()) {
                                System.out.println("dependant not yet read!: " + dependant);
                                continue;
                            }
                            if (dependant.value instanceof Collection) {
                                dependantsValues.addAll((Collection<?>) dependant.value);
                                continue;
                            }
                            dependantsValues.add(dependant.value);
                        }
                        try {
                            clientConsumer.consume(dependantsValues);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        for (Dependant dependant : withoutBeforeRun) {
                            this.valueDependants.remove(dependant.value);
                        }
                    }));

            for (Dependant dependant : withBeforeRun) {
                futures.add(future
                        .thenRun(() -> {
                            System.out.println("running with runnable after loading in:" + Thread.currentThread());

                            dependant.runBefore.run();

                            if (dependant.isReadyToBeConsumed()) {
                                try {
                                    if (dependant.value instanceof Collection) {
                                        //noinspection unchecked
                                        clientConsumer.consume((Collection<Object>) dependant.value);
                                    } else {
                                        clientConsumer.consume(Collections.singletonList(dependant.value));
                                    }
                                    this.valueDependants.remove(dependant.value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // todo what todo if it is still not ready?
                                System.out.println("dependant is still not ready!: " + dependant);
                            }
                        }));
            }
        }
    }

    private Map<ClientConsumer<?>, Set<Dependant>> mapDependantsToConsumer(Set<Dependant> dependants) {
        Map<Class<?>, Set<Dependant>> classValuesMap = new HashMap<>();

        for (Dependant dependant : dependants) {
            Class<?> clazz = null;

            if (dependant.value instanceof Collection) {
                Collection<?> collection = (Collection<?>) dependant.value;

                if (collection.isEmpty()) {
                    System.out.println("dependant list value is empty");
                    continue;
                }
                // check only the first value,
                // on the assumption that every value after it has the same class
                //noinspection LoopStatementThatDoesntLoop
                for (Object o : collection) {
                    clazz = o.getClass();
                    break;
                }
            } else {
                clazz = dependant.value.getClass();
            }
            classValuesMap.computeIfAbsent(clazz, c -> new HashSet<>()).add(dependant);
        }

        Map<ClientConsumer<?>, Set<Dependant>> consumerDependantsMap = new HashMap<>();
        Collection<ClientConsumer<?>> consumer = this.persister.getConsumer();

        for (ClientConsumer<?> clientConsumer : consumer) {
            Set<Dependant> dependantSet = classValuesMap.get(clientConsumer.getType());

            if (dependantSet != null) {
                consumerDependantsMap.put(clientConsumer, dependantSet);
            }
        }

        return consumerDependantsMap;
    }

    private static class Dependant {
        private final Map<Loader<?>, Set<?>> dependencies = new ConcurrentHashMap<>();
        private final Object value;
        private final Runnable runBefore;

        private Dependant(Object value, Runnable runBefore) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.runBefore = runBefore;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dependant dependant = (Dependant) o;

            return value.equals(dependant.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        <T> void addDependency(Loader<T> loader, T value) {
            synchronized (this.dependencies) {
                //noinspection unchecked
                Set<T> set = (Set<T>) dependencies
                        .computeIfAbsent(loader, l -> Collections.synchronizedSet(new HashSet<>()));
                set.add(value);
            }
        }

        boolean isReadyToBeConsumed() {
            for (Set<?> dependencies : this.dependencies.values()) {
                if (dependencies != null && !dependencies.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "Dependant{" +
                    "dependencies=" + dependencies +
                    ", value=" + value +
                    ", runBefore=" + runBefore +
                    '}';
        }
    }

    private abstract class Loader<T> {
        private final Set<T> toLoad = Collections.synchronizedSet(new HashSet<>());
        private final Set<T> loading = Collections.synchronizedSet(new HashSet<>());
        private final ConcurrentMap<T, Set<Dependant>> dependantMap = new ConcurrentHashMap<>();
        private final ConcurrentMap<T, CompletableFuture<Void>> loadingFutureMap = new ConcurrentHashMap<>();

        CompletableFuture<Void> load() {
            Set<T> toLoad;
            Set<T> alreadyLoading;

            synchronized (this.toLoad) {
                alreadyLoading = new HashSet<>(this.toLoad);
                alreadyLoading.retainAll(this.loading);
                this.toLoad.removeAll(this.loading);
                this.toLoad.removeAll(this.getLoadedSet());
                toLoad = new HashSet<>(this.toLoad);
                this.loading.addAll(toLoad);
                this.toLoad.clear();
            }
            CompletableFuture<Void> alreadyLoadingFuture = null;
            Set<CompletableFuture<Void>> loadingFutures = new HashSet<>();

            for (T t : alreadyLoading) {
                CompletableFuture<Void> loadingFuture = loadingFutureMap.get(t);

                if (loadingFuture == null) {
                    System.err.println("missed future");
                } else {
                    if (loadingFutures.add(loadingFuture)) {
                        if (alreadyLoadingFuture == null) {
                            alreadyLoadingFuture = loadingFuture;
                        } else {
                            alreadyLoadingFuture = alreadyLoadingFuture.thenCompose(a -> loadingFuture);
                        }
                    }
                }
            }

            if (toLoad.isEmpty()) {
                return alreadyLoadingFuture != null ? alreadyLoadingFuture : CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Void> future = this.loadItems(toLoad);

            for (T loading : toLoad) {
                if (this.loadingFutureMap.put(loading, future) != null) {
                    throw new IllegalStateException("loading an already loading item");
                }
            }

            if (alreadyLoadingFuture != null) {
                final CompletableFuture<Void> finalAlreadyLoadingFuture = alreadyLoadingFuture;
                future = future.thenCompose(a -> finalAlreadyLoadingFuture);
            }

            future = future.thenRun(() -> {
                for (T loaded : toLoad) {
                    if (!this.getLoadedSet().contains(loaded)) {
                        System.out.println("could not load id: " + loaded + " of " + this.getClass().getSimpleName());
                    }
                    // what should happen if it could not be loaded for whatever non-exceptional reason?
                    // can there even be a non exceptional reason that it cannot be loaded?
                    this.loadingFutureMap.remove(loaded);
                    this.loading.remove(loaded);
                    Set<Dependant> dependants = this.dependantMap.remove(loaded);

                    if (dependants == null) {
                        System.out.println(
                                "Id '" + loaded + "' loaded with '" +
                                        this.getClass().getSimpleName() +
                                        "' even though there are no dependants?"
                        );
                    } else {
                        for (Dependant dependant : dependants) {
                            // the dependencies of dependant of this loader
                            //noinspection unchecked
                            Set<T> dependencies = (Set<T>) dependant.dependencies.get(this);

                            if (dependencies == null) {
                                throw new IllegalStateException(String.format("Dependant listed as Dependant even though it does not depend on any value of %s", this.getClass().getSimpleName()));
                            }
                            dependencies.remove(loaded);
                        }
                    }
                }
            });
            return future;
        }

        void addDependant(T value, Dependant dependant) {
            Set<Dependant> dependants = this
                    .dependantMap
                    .computeIfAbsent(value, t -> Collections.synchronizedSet(new HashSet<>()));

            if (dependants.add(dependant)) {
                this.toLoad.add(value);
                dependant.addDependency(this, value);
            }
        }

        abstract CompletableFuture<Void> loadItems(Set<T> toLoad);

        abstract Set<T> getLoadedSet();

        boolean isLoaded(Set<T> set) {
            return this.getLoadedSet().containsAll(set);
        }

        void removeLoaded(Set<T> set) {
            set.removeAll(this.getLoadedSet());
        }

        Collection<Dependant> getCurrentDependants() {
            return this
                    .toLoad
                    .stream()
                    .flatMap(t -> this.dependantMap.get(t).stream())
                    .collect(Collectors.toSet());
        }

        boolean isLoading(T value) {
            return this.loading.contains(value);
        }
    }

    private class PartLoader extends Loader<Integer> {

        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadPart(toLoad)
                    .thenAccept(parts -> {
                        if (parts != null) {
                            LoadWorker.this.persister.persistParts(parts);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getPart();
        }
    }

    private class MediumLoader extends Loader<Integer> {
        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadMedia(toLoad)
                    .thenAccept(media -> {
                        if (media != null) {
                            LoadWorker.this.persister.persistMedia(media);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getMedia();
        }
    }

    private class EpisodeLoader extends Loader<Integer> {
        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadEpisode(toLoad)
                    .thenAccept(episodes -> {
                        if (episodes != null) {
                            LoadWorker.this.persister.persistEpisodes(episodes);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getEpisodes();
        }
    }

    private class MediaListLoader extends Loader<Integer> {
        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadMediaList(toLoad)
                    .thenAccept(listQuery -> {
                        if (listQuery != null) {
                            LoadWorker.this.persister.persist(listQuery);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getMediaList();
        }
    }

    private class ExtMediaListLoader extends Loader<Integer> {
        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadExternalMediaList(toLoad)
                    .thenAccept(externalMediaLists -> {
                        if (externalMediaLists != null) {
                            LoadWorker.this.persister.persistExternalMediaLists(externalMediaLists);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getExternalMediaList();
        }
    }

    private class ExtUserLoader extends Loader<String> {
        @Override
        CompletableFuture<Void> loadItems(Set<String> toLoad) {
            return repository.loadExternalUser(toLoad)
                    .thenAccept(externalUsers -> {
                        if (externalUsers != null) {
                            LoadWorker.this.persister.persistExternalUsers(externalUsers);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<String> getLoadedSet() {
            return LoadWorker.this.loadedData.getExternalUser();
        }
    }

    private class NewsLoader extends Loader<Integer> {
        @Override
        CompletableFuture<Void> loadItems(Set<Integer> toLoad) {
            return repository.loadNews(toLoad)
                    .thenAccept(news -> {
                        if (news != null) {
                            LoadWorker.this.persister.persistNews(news);
                            LoadWorker.this.work();
                        }
                    });
        }

        @Override
        Set<Integer> getLoadedSet() {
            return LoadWorker.this.loadedData.getNews();
        }
    }
}
