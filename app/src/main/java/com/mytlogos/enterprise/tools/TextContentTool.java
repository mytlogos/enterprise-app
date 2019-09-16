package com.mytlogos.enterprise.tools;

import android.annotation.SuppressLint;

import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.model.MediumType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;

public class TextContentTool extends ContentTool {

    private Map<Integer, File> externalTexts;
    private Map<Integer, File> internalTexts;
    private Map<Integer, String> episodePaths;

    TextContentTool(File internalContentDir, File externalContentDir) {
        super(internalContentDir, externalContentDir);
    }

    @Override
    public int getMedium() {
        return MediumType.TEXT;
    }

    @Override
    boolean isContentMedium(File file) {
        return file.getName().matches("\\d+\\.epub");
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    void removeMediaEpisodes(Set<Integer> episodeIds, String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        Map<Integer, String> episodePaths = getEpisodePaths(path);

        episodePaths.keySet().retainAll(episodeIds);
        // cant use filesystem, it needs minSdk >= 26, so fall back to rename
        // and copy all except the ones which shall be removed
        File file = new File(path);
        String originalName = file.getName();

        File src = new File(file.getParent(), "(1)" + originalName);
        if (!file.renameTo(src)) {
            System.err.println("could not rename file " + file.getAbsolutePath());
            return;
        }

        try (ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(file)); ZipFile source = new ZipFile(src)) {
            byte[] buffer = new byte[2048];

            for (ZipEntry entry : Collections.list(source.entries())) {
                if (episodePaths.containsValue(entry.getName())) {
                    continue;
                }
                ZipEntry newEntry = new ZipEntry(entry.getName());
                stream.putNextEntry(newEntry);

                try (InputStream in = source.getInputStream(entry)) {

                    while (in.available() > 0) {
                        int read = in.read(buffer);

                        if (read > 0) {
                            stream.write(buffer, 0, read);
                        }
                    }
                }
                stream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (src.exists() && !src.delete()) {
            System.err.println("could not delete old epub: " + src.getAbsolutePath());
        }
    }

    @Override
    Pattern getMediumContainerPattern() {
        return Pattern.compile("^(\\d+)\\.epub$");
    }

    @Override
    int getMediumContainerPatternGroup() {
        return 1;
    }

    @Override
    public Map<Integer, String> getEpisodePaths(String mediumPath) {
        if (mediumPath == null || !mediumPath.endsWith(".epub")) {
            throw new IllegalArgumentException("'%s' is not a epub");
        }
        try (ZipFile file = new ZipFile(mediumPath)) {
            String markerFile = "content.opf";
            Enumeration<? extends ZipEntry> entries = file.entries();

            List<String> chapterFiles = new ArrayList<>();
            String folder = null;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".xhtml")) {
                    chapterFiles.add(entry.getName());
                }
                int index = entry.getName().indexOf(markerFile);
                if (index > 0) {
                    folder = entry.getName().substring(0, index);
                }
            }
            if (folder == null) {
                return Collections.emptyMap();
            }
            @SuppressLint("UseSparseArrays")
            Map<Integer, String> episodeMap = new HashMap<>();

            for (String chapterFile : chapterFiles) {
                if (!chapterFile.startsWith(folder)) {
                    continue;
                }

                try (InputStream inputStream = file.getInputStream(file.getEntry(chapterFile))) {
                    byte[] buffer = new byte[128];
                    String readInput = "";
                    Pattern pattern = Pattern.compile("<body id=\"(\\d+)\">");
                    int read = inputStream.read(buffer);

                    while (read != -1) {
                        readInput += new String(buffer);
                        Matcher matcher = pattern.matcher(readInput);

                        if (matcher.find()) {
                            String group = matcher.group(1);
                            int episodeId = Integer.parseInt(group);
                            episodeMap.put(episodeId, chapterFile);
                            break;
                        }
                        read = inputStream.read(buffer);
                    }
                }
                if (!episodeMap.values().contains(chapterFile)) {
                    System.out.println("no id found for " + chapterFile);
                }
            }
            return episodeMap;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    @Override
    public String getItemPath(int mediumId, File dir) {
        for (File file : dir.listFiles()) {
            if (file.getName().matches(mediumId + "\\.epub")) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public void saveContent(Collection<ClientDownloadedEpisode> episodes, int mediumId) throws IOException {
        if (externalTexts == null) {
            externalTexts = this.getItemContainers(true);
        }
        if (internalTexts == null) {
            internalTexts = this.getItemContainers(false);
        }

        Book book;
        File file;

        boolean writeExternal = writeExternal();
        boolean writeInternal = writeInternal();

        if (writeExternal && externalTexts.containsKey(mediumId)) {
            file = externalTexts.get(mediumId);
            book = this.loadBook(file);
        } else if (writeInternal && internalTexts.containsKey(mediumId)) {
            file = internalTexts.get(mediumId);
            book = this.loadBook(file);
        } else {
            String fileName = mediumId + ".epub";

            File dir;

            if (writeExternal) {
                dir = externalContentDir;
            } else if (writeInternal) {
                dir = internalContentDir;
            } else {
                throw new IOException("Out of Storage Space: Less than " + minMBSpaceAvailable + "  MB available");
            }
            file = new File(dir, fileName);
            book = new Book();
        }
        if (file == null) {
            return;
        }
        for (ClientDownloadedEpisode episode : episodes) {
            Resource resource = new Resource(toXhtml(episode).getBytes(), MediatypeService.XHTML);
            book.addSection(episode.getTitle(), resource);
        }
        new EpubWriter().write(book, new FileOutputStream(file));

        if (writeExternal) {
            externalTexts.put(mediumId, file);
        } else {
            internalTexts.put(mediumId, file);
        }
    }

    private Book loadBook(File file) throws IOException {
        return new EpubReader().readEpub(new FileInputStream(file));
    }

    @Override
    void mergeExternalAndInternalMedium(boolean toExternal, File source, File goal, File toParent, Integer mediumId) {
        // TODO: 05.08.2019 implement
    }

    @Override
    public long getEpisodeSize(File value, int episodeId, Map<Integer, String> episodePaths) {
        this.episodePaths = episodePaths;
        return this.getEpisodeSize(value, episodeId);
    }

    @Override
    public long getEpisodeSize(File value, int episodeId) {
        try (ZipFile file = new ZipFile(value)) {

            if (episodePaths == null) {
                episodePaths = this.getEpisodePaths(value.getAbsolutePath());
            }
            String entryName = episodePaths.get(episodeId);

            if (entryName == null) {
                return 0;
            }
            ZipEntry entry = file.getEntry(entryName);

            if (entry == null) {
                return 0;
            }
            return entry.getCompressedSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String toXhtml(ClientDownloadedEpisode episode) {
        String[] arrayContent = episode.getContent();

        if (arrayContent.length == 0) {
            return "";
        }
        String content = arrayContent[0];
        int titleIndex = content.indexOf(episode.getTitle());

        if (titleIndex < 0 || titleIndex > (content.length() / 3)) {
            content = "<h3>" + episode.getTitle() + "</h3>" + content;
        }

        if (content.matches("\\s*<html.*>(<head>.*</head>)?<body>.+</body></html>\\s*")) {
            return content;
        }
        return "<html><head></head><body id=\"" + episode.getEpisodeId() + "\">" + content + "</body></html>";
    }

    public String openEpisode(String zipFileLink, String episodeFile) {
        if (zipFileLink == null || !zipFileLink.endsWith(".epub")) {
            return "Invalid File Link";
        }
        if (episodeFile == null || !episodeFile.endsWith(".xhtml")) {
            return "Invalid Episode Link";
        }
        try (ZipFile file = new ZipFile(zipFileLink)) {
            ZipEntry entry = file.getEntry(episodeFile);

            if (entry == null) {
                return "Invalid Episode Link";
            }
            StringBuilder builder = new BufferedReader(new InputStreamReader(file.getInputStream(entry)))
                    .lines()
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not open Book";
        }
    }
}
