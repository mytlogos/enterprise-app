package com.mytlogos.enterprise.tools

import android.annotation.SuppressLint
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

abstract class ContentTool internal constructor(
    val internalContentDir: File?,
    val externalContentDir: File?
) {
    val minMBSpaceAvailable = 150
    val mediaPaths: List<String>
        get() {
            val books: MutableList<String> = ArrayList()
            if (externalContentDir != null) {
                books.addAll(getMediaPaths(externalContentDir))
            }
            if (internalContentDir != null) {
                books.addAll(getMediaPaths(internalContentDir))
            }
            return books
        }
    abstract val medium: Int
    private fun getMediaPaths(dir: File): List<String> {
        val imageMedia: MutableList<String> = ArrayList()
        for (file in dir.listFiles()) {
            if (isContentMedium(file)) {
                imageMedia.add(file.absolutePath)
            }
        }
        return imageMedia
    }

    abstract fun isContentMedium(file: File): Boolean

    @SuppressLint("UseSparseArrays")
    fun getItemContainers(externalSpace: Boolean): MutableMap<Int, File> {
        // return an empty hashmap if no content dir is available
        val file = (if (externalSpace) externalContentDir else internalContentDir)
            ?: return HashMap()

        val pattern = mediumContainerPattern
        val files =
            file.listFiles { _: File?, name: String? -> Pattern.matches(pattern.pattern(), name) }

        val mediumIdFileMap: MutableMap<Int, File> = HashMap()

        for (bookFile in files) {
            val matcher = pattern.matcher(bookFile.name)
            if (!matcher.matches()) {
                continue
            }
            val mediumIdString = matcher.group(mediumContainerPatternGroup)

            val mediumId: Int = try {
                mediumIdString.toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                continue
            }
            mediumIdFileMap[mediumId] = bookFile
        }
        return mediumIdFileMap
    }

    fun removeMediaEpisodes(mediumId: Int, episodeIds: Set<Int>) {
        if (externalContentDir != null) {
            val externalFile = getItemPath(mediumId, externalContentDir)
            if (externalFile != null) {
                this.removeMediaEpisodes(episodeIds, externalFile)
            }
        }
        if (internalContentDir != null) {
            val internalFile = getItemPath(mediumId, internalContentDir)
            if (internalFile != null) {
                this.removeMediaEpisodes(episodeIds, internalFile)
            }
        }
    }

    abstract val isSupported: Boolean
    abstract fun removeMediaEpisodes(episodeIds: Set<Int>, internalFile: String?)
    abstract val mediumContainerPattern: Pattern
    abstract val mediumContainerPatternGroup: Int
    abstract fun getEpisodePaths(mediumPath: String?): Map<Int, String>
    open fun getItemPath(mediumId: Int): String? {
        var bookZipFile: String? = null
        if (externalContentDir != null) {
            bookZipFile = getItemPath(mediumId, externalContentDir)
        }
        if (bookZipFile == null && internalContentDir != null) {
            bookZipFile = getItemPath(mediumId, internalContentDir)
        }
        return bookZipFile ?: ""
    }

    @SuppressLint("UsableSpace")
    fun writeExternal(): Boolean {
        return externalContentDir != null && externalContentDir.usableSpace >= minByteSpaceAvailable()
    }

    @SuppressLint("UsableSpace")
    fun writeExternal(toWriteBytes: Long): Boolean {
        return externalContentDir != null && externalContentDir.usableSpace - toWriteBytes >= minByteSpaceAvailable()
    }

    @SuppressLint("UsableSpace")
    fun writeInternal(): Boolean {
        return internalContentDir != null && internalContentDir.usableSpace >= minByteSpaceAvailable()
    }

    @SuppressLint("UsableSpace")
    fun writeInternal(toWriteBytes: Long): Boolean {
        return internalContentDir != null && internalContentDir.usableSpace - toWriteBytes >= minByteSpaceAvailable()
    }

    fun writeable(): Boolean {
        return this.writeExternal() || this.writeInternal()
    }

    fun writeable(toWriteBytes: Long): Boolean {
        return writeExternal(toWriteBytes) || writeInternal(toWriteBytes)
    }

    @SuppressLint("UsableSpace")
    fun writeable(file: File?, toWriteBytes: Long): Boolean {
        return file != null && file.usableSpace - toWriteBytes >= minByteSpaceAvailable()
    }

    private fun minByteSpaceAvailable(): Long {
        return (minMBSpaceAvailable * 1024 * 1024).toLong()
    }

    abstract fun getItemPath(mediumId: Int, dir: File): String?

    @Throws(IOException::class)
    abstract fun saveContent(episodes: Collection<ClientDownloadedEpisode>, mediumId: Int)

    @Synchronized
    open fun mergeExternalAndInternalMedia(toExternal: Boolean) {
        val internalContainers = getItemContainers(false)
        val externalContainers = getItemContainers(true)
        val sourceContainers = if (toExternal) internalContainers else externalContainers
        val toContainers = if (toExternal) externalContainers else internalContainers
        val toParent = if (toExternal) externalContentDir else internalContentDir
        for ((key, value) in sourceContainers) {
            val file = toContainers[key]
            mergeExternalAndInternalMedium(toExternal, value, file, toParent, key)
        }
    }

    fun mergeIfNecessary() {
        if (!isSupported) {
            return
        }
        if (this.writeInternal() && !this.writeExternal()) {
            mergeExternalAndInternalMedia(false)
        } else if (!this.writeInternal() && this.writeExternal()) {
            mergeExternalAndInternalMedia(true)
        }
    }

    abstract fun mergeExternalAndInternalMedium(
        toExternal: Boolean,
        source: File,
        goal: File?,
        toParent: File?,
        mediumId: Int
    )

    abstract fun getEpisodeSize(value: File, episodeId: Int): Long
    fun removeMedia(id: Int) {
        if (externalContentDir != null) {
            val externalFile = getItemPath(id, externalContentDir)
            if (externalFile != null && !File(externalFile).delete()) {
                System.err.println("could not delete file: $externalFile")
            }
        }
        if (internalContentDir != null) {
            val internalFile = getItemPath(id, internalContentDir)
            if (internalFile != null && !File(internalFile).delete()) {
                System.err.println("could not delete file: $internalFile")
            }
        }
    }

    fun removeAll() {
        for (file in internalContentDir!!.listFiles()) {
            if (file.isDirectory) {
                deleteDir(file)
            }
            if (file.exists() && !file.delete()) {
                System.err.println("could not delete file: " + file.absolutePath)
            }
        }
        for (file in externalContentDir!!.listFiles()) {
            if (file.isDirectory) {
                deleteDir(file)
            }
            if (file.exists() && !file.delete()) {
                System.err.println("could not delete file: " + file.absolutePath)
            }
        }
    }

    private fun deleteDir(file: File) {
        for (content in file.listFiles()) {
            if (content.isDirectory) {
                deleteDir(content)
            }
            if (content.exists() && !content.delete()) {
                break
            }
        }
    }

    open fun getEpisodeSize(value: File, episodeId: Int, episodePaths: Map<Int, String>?): Long {
        return this.getEpisodeSize(value, episodeId)
    }

    abstract fun getAverageEpisodeSize(mediumId: Int): Double

    companion object {
        /**
         * Copied from [
 * How to move/rename file from internal app storage to external storage on Android?
](https://stackoverflow.com/a/4770586/9492864) *
         */
        @Throws(IOException::class)
        fun copyFile(src: File?, dst: File?) {
            FileInputStream(src).channel.use { inChannel ->
                FileOutputStream(dst).channel.use { outChannel ->
                    inChannel.transferTo(0,
                        inChannel.size(),
                        outChannel)
                }
            }
        }
    }
}