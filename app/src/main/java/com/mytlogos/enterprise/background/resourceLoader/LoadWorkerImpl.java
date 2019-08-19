package com.mytlogos.enterprise.background.resourceLoader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mytlogos.enterprise.background.ClientConsumer;
import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;

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

public class LoadWorkerImpl extends LoadWorker {

    private final Map<Object, DependantImpl> valueDependants = new ConcurrentHashMap<>();
    private final Map<Class<?>, LoaderManagerImpl<?>> loaderMap = new ConcurrentHashMap<>();

    public LoadWorkerImpl(LoadData loadedData, Repository repository, ClientModelPersister persister) {
        super(repository, persister, loadedData, null);
    }


    private <T> void addLoaderDependant(LoaderManagerImpl<T> loader, T id, @Nullable Object dependantValue, Runnable runnable) {
        if (id == null) {
            throw new IllegalArgumentException("an id of null is not valid");
        }
        if (id == (Integer) 0) {
            throw new IllegalArgumentException("an id of zero is not valid");
        }
        if (id instanceof String && ((String) id).isEmpty()) {
            throw new IllegalArgumentException("an empty id string is not valid");
        }
        DependantImpl dependantImpl = null;

        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue, o -> new DependantImpl(o, runnable));
        }
        loader.addDependant(id, dependantImpl);
    }

    @Override
    public void addIntegerIdTask(int id, @Nullable Object dependantValue, NetworkLoader<Integer> loader, Runnable runnable, boolean optional) {
        if (id == 0) {
            throw new IllegalArgumentException("an id of null is not valid");
        }
        DependantImpl dependantImpl = null;

        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue, o -> new DependantImpl(o, runnable));
        }
//        loader.addDependant(id, dependantImpl);
    }

    @Override
    public void addStringIdTask(String id, @Nullable Object dependantValue, NetworkLoader<String> loader, Runnable runnable, boolean optional) {
        if (id == null) {
            throw new IllegalArgumentException("an id of null is not valid");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("an empty id string is not valid");
        }
        DependantImpl dependantImpl = null;

        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue, o -> new DependantImpl(o, runnable));
        }
