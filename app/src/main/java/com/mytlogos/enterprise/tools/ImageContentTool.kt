package com.mytlogos.enterprise.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mytlogos.enterprise.background.Repository
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.model.ChapterPage
import com.mytlogos.enterprise.model.IMAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

class ImageContentTool internal constructor(
    internalContentDir: File?,
    externalContentDir: File?,
    private val repository: Repository?
) : ContentTool(internalContentDir, externalContentDir) {
    private var internalImageMedia: Map<Int, File>? = null
    private var externalImageMedia: Map<Int, File>? = null

    override val medium = IMAGE

    override fun isContentMedium(file: File): Boolean {
        return file.name.matches(Regex("\\d+")) && file.isDirectory
    }

    override val isSupported: Boolean
        get() = true

    override fun removeMediaEpisodes(episodeIds: Set<Int>, internalFile: String?) {
        if (internalFile == null) {
            return
        }
        val file = File(internalFile)
        val prefixes: MutableSet<String> = HashSet()
        for (episodeId in episodeIds) {
            prefixes.add("$episodeId-")
        }
        for (episodePath in file.listFiles()) {
            val name = episodePath.name

            for (prefix in prefixes) {
                if (!name.startsWith(prefix)) {
                    continue
                }
                try {
                    if (episodePath.exists() && !episodePath.delete()) {
                        val idSubString = prefix.substring(0, prefix.indexOf("-"))
                        print("could not delete episode $idSubString totally, deleting: '${file.name}' failed")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    override val mediumContainerPattern: Pattern
        get() = Pattern.compile("^(\\d+)$")

    override val mediumContainerPatternGroup: Int
        get() = 1

    override fun getEpisodePaths(mediumPath: String?): Map<Int, String> {
        val file = File(mediumPath)
        if (!file.exists() || !file.isDirectory) {
            return emptyMap()
        }
        val pagePattern = Pattern.compile("^(\\d+)-\\d+\\.(png|jpg)$")
        val firstPageEpisodes: MutableMap<Int, String> = HashMap()

        for (episodePath in file.list()) {
            val matcher = pagePattern.matcher(episodePath)
            if (!matcher.matches()) {
                continue
            }
            val episode = matcher.group(1)
            val episodeId = episode.toInt()

            // look for available pages
            if (!firstPageEpisodes.containsKey(episodeId)) {
                firstPageEpisodes[episodeId] = episodePath
            }
        }
        return firstPageEpisodes
    }

    override fun getItemPath(mediumId: Int, dir: File): String? {
        for (file in dir.listFiles()) {
            if (mediumId.toString() + "" == file.name && file.isDirectory) {
                return file.absolutePath
            }
        }
        return null
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class)
    override suspend fun saveContent(episodes: Collection<ClientDownloadedEpisode>, mediumId: Int) = withContext(Dispatchers.IO) {
        if (externalImageMedia == null) {
            externalImageMedia = getItemContainers(true)
        }
        if (internalImageMedia == null) {
            internalImageMedia = getItemContainers(false)
        }
        val writeExternal = writeExternal()
        val writeInternal = writeInternal()
        val file: File?

        if (writeExternal && externalImageMedia!!.containsKey(mediumId)) {
            file = externalImageMedia!![mediumId]
        } else if (writeInternal && internalImageMedia!!.containsKey(mediumId)) {
            file = internalImageMedia!![mediumId]
        } else {
            val dir: File = when {
                writeExternal -> externalContentDir!!
                writeInternal -> internalContentDir!!
                else -> throw NotEnoughSpaceException("Out of Storage Space: Less than $minMBSpaceAvailable MB available")
            }
            file = File(dir, mediumId.toString() + "")
            if (!file.exists() && !file.mkdir()) {
                throw IOException("could not create image medium directory")
            }
        }
        for (episode in episodes) {
            val content = episode.getContent()
            if (content.isEmpty()) {
                continue
            }
            val links = repository!!.getReleaseLinks(episode.episodeId)
            val writtenFiles: MutableList<File> = ArrayList()
            downloadPage(content, 0, file, episode.episodeId, links, writtenFiles)
            val firstImage = writtenFiles[0]
            val estimatedByteSize = firstImage.length() * content.size
            if (!writeable(file, estimatedByteSize)) {
                if (firstImage.exists() && !firstImage.delete()) {
                    println("could not delete image: " + firstImage.absolutePath)
                }
                throw NotEnoughSpaceException()
            }
            val futures: MutableList<CompletableFuture<Void>> = ArrayList()
            var page = 1
            val contentLength = content.size
            while (page < contentLength) {
                val tempPage = page
                futures.add(CompletableFuture.runAsync {
                    try {
                        downloadPage(content,
                            tempPage,
                            file,
                            episode.episodeId,
                            links,
                            writtenFiles)
                    } catch (e: IOException) {
                        throw IllegalStateException(e)
                    }
                })
                page++
            }
            try {
                CompletableFuture
                    .allOf(*futures.toTypedArray<CompletableFuture<*>>())
                    .whenComplete { _: Void?, throwable: Throwable? ->
                        if (throwable == null) {
                            return@whenComplete
                        }
                        // first exception is completionException, then the illegalStateException
                        // followed by notEnoughSpaceException
                        var cause = throwable.cause
                        if (cause != null && cause.cause is NotEnoughSpaceException) {
                            cause = cause.cause
                            for (writtenFile in writtenFiles) {
                                if (writtenFile.exists() && !writtenFile.delete()) {
                                    println("could not delete image: " + writtenFile.absolutePath)
                                }
                            }
                            throw RuntimeException(cause!!.cause)
                        }
                        throw RuntimeException(throwable)
                    }
                    .get()
            } catch (e: ExecutionException) {
                when (e.cause) {
                    is NotEnoughSpaceException -> throw NotEnoughSpaceException()
                    is IOException -> throw IOException(e)
                    else -> e.printStackTrace()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun downloadPage(
        content: Array<String>,
        page: Int,
        file: File?,
        episodeId: Int,
        links: List<String>,
        writtenFiles: MutableList<File>
    ) {
        val link = content[page]
        val pageLinkDomain = getDomain(link)
        if (pageLinkDomain == null) {
            System.err.println("invalid url: '$link'")
            return
        }
        var referer: String? = null
        for (releaseUrl in links) {
            if (getDomain(link) == pageLinkDomain) {
                referer = releaseUrl
                break
            }
        }
        if (referer == null || referer.isEmpty()) {
            // we need a referrer for sites like mangahasu
            return
        }
        // TODO: 06.08.2019 instead of continuing maybe create an empty image file to signal
        //  the reader that this page is explicitly missing?
        if (link.isEmpty()) {
            System.err.println("got an invalid link")
            return
        }
        val imageFormat: String = when {
            link.lowercase(Locale.getDefault()).endsWith(".png") -> {
                "png"
            }
            link.lowercase(Locale.getDefault()).endsWith(".jpg") -> {
                "jpg"
            }
            else -> {
                System.err.println("got unsupported/unwanted image format: $link")
                return
            }
        }
        try {
            val url = URL(link)
            var httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.doInput = true
            httpURLConnection.setRequestProperty("Referer", referer)
            httpURLConnection.connect()
            var responseCode = httpURLConnection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.doInput = true
                httpURLConnection.connect()
                responseCode = httpURLConnection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("could not get resource successfully: $link")
                }
            }
            BufferedInputStream(httpURLConnection.inputStream).use { `in` ->
                val pageName = String.format("%s-%s.%s", episodeId, page + 1, imageFormat)
                val image = File(file, pageName)
                writtenFiles.add(image)
                saveImageStream(`in`, image)
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        // if the estimation was too low
        // and subsequent images took more space than expected
        // check if it can still write after this
        if (!this.writeable()) {
            throw NotEnoughSpaceException()
        }
    }

    @Throws(IOException::class)
    private fun saveImageStream(`in`: InputStream, image: File) {
        BufferedOutputStream(FileOutputStream(image)).use { outputStream ->
            var ch: Int
            while (`in`.read().also { ch = it } != -1) {
                outputStream.write(ch)
            }
        }
    }

    @Throws(IOException::class)
    private fun saveImageBitmap(`in`: InputStream, image: File) {
        val bitmap = BitmapFactory.decodeStream(`in`)
        BufferedOutputStream(FileOutputStream(image)).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            outputStream.flush()
        }
    }

    override fun mergeExternalAndInternalMedium(
        toExternal: Boolean,
        source: File,
        goal: File?,
        toParent: File?,
        mediumId: Int
    ) {
        var destinationFile = goal

        if (destinationFile == null) {
            destinationFile = File(toParent, mediumId.toString() + "")
            if (!destinationFile.mkdirs()) {
                System.err.println("could not create medium container")
                return
            }
        }
        val paths = getEpisodePagePaths(source.absolutePath)
        for ((_, value) in paths) {
            val files: MutableSet<File> = HashSet()
            var neededSpace: Long = 0
            for (page in value) {
                val file = File(page.path)
                files.add(file)
                neededSpace += file.length()
            }
            if (!writeable(destinationFile, neededSpace)) {
                continue
            }
            var successFull = false
            try {
                for (file in files) {
                    copyFile(file, File(destinationFile, file.name))
                }
                successFull = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (!successFull) {
                continue
            }
            for (file in files) {
                if (file.exists() && !file.delete()) {
                    System.err.println("could not delete file")
                }
            }
        }
    }

    override fun getEpisodeSize(value: File, episodeId: Int): Long {
        val prefix = "$episodeId-"
        var size: Long = 0
        for (file in value.listFiles()) {
            if (!file.name.startsWith(prefix)) {
                continue
            }
            size += file.length()
        }
        return size
    }

    override fun getAverageEpisodeSize(mediumId: Int): Double {
        val itemPath = this.getItemPath(mediumId)
        var sum = 0.0
        val files = File(itemPath).listFiles()
        for (file in files) {
            sum += file.length().toDouble()
        }
        return if (files.isEmpty()) 0.0 else sum / files.size
    }

    fun getEpisodePagePaths(mediumDir: String?): Map<Int, MutableSet<ChapterPage>> {
        val file = File(mediumDir)
        if (!file.exists() || !file.isDirectory) {
            return emptyMap()
        }
        val pagePattern = Pattern.compile("^(\\d+)-(\\d+)\\.(png|jpg)$")
        val episodePages: MutableMap<Int, MutableSet<ChapterPage>> = HashMap()

        for (episodePath in file.list()) {
            val matcher = pagePattern.matcher(episodePath)

            if (!matcher.matches()) {
                continue
            }

            val episodeIdString = matcher.group(1)
            val pageString = matcher.group(2)
            val episodeId = episodeIdString.toInt()
            val page = pageString.toInt()
            val absolutePath = File(file, episodePath).absolutePath

            episodePages
                .computeIfAbsent(episodeId) { HashSet() }
                .add(ChapterPage(episodeId, page, absolutePath))
        }
        return episodePages
    }
}