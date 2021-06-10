package com.mytlogos.enterprise.tools

import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.model.AUDIO
import com.mytlogos.enterprise.model.MediumType
import java.io.File
import java.util.regex.Pattern

// TODO: 05.08.2019 implement this class
class AudioContentTool internal constructor(internalContentDir: File?, externalContentDir: File?) :
    ContentTool(internalContentDir, externalContentDir) {
    override val medium = AUDIO

    override fun isContentMedium(file: File): Boolean {
        throw IllegalStateException("Not yet implemented")
    }

    override val isSupported: Boolean
        get() = false

    override fun removeMediaEpisodes(episodeIds: Set<Int>, internalFile: String?) {
        throw IllegalStateException("Not yet implemented")
    }

    override val mediumContainerPattern: Pattern
        get() {
            throw IllegalStateException("Not yet implemented")
        }
    override val mediumContainerPatternGroup: Int
        get() {
            throw IllegalStateException("Not yet implemented")
        }

    override fun getEpisodePaths(mediumPath: String?): Map<Int, String> {
        throw IllegalStateException("Not yet implemented")
    }

    override fun getItemPath(mediumId: Int): String? {
        throw IllegalStateException("Not yet implemented")
    }

    override fun getItemPath(mediumId: Int, dir: File): String? {
        throw IllegalStateException("Not yet implemented")
    }

    override fun saveContent(episodes: Collection<ClientDownloadedEpisode>, mediumId: Int) {
        throw IllegalStateException("Not yet implemented")
    }

    override fun mergeExternalAndInternalMedia(toExternal: Boolean) {
        throw IllegalStateException("Not yet implemented")
    }

    override fun mergeExternalAndInternalMedium(
        toExternal: Boolean,
        source: File,
        goal: File?,
        toParent: File?,
        mediumId: Int
    ) {
        throw IllegalStateException("Not yet implemented")
    }

    override fun getEpisodeSize(value: File, episodeId: Int): Long {
        throw IllegalStateException("Not yet implemented")
    }

    override fun getAverageEpisodeSize(mediumId: Int): Double {
        throw IllegalStateException("Not yet implemented")
    }
}