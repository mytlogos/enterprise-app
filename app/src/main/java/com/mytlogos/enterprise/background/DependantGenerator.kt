package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.resourceLoader.DependencyTask
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*

interface DependantGenerator {
    fun generateReadEpisodesDependant(readEpisodes: FilteredReadEpisodes): Collection<DependencyTask<*>>
    fun generatePartsDependant(parts: FilteredParts): Collection<DependencyTask<*>>
    fun generateEpisodesDependant(episodes: FilteredEpisodes): Collection<DependencyTask<*>>
    fun generateMediaDependant(media: FilteredMedia): MutableCollection<DependencyTask<*>>
    fun generateMediaListsDependant(mediaLists: FilteredMediaList): Collection<DependencyTask<*>>
    fun generateExternalMediaListsDependant(externalMediaLists: FilteredExtMediaList): Collection<DependencyTask<*>>
    fun generateExternalUsersDependant(externalUsers: FilteredExternalUser): Collection<DependencyTask<*>>
}