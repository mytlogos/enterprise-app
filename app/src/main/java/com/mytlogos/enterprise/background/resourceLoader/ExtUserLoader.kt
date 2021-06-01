package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientExternalUser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class ExtUserLoader implements NetworkLoader<String> {

    private LoadWorker loadWorker;

    ExtUserLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<String> toLoad) {
        return this.loadWorker.repository.loadExternalUserAsync(toLoad)
                .thenAccept(this::process);
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<String> toLoad) {
        List<ClientExternalUser> externalUsers = this.loadWorker.repository.loadExternalUserSync(toLoad);

        if (externalUsers != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
            LoadWorkGenerator.FilteredExternalUser user = generator.filterExternalUsers(externalUsers);
            this.loadWorker.persister.persist(user);
            return this.loadWorker.generator.generateExternalUsersDependant(user);
        }
        return Collections.emptyList();
    }

    private void process(List<ClientExternalUser> externalUsers) {
        if (externalUsers != null) {
            loadWorker.persister.persistExternalUsers(externalUsers);
        }
    }


    @Override
    public Set<String> getLoadedSet() {
        return loadWorker.loadedData.getExternalUser();
    }
}
