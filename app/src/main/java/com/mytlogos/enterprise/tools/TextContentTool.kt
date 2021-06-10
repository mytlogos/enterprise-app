package com.mytlogos.enterprise.tools

import android.annotation.SuppressLint
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.TEXT
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import nl.siegmann.epublib.epub.EpubWriter
import nl.siegmann.epublib.service.MediatypeService
import java.io.*
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.collections.HashMap

class TextContentTool internal constructor(internalContentDir: File?, externalContentDir: File?) :
    ContentTool(internalContentDir, externalContentDir) {
    private var externalTexts: MutableMap<Int, File>? = null
    private var internalTexts: MutableMap<Int, File>? = null
    private var episodePaths: Map<Int, String>? = null

    override val medium = TEXT

    override fun isContentMedium(file: File): Boolean {
        return file.name.matches(Regex("\\d+\\.epub"))
    }

    override val isSupported: Boolean
        get() = true

    override fun removeMediaEpisodes(episodeIds: Set<Int>, internalFile: String?) {
        if (internalFile == null || internalFile.isEmpty()) {
            return
        }
        val episodePaths = getEpisodePaths(internalFile)
        episodePaths.keys.retainAll(episodeIds)
        // cant use filesystem, it needs minSdk >= 26, so fall back to rename
        // and copy all except the ones which shall be removed
        val file = File(internalFile)
        val originalName = file.name
        val src = File(file.parent, "(1)$originalName")
        if (!file.renameTo(src)) {
            System.err.println("could not rename file " + file.absolutePath)
            return
        }
        try {
            ZipOutputStream(FileOutputStream(file)).use { stream ->
                ZipFile(src).use { source ->
                    val buffer = ByteArray(2048)
                    for (entry in Collections.list(source.entries())) {
                        if (episodePaths.containsValue(entry.name)) {
                            continue
                        }
                        val newEntry = ZipEntry(entry.name)
                        stream.putNextEntry(newEntry)
                        source.getInputStream(entry).use { `in` ->
                            while (`in`.available() > 0) {
                                val read = `in`.read(buffer)
                                if (read > 0) {
                                    stream.write(buffer, 0, read)
                                }
                            }
                        }
                        stream.closeEntry()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (src.exists() && !src.delete()) {
            System.err.println("could not delete old epub: " + src.absolutePath)
        }
    }

    override val mediumContainerPattern: Pattern
        get() = Pattern.compile("^(\\d+)\\.epub$")
    override val mediumContainerPatternGroup: Int
        get() = 1

    /**
     * Retrieves the EpisodeIds and their Paths via Resource Id
     * of the Book.
     */
    private fun getEpisodePathViaBook(mediumPath: String): MutableMap<Int, String> {
        val book = loadBook(File(mediumPath))

        val episodePaths = HashMap<Int, String>()

        for (tocReference in book.tableOfContents.tocReferences) {
            val episodeId = tocReference.resourceId.toIntOrNull()

            if (episodeId != null) {
                episodePaths[episodeId] = tocReference.completeHref
            }
        }
        return episodePaths
    }

    override fun getEpisodePaths(mediumPath: String?): MutableMap<Int, String> {
        require(!(mediumPath == null || !mediumPath.endsWith(".epub"))) {
            "'$mediumPath' is not a epub"
        }
        try {
            ZipFile(mediumPath).use { file ->
                val markerFile = "content.opf"
                val entries = file.entries()
                val chapterFiles: MutableList<String> = ArrayList()
                var folder: String? = null

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".xhtml")) {
                        chapterFiles.add(entry.name)
                    }
                    val index = entry.name.indexOf(markerFile)
                    if (index > 0) {
                        folder = entry.name.substring(0, index)
                    }
                }

                if (folder == null) {
                    return mutableMapOf()
                }

                @SuppressLint("UseSparseArrays")
                val episodeMap: MutableMap<Int, String> = HashMap()

                for (chapterFile in chapterFiles) {
                    if (!chapterFile.startsWith(folder)) {
                        continue
                    }
                    // read chapter stream till it matches the opening tag of the body
                    // with the episodeId as its id attribute
                    file.getInputStream(file.getEntry(chapterFile)).use { inputStream ->
                        val buffer = ByteArray(128)
                        var readInput = ""
                        val pattern = Pattern.compile("<body id=\"(\\d+)\">")
                        var read = inputStream.read(buffer)

                        while (read != -1) {
                            readInput += String(buffer)
                            val matcher = pattern.matcher(readInput)
                            if (matcher.find()) {
                                val group = matcher.group(1)
                                val episodeId = group.toInt()
                                episodeMap[episodeId] = chapterFile
                                break
                            }
                            read = inputStream.read(buffer)
                        }
                    }
                    if (!episodeMap.values.contains(chapterFile)) {
                        println("no id found for $chapterFile")
                    }
                }
                return episodeMap
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return mutableMapOf()
        }
    }

    override fun getItemPath(mediumId: Int, dir: File): String? {
        for (file in dir.listFiles()) {
            if (file.name.matches(Regex("$mediumId\\.epub"))) {
                return file.absolutePath
            }
        }
        return null
    }

    @Throws(IOException::class)
    override fun saveContent(episodes: Collection<ClientDownloadedEpisode>, mediumId: Int) {
        if (externalTexts == null) {
            externalTexts = getItemContainers(true)
        }
        if (internalTexts == null) {
            internalTexts = getItemContainers(false)
        }
        val book: Book
        val file: File?
        val writeExternal = writeExternal()
        val writeInternal = writeInternal()
        if (writeExternal && externalTexts!!.containsKey(mediumId)) {
            file = externalTexts!![mediumId]
            book = loadBook(file)
        } else if (writeInternal && internalTexts!!.containsKey(mediumId)) {
            file = internalTexts!![mediumId]
            book = loadBook(file)
        } else {
            val fileName = "$mediumId.epub"
            val dir: File = when {
                writeExternal -> externalContentDir!!
                writeInternal -> internalContentDir!!
                else -> throw IOException("Out of Storage Space: Less than $minMBSpaceAvailable  MB available")
            }
            file = File(dir, fileName)
            book = Book()
        }
        if (file == null) {
            return
        }
        for (episode in episodes) {
            // enable episode identification via Resource
            val resource = Resource(
                episode.episodeId.toString(),
                toXhtml(episode).toByteArray(),
                episode.episodeId.toString() + ".xhtml",
                MediatypeService.XHTML
            )
            book.addSection(episode.getTitle(), resource)
        }
        EpubWriter().write(book, FileOutputStream(file))
        if (writeExternal) {
            externalTexts!![mediumId] = file
        } else {
            internalTexts!![mediumId] = file
        }
    }

    @Throws(IOException::class)
    private fun loadBook(file: File?): Book {
        return EpubReader().readEpub(FileInputStream(file))
    }

    override fun mergeExternalAndInternalMedium(
        toExternal: Boolean,
        source: File,
        goal: File?,
        toParent: File?,
        mediumId: Int
    ) {
        // TODO: 05.08.2019 implement
    }

    override fun getEpisodeSize(
        value: File,
        episodeId: Int,
        episodePaths: Map<Int, String>?
    ): Long {
        this.episodePaths = episodePaths
        return this.getEpisodeSize(value, episodeId)
    }

    override fun getAverageEpisodeSize(mediumId: Int): Double {
        val path = this.getItemPath(mediumId)
        if (path == null || path.isEmpty()) {
            return 0.0
        }
        try {
            ZipFile(path).use { file ->
                if (episodePaths == null) {
                    episodePaths = getEpisodePaths(path)
                }
                var sum = 0.0
                val values = episodePaths!!.values
                for (entryName in values) {
                    val entry = file.getEntry(entryName) ?: continue
                    sum += entry.compressedSize.toDouble()
                }
                return if (values.isEmpty()) 0.0 else sum / values.size
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0.0
    }

    override fun getEpisodeSize(value: File, episodeId: Int): Long {
        try {
            ZipFile(value).use { file ->
                if (episodePaths == null) {
                    episodePaths = getEpisodePaths(value.absolutePath)
                }
                val entryName = episodePaths!![episodeId] ?: return 0
                val entry = file.getEntry(entryName) ?: return 0
                return entry.compressedSize
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun toXhtml(episode: ClientDownloadedEpisode): String {
        val arrayContent = episode.getContent()
        if (arrayContent.isEmpty()) {
            return ""
        }
        var content = arrayContent[0]
        val titleIndex = content.indexOf(episode.getTitle())
        if (titleIndex < 0 || titleIndex > content.length / 3) {
            content = "<h3>" + episode.getTitle() + "</h3>" + content
        }
        return if (content.matches(Regex("\\s*<html.*>(<head>.*</head>)?<body>.+</body></html>\\s*"))) {
            content
        } else "<html><head></head><body id=\"" + episode.episodeId + "\">" + content + "</body></html>"
    }

    fun openEpisode(zipFileLink: String?, episodeFile: String?): String {
        if (zipFileLink == null || !zipFileLink.endsWith(".epub")) {
            return "Invalid File Link"
        }
        if (episodeFile == null || !episodeFile.endsWith(".xhtml")) {
            return "Invalid Episode Link"
        }
        try {
            ZipFile(zipFileLink).use { file ->
                val entry = file.getEntry(episodeFile) ?: return "Invalid Episode Link"
                val builder = BufferedReader(InputStreamReader(file.getInputStream(entry)))
                    .lines()
                    .collect({ StringBuilder() },
                        { obj: java.lang.StringBuilder, str: String? -> obj.append(str) }) { obj: java.lang.StringBuilder, s: java.lang.StringBuilder? ->
                        obj.append(s)
                    }
                return builder.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Could not open Book"
        }
    }
}