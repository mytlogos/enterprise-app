package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientExternalUser
import java.util.concurrent.CompletableFuture

open class ExtUserLoader(private val loadWorker: LoadWorker) : NetworkLoader<String> {
    override fun loadItemsAsync(toLoad: Set<String>): CompletableFuture<Void> {
        return loadWorker.repository.loadExternalUserAsync(toLoad)
            .thenAccept { externalUsers: List<ClientExternalUser>? -> process(externalUsers) }
    }

    override fun loadItemsSync(toLoad: Set<String>): Collection<DependencyTask<*>> {
        val externalUsers = loadWorker.repository.loadExternalUserSync(toLoad)
        if (externalUsers != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val user = generator.filterExternalUsers(externalUsers)
            loadWorker.persister.persist(user)
            return loadWorker.generator!!.generateExternalUsersDependant(user)
        }
        return emptyList()
    }

    private fun process(externalUsers: List<ClientExternalUser>?) {
        if (externalUsers != null) {
            loadWorker.persister.persistExternalUsers(externalUsers)
        }
    }

    override val loadedSet: Set<String>
        get() = loadWorker.loadedData.externalUser
}