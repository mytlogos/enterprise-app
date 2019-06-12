package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientExternalUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyExtUserLoader extends ExtUserLoader {

    private final Collection<ClientExternalUser> users;
    private LoadWorker loadWorker;

    public DummyExtUserLoader(Collection<ClientExternalUser> users) {
        super(null);
        this.users = users;
    }

    public void setLoadWorker(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<String> toLoad) {
        return CompletableFuture.runAsync(() -> this.loadItemsSync(toLoad));
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<String> toLoad) {
        Collection<ClientExternalUser> loadedUsers = new ArrayList<>(toLoad.size());

        for (String integer : toLoad) {
            for (ClientExternalUser user : users) {
                if (user.getUuid().equals(integer)) {
                    loadedUsers.add(user);
                    break;
                }
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
        LoadWorkGenerator.FilteredExternalUser filteredExUsers = generator.filterExternalUsers(loadedUsers);
        this.loadWorker.persister.persist(filteredExUsers);
        return this.loadWorker.generator.generateExternalUsersDependant(filteredExUsers);

    }

    @Override
    public Set<String> getLoadedSet() {
        return loadWorker.loadedData.getExternalUser();
    }
}
