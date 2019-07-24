package com.mytlogos.enterprise.background.resourceLoader;

import androidx.annotation.Nullable;

import com.mytlogos.enterprise.background.ClientConsumer;
import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DependantGenerator;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockingLoadWorker extends LoadWorker {
    // TODO: 11.06.2019 starting after a month with this new loader lead to many episodeNodes rejecting
    // FIXME: 11.06.2019 apparently the databaseValidator detected a null id value as parameter and threw an error
    private final ConcurrentMap<DependantValue, DependantNode> valueNodes = new ConcurrentHashMap<>();
    private final ExecutorService workService = Executors.newSingleThreadExecutor();
    private final ExecutorService loadingService = Executors.newFixedThreadPool(5);

    private Map<NetworkLoader<Integer>, IntLoaderManager> intLoaderManager = new HashMap<>();
    private Map<NetworkLoader<String>, StringLoaderManager> stringLoaderManager = new HashMap<>();

    public BlockingLoadWorker(LoadData loadedData, Repository repository, ClientModelPersister persister, DependantGenerator generator) {
        super(repository, persister, loadedData, generator);
    }

    BlockingLoadWorker(Repository repository, ClientModelPersister persister, LoadData loadedData, ExtUserLoader extUserLoader, ExtMediaListLoader external_medialist_loader, MediaListLoader medialist_loader, MediumLoader medium_loader, PartLoader part_loader, EpisodeLoader episode_loader, NewsLoader news_loader, Map<Class<?>, LoaderManagerImpl<?>> loaderManagerMap, DependantGenerator generator) {
        super(repository, persister, loadedData, extUserLoader, external_medialist_loader, medialist_loader, medium_loader, part_loader, episode_loader, news_loader, generator);
    }

    BlockingLoadWorker(Repository repository, ClientModelPersister persister, LoadData loadedData, ExtUserLoader extUserLoader, ExtMediaListLoader external_medialist_loader, MediaListLoader medialist_loader, MediumLoader medium_loader, PartLoader part_loader, EpisodeLoader episode_loader, NewsLoader news_loader, DependantGenerator generator) {
        super(repository, persister, loadedData, extUserLoader, external_medialist_loader, medialist_loader, medium_loader, part_loader, episode_loader, news_loader, generator);
    }

    public void addIntegerIdTask(int id, @Nullable DependantValue value, NetworkLoader<Integer> loader, boolean optional) {
        this.addIdTask(id, value, optional, this.getIntLoaderManager(loader));
    }

    public void addStringIdTask(String id, @Nullable DependantValue value, NetworkLoader<String> loader, boolean optional) {
        this.addIdTask(id, value, optional, this.getStringLoaderManager(loader));
    }

    private IntLoaderManager getIntLoaderManager(NetworkLoader<Integer> loader) {
        return this.intLoaderManager.computeIfAbsent(loader, loader1 -> new IntLoaderManager(loader1, loader.getLoadedSet()));
    }

    private StringLoaderManager getStringLoaderManager(NetworkLoader<String> loader) {
        return this.stringLoaderManager.computeIfAbsent(loader, loader1 -> new StringLoaderManager(loader1, loader.getLoadedSet()));
    }

    private <T> void addIdTask(T id, @Nullable DependantValue value, boolean optional, LoaderManagerImpl<T> loaderManager) {
        DependantNode node = null;

        if (value != null) {
            node = this.valueNodes.get(value);

            if (value.getIntId() > 0) {
                IntLoaderManager manager = this.getIntLoaderManager(value.getIntegerLoader());
                node = manager
                        .valueDependants
                        .compute(value.getIntId(), (i, remapNode) -> remapNode(value, remapNode));
            } else if (value.getStringId() != null) {
                StringLoaderManager manager = this.getStringLoaderManager(value.getStringLoader());
                node = manager
                        .valueDependants
                        .compute(value.getStringId(), (s, remapNode) -> remapNode(value, remapNode));
            }
            if (node == null) {
                node = new DependantNode(value);
                this.valueNodes.put(value, node);
            }
        }
        loaderManager.addDependant(id, node, optional);
    }

    private DependantNode remapNode(DependantValue value, DependantNode dependantNode) {
        if (dependantNode == null || !dependantNode.isRoot()) {
            return null;
        }
        DependantNode newNode = dependantNode.createNewNode(value);
        this.valueNodes.put(value, newNode);
        return newNode;
    }

    @Deprecated
    @Override
    public void addIntegerIdTask(int id, @Nullable Object dependantValue, NetworkLoader<Integer> loader, Runnable runnable, boolean optional) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void addStringIdTask(String id, @Nullable Object dependantValue, NetworkLoader<String> loader, Runnable runnable, boolean optional) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEpisodeLoading(int id) {
        return isIntegerIdLoading(id, this.EPISODE_LOADER);
    }

    @Override
    public boolean isPartLoading(int id) {
        return isIntegerIdLoading(id, this.PART_LOADER);
    }

    @Override
    public boolean isMediumLoading(int id) {
        return isIntegerIdLoading(id, this.MEDIUM_LOADER);
    }

    @Override
    public boolean isMediaListLoading(int id) {
        return isIntegerIdLoading(id, this.MEDIALIST_LOADER);
    }

    @Override
    public boolean isExternalMediaListLoading(int id) {
        return isIntegerIdLoading(id, this.EXTERNAL_MEDIALIST_LOADER);
    }

    @Override
    public boolean isExternalUserLoading(String uuid) {
        return isStringIdLoading(uuid, this.EXTERNAL_USER_LOADER);
    }

    private boolean isStringIdLoading(String uuid, NetworkLoader<String> loader) {
        StringLoaderManager manager = this.stringLoaderManager.get(loader);
        if (manager == null) {
            return false;
        }
        return manager.isLoading(uuid);
    }

    @Override
    public boolean isNewsLoading(Integer id) {
        return isIntegerIdLoading(id, this.NEWS_LOADER);
    }

    private boolean isIntegerIdLoading(int id, NetworkLoader<Integer> loader) {
        IntLoaderManager manager = this.intLoaderManager.get(loader);
        if (manager == null) {
            return false;
        }
        return manager.isLoading(id);
    }

    private <T> Collection<DependantNode> processTasks(LoaderManagerImpl<T> manager, Set<T> freeIds, Collection<DependencyTask<?>> tasks) {
        for (DependencyTask<?> task : tasks) {
            if (task.idValue instanceof Integer) {
                //noinspection unchecked
                this.addIntegerIdTask(
                        (Integer) task.idValue,
                        task.dependantValue,
                        (NetworkLoader<Integer>) task.loader,
                        task.optional
                );
                removeUnresolvedIds(manager, freeIds, task);
            } else if (task.idValue instanceof String) {
                //noinspection unchecked
                this.addStringIdTask(
                        (String) task.idValue,
                        task.dependantValue,
                        (NetworkLoader<String>) task.loader,
                        task.optional
                );
                removeUnresolvedIds(manager, freeIds, task);
            } else {
                throw new IllegalArgumentException("unknown id value type: neither Integer nor String");
            }
        }

        return getResolvedNodes(manager, freeIds);
    }

    private <T> void removeUnresolvedIds(LoaderManagerImpl<T> manager, Set<T> freeIds, DependencyTask<?> task) {
        if (task.optional || task.dependantValue == null) {
            return;
        }
        if (task.dependantValue.getIntegerLoader() == manager.getLoader()) {
            //noinspection SuspiciousMethodCalls
            freeIds.remove(task.dependantValue.getIntId());
        } else if (task.dependantValue.getStringLoader() == manager.getLoader()) {
            //noinspection SuspiciousMethodCalls
            freeIds.remove(task.dependantValue.getStringId());
        }
    }

    private <T> Collection<DependantNode> loadIds(LoaderManagerImpl<T> manager) {
        Set<T> freeIds = manager.getFreeIds();
        System.out.printf("Loader: %s got Ids: %s\n", manager.getLoader().getClass().getSimpleName(), freeIds);

        if (freeIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (manager.loaded.containsAll(freeIds)) {
            return getResolvedNodes(manager, freeIds);
        }

        Collection<DependencyTask<?>> tasks = manager.getLoader().loadItemsSyncIncremental(freeIds);

        for (DependencyTask<?> task : tasks) {
            if (task.idValue instanceof Integer) {
                //noinspection unchecked
                this.addIntegerIdTask(
                        (Integer) task.idValue,
                        task.dependantValue,
                        (NetworkLoader<Integer>) task.loader,
                        task.optional
                );
                removeUnresolvedId(manager, freeIds, task);
            } else if (task.idValue instanceof String) {
                //noinspection unchecked
                this.addStringIdTask(
                        (String) task.idValue,
                        task.dependantValue,
                        (NetworkLoader<String>) task.loader,
                        task.optional
                );
                removeUnresolvedId(manager, freeIds, task);
            } else {
                throw new IllegalArgumentException("unknown id value type: neither Integer nor String");
            }
        }

        return getResolvedNodes(manager, freeIds);
    }

    private <T> void removeUnresolvedId(LoaderManagerImpl<T> manager, Set<T> freeIds, DependencyTask<?> task) {
        if (!task.optional && task.dependantValue != null) {
            if (task.dependantValue.getIntegerLoader() == manager.getLoader()) {
                //noinspection SuspiciousMethodCalls
                freeIds.remove(task.dependantValue.getIntId());
            } else if (task.dependantValue.getStringLoader() == manager.getLoader()) {
                //noinspection SuspiciousMethodCalls
                freeIds.remove(task.dependantValue.getStringId());
            }
        }
    }

    private <T> Collection<DependantNode> getResolvedNodes(LoaderManagerImpl<T> manager, Set<T> freeIds) {
        Collection<DependantNode> nodes = new ArrayList<>();

        System.out
                .printf("Consumed FreeIds %d for %s", freeIds.size(), manager.getLoader().getClass().getSimpleName())
                .println();

        for (T id : freeIds) {
            DependantNode node = manager.getIdDependant(id);

            if (node == null) {
                System.err.println("Node is null even though it just loaded");
                continue;
            }
            manager.removeId(id);

            if (!manager.isLoaded(id)) {
                System.out
                        .printf("Rejecting Dependencies for Id '%s' with loader '%s'", id, manager.getLoader().getClass().getSimpleName())
                        .println();
                node.rejectNode();
                continue;
            }
            nodes.addAll(node.removeAsParent());
        }
        return nodes;
    }

    private void consumeDependantValue(Collection<DependantValue> values) {
        Objects.requireNonNull(values);
        Map<ClientConsumer<?>, Set<DependantValue>> consumerMap = this.mapDependantsToConsumer(values);

        for (Map.Entry<ClientConsumer<?>, Set<DependantValue>> entry : consumerMap.entrySet()) {
            Collection<Object> dependantsValues = new HashSet<>();

            for (DependantValue value : entry.getValue()) {
                if (value.getValue() instanceof Collection) {
                    dependantsValues.addAll((Collection<?>) value.getValue());
                    continue;
                }
                dependantsValues.add(value.getValue());
            }
            try {
                //noinspection unchecked
                ClientConsumer<Object> clientConsumer = (ClientConsumer<Object>) entry.getKey();
                clientConsumer.consume(dependantsValues);

                for (DependantValue value : entry.getValue()) {
                    DependantNode node = this.valueNodes.remove(value);

                    if (node != null && !node.isFree()) {
                        System.out.println("removed node is not free!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private Map<ClientConsumer<?>, Set<DependantValue>> mapDependantsToConsumer(Collection<DependantValue> values) {
        Map<Class<?>, Set<DependantValue>> classValuesMap = new HashMap<>();

        for (DependantValue dependantImpl : values) {
            Class<?> clazz = null;

            if (dependantImpl.getValue() instanceof Collection) {
                Collection<?> collection = (Collection<?>) dependantImpl.getValue();

                if (collection.isEmpty()) {
                    System.err.println("collection is empty");
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
                clazz = dependantImpl.getValue().getClass();
            }
            classValuesMap.computeIfAbsent(clazz, c -> new HashSet<>()).add(dependantImpl);
        }

        Map<ClientConsumer<?>, Set<DependantValue>> consumerDependantsMap = new HashMap<>();
        Collection<ClientConsumer<?>> consumer = this.persister.getConsumer();

        for (ClientConsumer<?> clientConsumer : consumer) {
            Set<DependantValue> dependantImplSet = classValuesMap.get(clientConsumer.getType());

            if (dependantImplSet != null) {
                consumerDependantsMap.put(clientConsumer, dependantImplSet);
            }
        }

        return consumerDependantsMap;
    }

    private boolean hasCircularDependencies() {
        for (DependantNode root : this.valueNodes.values()) {
            Set<DependantNode> visitedNodes = new HashSet<>();
            Deque<DependantNode> nodeStack = new LinkedList<>();

            nodeStack.push(root);

            while (!nodeStack.isEmpty()) {
                DependantNode current = nodeStack.pop();

                if (!visitedNodes.add(current)) {
                    return true;
                }

                for (DependantNode child : current.getChildren()) {
                    nodeStack.push(child);
                }
            }
        }
        return false;
    }

    private interface ProcessTasks extends Function<Collection<DependencyTask<?>>, Collection<DependantNode>> {

    }

    @Override
    public void doWork() {
        System.out.println("do work");
        print();

        Collection<DependantNode> nodes = new ArrayList<>();
        Map<Future<Collection<DependencyTask<?>>>, ProcessTasks> futureProcessTasksMap = new HashMap<>();

        for (StringLoaderManager manager : new ArrayList<>(this.stringLoaderManager.values())) {
            processLoadingManager(nodes, futureProcessTasksMap, manager);
        }
        for (LoaderManagerImpl<Integer> manager : new ArrayList<>(this.intLoaderManager.values())) {
            processLoadingManager(nodes, futureProcessTasksMap, manager);
        }

        for (Map.Entry<Future<Collection<DependencyTask<?>>>, ProcessTasks> entry : futureProcessTasksMap.entrySet()) {
            try {
                Collection<DependencyTask<?>> tasks = entry.getKey().get();
//                Collection<DependencyTask<?>> tasks = entry.getKey().get(20, TimeUnit.SECONDS);
                nodes.addAll(entry.getValue().apply(tasks));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*for (StringLoaderManager manager : new ArrayList<>(this.stringLoaderManager.values())) {
            if (manager.hasFreeIds()) {
                nodes.addAll(this.loadIds(manager));
            }
        }
        for (LoaderManagerImpl<Integer> manager : new ArrayList<>(this.intLoaderManager.values())) {
            if (manager.hasFreeIds()) {
                nodes.addAll(this.loadIds(manager));
            }
        }*/

        System.out.println("finish work");
        print();

        // todo only exit condition, maybe do more
        if (!nodes.isEmpty()) {
            Collection<DependantValue> values = new ArrayList<>();
            for (DependantNode node : nodes) {
                if (node.isFree()) {
                    if (node.getValue().getRunnable() != null) {
                        try {
                            node.getValue().getRunnable().run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    values.add(node.getValue());
                }
            }
            this.consumeDependantValue(values);
        } else if (!this.hasFreeIds()) {
            System.out.println("empty nodes and no free ids");
            return;
        }

        this.doWork();
    }

    private void print() {
        for (Map.Entry<NetworkLoader<Integer>, IntLoaderManager> entry : this.intLoaderManager.entrySet()) {
            printManager(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<NetworkLoader<String>, StringLoaderManager> entry : this.stringLoaderManager.entrySet()) {
            printManager(entry.getKey(), entry.getValue());
        }
        String s = getFormattedLoaded("Media", this.loadedData.getMedia()) +
                getFormattedLoaded("Parts", this.loadedData.getPart()) +
                getFormattedLoaded("Episodes", this.loadedData.getEpisodes()) +
                getFormattedLoaded("News", this.loadedData.getNews()) +
                getFormattedLoaded("MediaLists", this.loadedData.getMediaList()) +
                getFormattedLoaded("ExtMediaLists", this.loadedData.getExternalMediaList()) +
                getFormattedLoaded("ExtUser", this.loadedData.getExternalUser());
        System.err.println(s);
    }

    private <T> String getFormattedLoaded(String s, Set<T> set) {
        return String.format(Locale.getDefault(), "Loaded %s %d: %s\n", s, set.size(), set);
    }

    private <T> void printManager(NetworkLoader<T> loader, LoaderManagerImpl<T> manager) {
        String name = loader.getClass().getSimpleName();
        Set<T> loading = manager.loading;
        Set<T> freeIds = manager.getFreeIds();

        for (T loadingId : loading) {
            if (!manager.valueDependants.containsKey(loadingId)) {
                System.out.printf("%s contains loading Id '%s' without a node\n", name, loadingId);
            }
        }
        Map<String, Integer> classFrequencyChildren = new HashMap<>();
        Map<String, Integer> classFrequencyOptChildren = new HashMap<>();

        for (DependantNode node : manager.valueDependants.values()) {
            for (DependantNode child : node.getChildren()) {
                String simpleName = child.getValue().getValue().getClass().getSimpleName();
                classFrequencyChildren.merge(simpleName, 1, Integer::sum);
            }
            for (DependantNode child : node.getOptionalChildren()) {
                String simpleName = child.getValue().getValue().getClass().getSimpleName();
                classFrequencyOptChildren.merge(simpleName, 1, Integer::sum);
            }
        }
        String format = "%s:\nLoading %d: %s\nFree %d: %s\n";
        StringBuilder builder = new StringBuilder(format + "Children: ");

        List<Object> parameterList = new ArrayList<>((classFrequencyChildren.size() * 2) + (classFrequencyOptChildren.size() * 2));

        for (Map.Entry<String, Integer> stringIntegerEntry : classFrequencyChildren.entrySet()) {
            builder.append("%s: %d, ");
            parameterList.add(stringIntegerEntry.getKey());
            parameterList.add(stringIntegerEntry.getValue());
        }
        builder.append("\nOptional Children: ");

        for (Map.Entry<String, Integer> stringIntegerEntry : classFrequencyOptChildren.entrySet()) {
            builder.append("%s: %d, ");
            parameterList.add(stringIntegerEntry.getKey());
            parameterList.add(stringIntegerEntry.getValue());
        }
        builder.append("\n");

        Object[] objects = {name, loading.size(), loading, freeIds.size(), freeIds};
        Object[] parameter = Stream.concat(Arrays.stream(objects), parameterList.stream()).toArray();

        System.err.println(String.format(
                Locale.getDefault(),
                builder.toString(),
                parameter
        ));
    }

    private <T> void processLoadingManager(Collection<DependantNode> nodes, Map<Future<Collection<DependencyTask<?>>>, ProcessTasks> futureProcessTasksMap, LoaderManagerImpl<T> manager) {
        if (manager.hasFreeIds()) {
            Set<T> freeIds = manager.getFreeIds();
            System.out.printf("Loader: %s got Ids: %s\n", manager.getLoader().getClass().getSimpleName(), freeIds);

            if (manager.loaded.containsAll(freeIds)) {
                nodes.addAll(getResolvedNodes(manager, freeIds));
            }
            Future<Collection<DependencyTask<?>>> future = this.loadingService.submit(() -> manager.getLoader().loadItemsSyncIncremental(freeIds));
            ProcessTasks processTasks = tasks -> this.processTasks(manager, freeIds, tasks);
            futureProcessTasksMap.put(future, processTasks);
        }
    }

    private boolean hasFreeIds() {
        for (IntLoaderManager manager : this.intLoaderManager.values()) {
            if (manager.hasFreeIds()) {
                return true;
            }
        }
        for (StringLoaderManager manager : this.stringLoaderManager.values()) {
            if (manager.hasFreeIds()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void work() {
        try {
            workService.submit(this::doWork).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static abstract class LoaderManagerImpl<T> {
        private final Set<T> loading = Collections.synchronizedSet(new HashSet<>());
        private final Set<T> loaded;
        final ConcurrentMap<T, DependantNode> valueDependants = new ConcurrentHashMap<>();
        private final NetworkLoader<T> loader;

        LoaderManagerImpl(NetworkLoader<T> loader, Set<T> loaded) {
            Objects.requireNonNull(loader);
            Objects.requireNonNull(loaded);
            this.loader = loader;
            this.loaded = loaded;
        }

        NetworkLoader<T> getLoader() {
            return loader;
        }

        void addDependant(T value, @Nullable DependantNode node, boolean optional) {
            this.loading.add(value);
            this.addDependantNode(value, node, optional);
        }

        abstract void addDependantNode(T value, @Nullable DependantNode node, boolean optional);

        public boolean isLoaded(Set<T> set) {
            return this.loaded.containsAll(set);
        }

        boolean isLoaded(T value) {
            return this.loaded.contains(value);
        }

        void removeId(T value) {
            this.valueDependants.remove(value);
            this.loading.remove(value);
        }

        Set<T> getFreeIds() {
            Set<T> set = new HashSet<>();
            for (Map.Entry<T, DependantNode> entry : this.valueDependants.entrySet()) {
                if (entry.getValue().isFree()) {
                    set.add(entry.getKey());
                }
            }
            return set;
        }

        boolean hasFreeIds() {
            for (Map.Entry<T, DependantNode> entry : this.valueDependants.entrySet()) {
                if (entry.getValue().isFree()) {
                    return true;
                }
            }
            return false;
        }

        DependantNode getIdDependant(T value) {
            return this.valueDependants.get(value);
        }

        boolean isLoading(T value) {
            return this.loading.contains(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoaderManagerImpl<?> that = (LoaderManagerImpl<?>) o;

            return loader.equals(that.loader);
        }

        @Override
        public int hashCode() {
            return loader.hashCode();
        }
    }

    private static class StringLoaderManager extends LoaderManagerImpl<String> {
        private StringLoaderManager(NetworkLoader<String> loader, Set<String> loaded) {
            super(loader, loaded);
        }

        @Override
        public void addDependantNode(String value, @Nullable DependantNode node, boolean optional) {
            DependantNode parentNode = this.valueDependants.computeIfAbsent(value, DependantNode::new);

            if (node != null) {
                parentNode.addChild(node, optional);
            }
        }
    }

    private static class IntLoaderManager extends LoaderManagerImpl<Integer> {
        private IntLoaderManager(NetworkLoader<Integer> loader, Set<Integer> loaded) {
            super(loader, loaded);
        }

        @Override
        public void addDependantNode(Integer value, @Nullable DependantNode node, boolean optional) {
            DependantNode parentNode = this.valueDependants.computeIfAbsent(value, DependantNode::new);

            if (node != null) {
                parentNode.addChild(node, optional);
            }
        }
    }
}
