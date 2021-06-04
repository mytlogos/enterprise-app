package com.mytlogos.enterprise.tools

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.MediumType
import java.io.File
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

object FileTools {
    private const val minMBSpaceAvailable = 150

    /**
     * Copied from
     * [
 * How to convert byte size into human readable format in java?
](https://stackoverflow.com/a/3758880/9492864) *
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    /**
     * With access to internal app dirs
     */
    fun getTextContentTool(application: Application): TextContentTool {
        return TextContentTool(getInternalBookDir(application), getExternalBookDir(application))
    }

    /**
     * Without access to internal app dirs
     */
    @JvmStatic
    val textContentTool: TextContentTool
        get() = TextContentTool(null, null)

    /**
     * With access to internal app dirs
     */
    fun getAudioContentTool(application: Application): AudioContentTool {
        return AudioContentTool(getInternalAudioDir(application), getExternalAudioDir(application))
    }

    /**
     * Without access to internal app dirs
     */
    val audioContentTool: AudioContentTool
        get() = AudioContentTool(null, null)

    /**
     * With access to internal app dirs
     */
    fun getVideoContentTool(application: Application): VideoContentTool {
        return VideoContentTool(getInternalVideoDir(application), getExternalVideoDir(application))
    }

    /**
     * Without access to internal app dirs
     */
    val videoContentTool: VideoContentTool
        get() = VideoContentTool(null, null)

    /**
     * With access to internal app dirs
     */
    fun getImageContentTool(application: Application): ImageContentTool {
        return ImageContentTool(getInternalImageDir(application),
            getExternalImageDir(application),
            getInstance(application))
    }

    /**
     * Without access to internal app dirs
     */
    val imageContentTool: ImageContentTool
        get() = ImageContentTool(null, null, instance)
    val isImageContentSupported: Boolean
        get() = ImageContentTool(null, null, null).isSupported
    val isVideoContentSupported: Boolean
        get() = ImageContentTool(null, null, null).isSupported
    val isTextContentSupported: Boolean
        get() = ImageContentTool(null, null, null).isSupported
    val isAudioContentSupported: Boolean
        get() = ImageContentTool(null, null, null).isSupported

    fun getExternalBookDir(application: Application): File? {
        return if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            null
        } else createBookDirectory(application.getExternalFilesDir(null))
    }

    fun getInternalBookDir(application: Application): File? {
        return createBookDirectory(application.filesDir)
    }

    fun getInternalAudioDir(application: Application): File? {
        return createAudioDirectory(getInternalAppDir(application))
    }

    fun getExternalAudioDir(application: Application): File? {
        val dir = getExternalAppDir(application) ?: return null
        return createAudioDirectory(dir)
    }

    fun getExternalImageDir(application: Application): File? {
        val dir = getExternalAppDir(application) ?: return null
        return createImageDirectory(dir)
    }

    fun getInternalImageDir(application: Application): File? {
        return createImageDirectory(getInternalAppDir(application))
    }

    fun getExternalVideoDir(application: Application): File? {
        val dir = getExternalAppDir(application) ?: return null
        return createVideoDirectory(dir)
    }

    fun getInternalVideoDir(application: Application): File? {
        return createVideoDirectory(getInternalAppDir(application))
    }

    fun getExternalAppDir(application: Application): File? {
        return if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            null
        } else application.getExternalFilesDir(null)
    }

    fun getInternalAppDir(application: Application): File {
        return application.filesDir
    }

    private fun createBookDirectory(filesDir: File): File? {
        return createDir(filesDir, "Enterprise Books")
    }

    private fun createAudioDirectory(filesDir: File): File? {
        return createDir(filesDir, "Enterprise Audios")
    }

    private fun createVideoDirectory(filesDir: File): File? {
        return createDir(filesDir, "Enterprise Videos")
    }

    private fun createImageDirectory(filesDir: File): File? {
        return createDir(filesDir, "Enterprise Images")
    }

    private fun createDir(filesDir: File?, name: String): File? {
        if (filesDir == null) {
            return null
        }
        val file = File(filesDir, name)
        return if (!file.exists()) {
            // TODO: 13.08.2019 cannot create dir on external storage
            if (file.mkdirs()) {
                file
            } else {
                null
            }
        } else file
    }

    @JvmStatic
    fun getSupportedContentTools(application: Application): Set<ContentTool> {
        val tools: MutableSet<ContentTool> = HashSet()
        var tool: ContentTool = getImageContentTool(application)
        if (tool.isSupported) {
            tools.add(tool)
        }
        tool = getAudioContentTool(application)
        if (tool.isSupported) {
            tools.add(tool)
        }
        tool = getTextContentTool(application)
        if (tool.isSupported) {
            tools.add(tool)
        }
        tool = getVideoContentTool(application)
        if (tool.isSupported) {
            tools.add(tool)
        }
        return tools
    }

    @JvmStatic
    fun getContentTool(mediumType: Int, application: Application): ContentTool {
        return when (mediumType) {
            MediumType.TEXT -> getTextContentTool(application)
            MediumType.IMAGE -> getImageContentTool(application)
            MediumType.VIDEO -> getVideoContentTool(application)
            MediumType.AUDIO -> getAudioContentTool(application)
            else -> throw IllegalArgumentException("invalid medium type: $mediumType")
        }
    }

    fun writeInternal(application: Application): Boolean {
        return isWriteable(application, getInternalAppDir(application))
    }

    fun writeExternal(application: Application): Boolean {
        return isWriteable(application, getExternalAppDir(application))
    }

    fun writable(application: Application): Boolean {
        return writeExternal(application) || writeInternal(application)
    }

    private fun isWriteable(application: Application, dir: File?): Boolean {
        return dir != null && getFreeMBSpace(dir) >= minMBSpaceAvailable
    }

    @SuppressLint("UsableSpace")
    fun getFreeMBSpace(file: File): Long {
        return file.usableSpace / (1024L * 1024L)
    }
}