//        loader.addDependant(id, dependantImpl);
    }

    @Override
    public boolean isEpisodeLoading(int id) {
        return this.checkIsLoading(EpisodeLoader.class, id);
    }

    @Override
    public boolean isPartLoading(int id) {
        return this.checkIsLoading(PartLoader.class, id);
    }

    @Override
    public boolean isMediumLoading(int id) {
        return this.checkIsLoading(MediumLoader.class, id);
    }

    @Override
    public boolean isMediaListLoading(int id) {
        return this.checkIsLoading(MediaListLoader.class, id);
    }

    @Override
    public boolean isExternalMediaListLoading(int id) {
        return this.checkIsLoading(ExtMediaListLoader.class, id);
    }

    @Override
    public boolean isExternalUserLoading(String uuid) {
        return this.checkIsLoading(ExtUserLoader.class, uuid);
    }

    @Override
    public boolean isNewsLoading(Integer id) {
        return this.checkIsLoading(NewsLoader.class, id);
    }

    private <T> boolean checkIsLoading(Class<? extends NetworkLoader<T>> loaderClass, T value) {
        for (LoaderManagerImpl<?> manager : this.loaderMap.values()) {
            if (manager.loader.getClass().isAssignableFrom(loaderClass)) {
                //noinspection unchecked
                LoaderManagerImpl<T> loaderManager = (LoaderManagerImpl<T>) manager;
                return loaderManager.isLoading(value);
            }
        }
        return false;
    }

    @Override
    public void doWork() {
        Map<LoaderManagerImpl<?>, CompletableFuture<Void>> loaderFutures = new HashMap<>();
        Set<DependantImpl> currentDependantImpls = new HashSet<>();

        for (Map.Entry<Class<?>, LoaderManagerImpl<?>> entry : this.loaderMap.entrySet()) {
            LoaderManagerImpl<?> loader = entry.getValue();
            currentDependantImpls.addAll(loader.getCurrentDependants());
            loaderFutures.put(loader, loader.loadAsync());
        }

        // this should be a partition of the values of valueDependants
        Map<Set<LoaderManagerImpl<?>>, Set<DependantImpl>> loaderCombinations = new HashMap<>();

        for (DependantImpl dependantImpl : currentDependantImpls) {
            Set<LoaderManagerImpl<?>> keySet = dependantImpl.dependencies.keySet();

            if (keySet.isEmpty()) {
                continue;
            }
            Set<DependantImpl> dependantImpls = loaderCombinations.get(keySet);

            if (dependantImpls == null) {
                dependantImpls = Collections.synchronizedSet(new HashSet<>());
                Set<LoaderManagerImpl<?>> key = Collections.unmodifiableSet(keySet);
                loaderCombinations.put(key, dependantImpls);
            }
            dependantImpls.add(dependantImpl);
        }

        Map<CompletableFuture<Void>, Set<DependantImpl>> futureCombinations = new HashMap<>();

        loaderCombinations.forEach((loaders, dependants) -> {
            CompletableFuture<Void> combinedFuture = null;

            for (LoaderManagerImpl<?> loader : loaders) {
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
        //  for now it is loading synchronously
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

    @Override
    public void work() {
        this.doWork();
    }


    private void processFutures(List<Future<Void>> futures, CompletableFuture<Void> future, Set<DependantImpl> dependantImpls) {
        Map<ClientConsumer<?>, Set<DependantImpl>> consumerMap = mapDependantsToConsumer(dependantImpls);

        for (ClientConsumer<?> consumer : consumerMap.keySet()) {
            List<DependantImpl> withBeforeRun = new ArrayList<>();
            List<DependantImpl> withoutBeforeRun = new ArrayList<>();

            Set<DependantImpl> dependantImplSet = consumerMap.get(consumer);

            if (dependantImplSet != null) {
                for (DependantImpl dependantImpl : dependantImplSet) {
                    if (dependantImpl.runBefore != null) {
                        withBeforeRun.add(dependantImpl);
                        continue;
                    }
                    withoutBeforeRun.add(dependantImpl);
                }
            }
            // fixme this cast could be a bug
            //noinspection unchecked
            ClientConsumer<Object> clientConsumer = (ClientConsumer<Object>) consumer;
            futures.add(future
                    .thenRun(() -> {
                        System.out.println("running after loading in:" + Thread.currentThread());
                        Collection<Object> dependantsValues = new HashSet<>();

                        for (DependantImpl dependantImpl : withoutBeforeRun) {
                            // skip dependantImpls which are not ready yet
                            if (!dependantImpl.isReadyToBeConsumed()) {
                                System.out.println("dependantImpl not yet ready!: " + dependantImpl);
                                continue;
                            }
                            if (dependantImpl.value instanceof Collection) {
                                dependantsValues.addAll((Collection<?>) dependantImpl.value);
                                continue;
                            }
                            dependantsValues.add(dependantImpl.value);
                        }
                        try {
                            clientConsumer.consume(dependantsValues);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        for (DependantImpl dependantImpl : withoutBeforeRun) {
                            this.valueDependants.remove(dependantImpl.value);
                        }
                    }));

            for (DependantImpl dependantImpl : withBeforeRun) {
                futures.add(future
                        .thenRun(() -> {
                            System.out.println("running with runnable after loading in:" + Thread.currentThread());

                            dependantImpl.runBefore.run();

                            if (dependantImpl.isReadyToBeConsumed()) {
                                try {
                                    if (dependantImpl.value instanceof Collection) {
                                        //noinspection unchecked
                                        clientConsumer.consume((Collection<Object>) dependantImpl.value);
                                    } else {
                                        clientConsumer.consume(Collections.singletonList(dependantImpl.value));
                                    }
                                    this.valueDependants.remove(dependantImpl.value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // todo what todo if it is still not ready?
                                System.out.println("dependantImpl is still not ready!: " + dependantImpl);
                            }
                        }));
            }
        }
    }

    private Map<ClientConsumer<?>, Set<DependantImpl>> mapDependantsToConsumer(Set<DependantImpl> dependantImpls) {
        Map<Class<?>, Set<DependantImpl>> classValuesMap = new HashMap<>();

        for (DependantImpl dependantImpl : dependantImpls) {
            Class<?> clazz = null;

            if (dependantImpl.value instanceof Collection) {
                Collection<?> collection = (Collection<?>) dependantImpl.value;

                if (collection.isEmpty()) {
                    System.out.println("dependantImpl list value is empty");
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
                clazz = dependantImpl.value.getClass();
            }
            classValuesMap.computeIfAbsent(clazz, c -> new HashSet<>()).add(dependantImpl);
        }

        Map<ClientConsumer<?>, Set<DependantImpl>> consumerDependantsMap = new HashMap<>();
        Collection<ClientConsumer<?>> consumer = this.persister.getConsumer();

        for (ClientConsumer<?> clientConsumer : consumer) {
            Set<DependantImpl> dependantImplSet = classValuesMap.get(clientConsumer.getType());

            if (dependantImplSet != null) {
                consumerDependantsMap.put(clientConsumer, dependantImplSet);
            }
        }

        return consumerDependantsMap;
    }

    public static class DependantImpl implements Dependant {
        private final Map<LoaderManagerImpl<?>, Set<?>> dependencies = new ConcurrentHashMap<>();
        private final Object value;
        private final Runnable runBefore;

        DependantImpl(Object value, Runnable runBefore) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.runBefore = runBefore;
            this.value = value;
        }

        public Map<LoaderManagerImpl<?>, Set<?>> getDependencies() {
            return dependencies;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Runnable getRunBefore() {
            return runBefore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DependantImpl dependantImpl = (DependantImpl) o;

            return value.equals(dependantImpl.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        <T> void addDependency(LoaderManagerImpl<T> loader, T value) {
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

        @NonNull
        @Override
        public String toString() {
            return "DependantImpl{" +
                    "dependencies=" + dependencies +
                    ", value=" + value +
                    ", runBefore=" + runBefore +
                    '}';
        }
    }

    private static class LoaderManagerImpl<T> implements LoaderManager<T> {
        private final Set<T> toLoad = Collections.synchronizedSet(new HashSet<>());
        private final Set<T> loading = Collections.synchronizedSet(new HashSet<>());
        private final ConcurrentMap<T, Set<DependantImpl>> dependantMap = new ConcurrentHashMap<>();
        private final ConcurrentMap<T, CompletableFuture<Void>> loadingFutureMap = new ConcurrentHashMap<>();
        private final NetworkLoader<T> loader;

        private LoaderManagerImpl(@NonNull NetworkLoader<T> loader) {
            this.loader = loader;
        }

        private NetworkLoader<T> getLoader() {
            return this.loader;
        }

        @Override
        public CompletableFuture<Void> loadAsync() {
            Set<T> toLoad;
            Set<T> alreadyLoading;

            synchronized (this.toLoad) {
                alreadyLoading = new HashSet<>(this.toLoad);
                alreadyLoading.retainAll(this.loading);
                this.toLoad.removeAll(this.loading);
                this.toLoad.removeAll(this.loader.getLoadedSet());
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

            CompletableFuture<Void> future = this.loader.loadItemsAsync(toLoad);

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
                    if (!this.loader.getLoadedSet().contains(loaded)) {
                        System.out.println("could not load id: " + loaded + " of " + this.getClass().getSimpleName());
                    }
                    // what should happen if it could not be loaded for whatever non-exceptional reason?
                    // can there even be a non exceptional reason that it cannot be loaded?
                    this.loadingFutureMap.remove(loaded);
                    this.loading.remove(loaded);
                    Set<DependantImpl> dependantImpls = this.dependantMap.remove(loaded);

                    if (dependantImpls == null) {
                        System.out.println(
                                "Id '" + loaded + "' loaded with '" +
                                        this.getClass().getSimpleName() +
                                        "' even though there are no dependantImpls?"
                        );
                    } else {
                        for (DependantImpl dependantImpl : dependantImpls) {
                            // the dependencies of dependantImpl of this loader
                            //noinspection unchecked
                            Set<T> dependencies = (Set<T>) dependantImpl.dependencies.get(this);

                            if (dependencies == null) {
                                throw new IllegalStateException(String.format("DependantImpl listed as DependantImpl even though it does not depend on any value of %s", this.getClass().getSimpleName()));
                            }
                            dependencies.remove(loaded);
                        }
                    }
                }
            });
            return future;
        }

        @Override
        public void load() {

        }

        @Override
        public void addDependant(T value, @Nullable Dependant dependant) {
            if (dependant != null && !(dependant instanceof DependantImpl)) {
                return;
            }
            Set<DependantImpl> dependantImpls = this
                    .dependantMap
                    .computeIfAbsent(value, t -> Collections.synchronizedSet(new HashSet<>()));

            if (dependant != null) {
                DependantImpl d = (DependantImpl) dependant;
                if (dependantImpls.add(d)) {
                    this.toLoad.add(value);
                    d.addDependency(this, value);
                }
            } else {
                this.toLoad.add(value);
            }
        }

        @Override
        public boolean isLoaded(Set<T> set) {
            return this.loader.getLoadedSet().containsAll(set);
        }

        @Override
        public void removeLoaded(Set<T> set) {
            set.removeAll(this.loader.getLoadedSet());
        }

        @Override
        public Collection<DependantImpl> getCurrentDependants() {
            Set<DependantImpl> set = new HashSet<>();

            for (T t : this.toLoad) {
                Set<DependantImpl> dependantImpls = this.dependantMap.get(t);

                if (dependantImpls != null) {
                    set.addAll(dependantImpls);
                }
            }
            return set;
        }

        @Override
        public boolean isLoading(T value) {
            return this.loading.contains(value);
        }
    }

}
