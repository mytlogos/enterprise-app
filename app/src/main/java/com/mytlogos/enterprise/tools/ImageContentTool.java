package com.mytlogos.enterprise.tools;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.model.ChapterPage;
import com.mytlogos.enterprise.model.MediumType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageContentTool extends ContentTool {

    private final Repository repository;
    private Map<Integer, File> internalImageMedia;
    private Map<Integer, File> externalImageMedia;

    ImageContentTool(File internalContentDir, File externalContentDir, Repository repository) {
        super(internalContentDir, externalContentDir);
        this.repository = repository;
    }

    @Override
    public int getMedium() {
        return MediumType.IMAGE;
    }

    boolean isContentMedium(File file) {
        return file.getName().matches("\\d+") && file.isDirectory();
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    void removeMediaEpisodes(Set<Integer> episodeIds, String path) {
        File file = new File(path);
        Set<String> prefixes = new HashSet<>();

        for (Integer episodeId : episodeIds) {
            prefixes.add(episodeId + "-");
        }
        for (File episodePath : file.listFiles()) {

            String name = episodePath.getName();

            if (!name.endsWith(".png")) {
                continue;
            }

            for (String prefix : prefixes) {
                if (!name.startsWith(prefix)) {
                    continue;
                }
                try {
                    if (!episodePath.delete()) {
                        String idSubString = prefix.substring(0, prefix.indexOf("-"));
                        System.err.printf("could not delete episode %s totally, deleting: '%s' failed%n", idSubString, file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    Pattern getMediumContainerPattern() {
        return Pattern.compile("^(\\d+)$");
    }

    @Override
    int getMediumContainerPatternGroup() {
        return 1;
    }

    @Override
    public Map<Integer, String> getEpisodePaths(String mediumPath) {
        File file = new File(mediumPath);
        if (!file.exists() || !file.isDirectory()) {
            return Collections.emptyMap();
        }
        Pattern pagePattern = Pattern.compile("^(\\d+)-\\d+\\.png$");

        @SuppressLint("UseSparseArrays")
        Map<Integer, String> firstPageEpisodes = new HashMap<>();

        for (String episodePath : file.list()) {
            Matcher matcher = pagePattern.matcher(episodePath);

            if (!matcher.matches()) {
                continue;
            }
            String episode = matcher.group(1);
            int episodeId = Integer.parseInt(episode);

            // look for available pages
            if (!firstPageEpisodes.containsKey(episodeId)) {
                firstPageEpisodes.put(episodeId, episodePath);
            }
        }
        return firstPageEpisodes;
    }

    @Override
    String getItemPath(int mediumId, File dir) {
        for (File file : dir.listFiles()) {
            if ((mediumId + "").equals(file.getName()) && file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public void saveContent(Collection<ClientDownloadedEpisode> episodes, int mediumId) throws IOException {
        if (externalImageMedia == null) {
            externalImageMedia = this.getItemContainers(true);
        }
        if (internalImageMedia == null) {
            internalImageMedia = this.getItemContainers(false);
        }
        File file;

        boolean writeExternal = writeExternal();
        boolean writeInternal = writeInternal();

        if (writeExternal && externalImageMedia.containsKey(mediumId)) {
            file = externalImageMedia.get(mediumId);
        } else if (writeInternal && internalImageMedia.containsKey(mediumId)) {
            file = internalImageMedia.get(mediumId);
        } else {
            File dir;

            if (writeExternal) {
                dir = externalContentDir;
            } else if (writeInternal) {
                dir = internalContentDir;
            } else {
                throw new NotEnoughSpaceException("Out of Storage Space: Less than " + minMBSpaceAvailable + " MB available");
            }
            file = new File(dir, mediumId + "");

            if (!file.exists() && !file.mkdir()) {
                throw new IOException("could not create image medium directory");
            }
        }
        for (ClientDownloadedEpisode episode : episodes) {
            String[] content = episode.getContent();
            if (content == null || content.length == 0) {
                continue;
            }
            List<String> links = this.repository.getReleaseLinks(episode.getEpisodeId());
            List<File> writtenFiles = new ArrayList<>();

            for (int page = 0, contentLength = content.length; page < contentLength; page++) {
                String link = content[page];
                String pageLinkDomain = Utils.getDomain(link);

                if (pageLinkDomain == null) {
                    System.err.println("invalid url: '" + link + "'");
                    continue;
                }
                String referer = null;

                for (String releaseUrl : links) {
                    if (Objects.equals(Utils.getDomain(link), pageLinkDomain)) {
                        referer = releaseUrl;
                        break;
                    }
                }
                if (referer == null || referer.isEmpty()) {
                    // we need a referrer for sites like mangahasu
                    continue;
                }
                // TODO: 06.08.2019 instead of continuing maybe create an empty image file to signal
                //  the reader that this page is explicitly missing?
                if (link == null || link.isEmpty()) {
                    System.err.println("got an invalid link");
                    continue;
                }
                try {
                    URL url = new URL(link);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestProperty("Referer", referer);
                    httpURLConnection.connect();
                    int responseCode = httpURLConnection.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.err.println("invalid response for " + link);
                        continue;
                    }

                    try (InputStream in = httpURLConnection.getInputStream()) {
                        Bitmap bitmap = BitmapFactory.decodeStream(in);

                        String pageName = String.format("%s-%s.png", episode.getEpisodeId(), page + 1);
                        File image = new File(file, pageName);

                        try (OutputStream outputStream = new FileOutputStream(image)) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                            outputStream.flush();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        writtenFiles.add(image);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (page == 0) {
                    File firstImage = writtenFiles.get(0);
                    long estimatedByteSize = firstImage.length() * content.length;

                    if (!writeable(file, estimatedByteSize)) {
                        if (!firstImage.delete()) {
                            System.out.println("could not delete image: " + firstImage.getAbsolutePath());
                        }
                        throw new NotEnoughSpaceException();
                    }
                }
                // if the estimation was too low
                // and subsequent images took more space than expected
                // check if it can still write after this
                if (!this.writeable()) {
                    for (File writtenFile : writtenFiles) {
                        if (!writtenFile.delete()) {
                            System.out.println("could not delete image: " + writtenFile.getAbsolutePath());
                        }
                    }
                    throw new NotEnoughSpaceException();
                }
            }
        }
    }

    @Override
    void mergeExternalAndInternalMedium(boolean toExternal, File source, File goal, File toParent, Integer mediumId) {
        if (goal == null) {
            goal = new File(toParent, mediumId + "");

            if (!goal.mkdirs()) {
                System.err.println("could not create medium container");
                return;
            }
        }
        Map<Integer, Set<ChapterPage>> paths = getEpisodePagePaths(source.getAbsolutePath());
        for (Map.Entry<Integer, Set<ChapterPage>> entry : paths.entrySet()) {

            Set<File> files = new HashSet<>();
            long neededSpace = 0;

            for (ChapterPage page : entry.getValue()) {
                File file = new File(page.getPath());
                files.add(file);
                neededSpace += file.length();
            }
            if (!writeable(goal, neededSpace)) {
                continue;
            }
            boolean successFull = false;
            try {
                for (File file : files) {
                    copyFile(file, new File(goal, file.getName()));
                }
                successFull = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!successFull) {
                continue;
            }
            for (File file : files) {
                if (!file.delete()) {
                    System.err.println("could not delete file");
                }
            }
        }
    }

    @Override
    public long getEpisodeSize(File value, int episodeId) {
        String prefix = episodeId + "-";
        long size = 0;

        for (File file : value.listFiles()) {
            if (!file.getName().startsWith(prefix)) {
                continue;
            }
            size += file.length();
        }
        return size;
    }

    public Map<Integer, Set<ChapterPage>> getEpisodePagePaths(String mediumDir) {
        File file = new File(mediumDir);

        if (!file.exists() || !file.isDirectory()) {
            return Collections.emptyMap();
        }
        Pattern pagePattern = Pattern.compile("^(\\d+)-(\\d+)\\.png$");

        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<ChapterPage>> episodePages = new HashMap<>();

        for (String episodePath : file.list()) {
            Matcher matcher = pagePattern.matcher(episodePath);

            if (!matcher.matches()) {
                continue;
            }

            String episodeIdString = matcher.group(1);
            String pageString = matcher.group(2);
            int episodeId = Integer.parseInt(episodeIdString);
            int page = Integer.parseInt(pageString);

            String absolutePath = new File(file, episodePath).getAbsolutePath();

            episodePages
                    .computeIfAbsent(episodeId, integer -> new HashSet<>())
                    .add(new ChapterPage(episodeId, page, absolutePath));
        }
        return episodePages;
    }
}
