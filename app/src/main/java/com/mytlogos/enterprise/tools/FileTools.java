package com.mytlogos.enterprise.tools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.MediumType;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileTools {
    private static final int minMBSpaceAvailable = 150;


    /**
     * Copied from
     * <a href="https://stackoverflow.com/a/3758880/9492864">
     * How to convert byte size into human readable format in java?
     * </a>
     */
    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * With access to internal app dirs
     */
    public static TextContentTool getTextContentTool(Application application) {
        return new TextContentTool(getInternalBookDir(application), getExternalBookDir(application));
    }

    /**
     * Without access to internal app dirs
     */
    public static TextContentTool getTextContentTool() {
        return new TextContentTool(null, null);
    }

    /**
     * With access to internal app dirs
     */
    public static AudioContentTool getAudioContentTool(Application application) {
        return new AudioContentTool(getInternalAudioDir(application), getExternalAudioDir(application));
    }

    /**
     * Without access to internal app dirs
     */
    public static AudioContentTool getAudioContentTool() {
        return new AudioContentTool(null, null);
    }

    /**
     * With access to internal app dirs
     */
    public static VideoContentTool getVideoContentTool(Application application) {
        return new VideoContentTool(getInternalVideoDir(application), getExternalVideoDir(application));
    }

    /**
     * Without access to internal app dirs
     */
    public static VideoContentTool getVideoContentTool() {
        return new VideoContentTool(null, null);
    }

    /**
     * With access to internal app dirs
     */
    public static ImageContentTool getImageContentTool(Application application) {
        return new ImageContentTool(getInternalImageDir(application), getExternalImageDir(application), RepositoryImpl.getInstance(application));
    }

    /**
     * Without access to internal app dirs
     */
    public static ImageContentTool getImageContentTool() {
        return new ImageContentTool(null, null, RepositoryImpl.getInstance());
    }

    public static boolean isImageContentSupported() {
        return new ImageContentTool(null, null, null).isSupported();
    }

    public static boolean isVideoContentSupported() {
        return new ImageContentTool(null, null, null).isSupported();
    }

    public static boolean isTextContentSupported() {
        return new ImageContentTool(null, null, null).isSupported();
    }

    public static boolean isAudioContentSupported() {
        return new ImageContentTool(null, null, null).isSupported();
    }

    @Nullable
    public static File getExternalBookDir(Application application) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }
        return createBookDirectory(application.getExternalFilesDir(null));
    }

    @Nullable
    public static File getInternalBookDir(Application application) {
        return createBookDirectory(application.getFilesDir());
    }

    @Nullable
    public static File getInternalAudioDir(Application application) {
        return createAudioDirectory(getInternalAppDir(application));
    }

    @Nullable
    public static File getExternalAudioDir(Application application) {
        File dir = getExternalAppDir(application);
        if (dir == null) {
            return null;
        }
        return createAudioDirectory(dir);
    }

    @Nullable
    public static File getExternalImageDir(Application application) {
        File dir = getExternalAppDir(application);
        if (dir == null) {
            return null;
        }
        return createImageDirectory(dir);
    }

    @Nullable
    public static File getInternalImageDir(Application application) {
        return createImageDirectory(getInternalAppDir(application));
    }

    @Nullable
    public static File getExternalVideoDir(Application application) {
        File dir = getExternalAppDir(application);
        if (dir == null) {
            return null;
        }
        return createVideoDirectory(dir);
    }

    @Nullable
    public static File getInternalVideoDir(Application application) {
        return createVideoDirectory(getInternalAppDir(application));
    }

    @Nullable
    public static File getExternalAppDir(Application application) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }
        return application.getExternalFilesDir(null);
    }

    @NonNull
    public static File getInternalAppDir(Application application) {
        return application.getFilesDir();
    }

    private static File createBookDirectory(File filesDir) {
        return createDir(filesDir, "Enterprise Books");
    }

    private static File createAudioDirectory(File filesDir) {
        return createDir(filesDir, "Enterprise Audios");
    }

    private static File createVideoDirectory(File filesDir) {
        return createDir(filesDir, "Enterprise Videos");
    }

    private static File createImageDirectory(File filesDir) {
        return createDir(filesDir, "Enterprise Images");
    }

    private static File createDir(File filesDir, String name) {
        if (filesDir == null) {
            return null;
        }
        File file = new File(filesDir, name);

        if (!file.exists()) {
            // TODO: 13.08.2019 cannot create dir on external storage
            if (file.mkdirs()) {
                return file;
            } else {
                return null;
            }
        }

        return file;
    }

    public static Set<ContentTool> getSupportedContentTools(Application application) {
        Set<ContentTool> tools = new HashSet<>();
        ContentTool tool = FileTools.getImageContentTool(application);

        if (tool.isSupported()) {
            tools.add(tool);
        }
        tool = FileTools.getAudioContentTool(application);

        if (tool.isSupported()) {
            tools.add(tool);
        }
        tool = FileTools.getTextContentTool(application);

        if (tool.isSupported()) {
            tools.add(tool);
        }
        tool = FileTools.getVideoContentTool(application);

        if (tool.isSupported()) {
            tools.add(tool);
        }
        return tools;
    }

    public static ContentTool getContentTool(int mediumType, Application application) {
        if (MediumType.is(MediumType.TEXT, mediumType)) {
            return FileTools.getTextContentTool(application);

        } else if (MediumType.is(MediumType.IMAGE, mediumType)) {
            return FileTools.getImageContentTool(application);

        } else if (MediumType.is(MediumType.VIDEO, mediumType)) {
            return FileTools.getVideoContentTool(application);

        } else if (MediumType.is(MediumType.AUDIO, mediumType)) {
            return FileTools.getAudioContentTool(application);

        } else {
            throw new IllegalArgumentException("invalid medium type: " + mediumType);
        }
    }

    public static boolean writeInternal(Application application) {
        return isWriteable(application, getInternalAppDir(application));
    }

    public static boolean writeExternal(Application application) {
        return isWriteable(application, getExternalAppDir(application));
    }

    public static boolean writable(Application application) {
        return FileTools.writeExternal(application) || FileTools.writeInternal(application);
    }

    private static boolean isWriteable(Application application, File dir) {
        return dir != null && getFreeMBSpace(dir) >= minMBSpaceAvailable;
    }

    @SuppressLint("UsableSpace")
    public static long getFreeMBSpace(File file) {
        return file.getUsableSpace() / (1024L * 1024L);
    }
}
