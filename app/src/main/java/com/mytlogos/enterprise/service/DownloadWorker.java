package com.mytlogos.enterprise.service;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.model.ToDownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;

public class DownloadWorker extends Worker {
    private static final String UNIQUE = "DOWNLOAD_WORKER";

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueueDownloadTask() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(DownloadWorker.class)
                .setConstraints(constraints)
                .build();
/*
        WorkManager.getInstance().enqueueUniqueWork(
                "DOWNLOAD_WORKER",
                ExistingWorkPolicy.APPEND,
                oneTimeWorkRequest
        );*/

        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        if (!(this.getApplicationContext() instanceof Application)) {
            System.out.println("Context not instance of Application");
            return Result.failure();
        }
        this.episodeLimit = maxEpisodeLimit;

        // todo read limit from settings
        try {
            synchronized (UNIQUE) {
                Application application = (Application) this.getApplicationContext();
                Repository repository = RepositoryImpl.getInstance(application);

                if (!repository.isClientAuthenticated()) {
                    return Result.retry();
                }

                List<ToDownload> toDownloadList = repository.getToDownload();

                List<Integer> prohibitedMedia = new ArrayList<>();
                Set<Integer> toDownloadMedia = new HashSet<>();

                for (ToDownload toDownload : toDownloadList) {
                    if (toDownload.getMediumId() != null) {
                        if (toDownload.isProhibited()) {
                            prohibitedMedia.add(toDownload.getMediumId());
                        } else {
                            toDownloadMedia.add(toDownload.getMediumId());
                        }
                    }

                    if (toDownload.getExternalListId() != null) {
                        toDownloadMedia.addAll(repository.getExternalListItems(toDownload.getExternalListId()));
                    }

                    if (toDownload.getListId() != null) {
                        toDownloadMedia.addAll(repository.getListItems(toDownload.getListId()));
                    }
                }

                toDownloadMedia.removeAll(prohibitedMedia);

                SparseArray<List<Integer>> mediaEpisodes = new SparseArray<>();

                for (Integer mediumId : toDownloadMedia) {
                    List<Integer> unReadEpisodes = repository.getDownloadableEpisodes(mediumId);

                    if (!unReadEpisodes.isEmpty()) {
                        mediaEpisodes.put(mediumId, unReadEpisodes);
                    }
                }
                if (mediaEpisodes.size() > 0) {
                    this.downloadEpisodes(mediaEpisodes, application, repository);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }

    private static final int maxEpisodeLimit = 50;
    private static final int maxPackageSize = 5;
    private static final int downloadedEpisodeLimit = 50;
    private int episodeLimit;

    /**
     * Download episodes for each medium id,
     * up to an episode limit initialized in {@link #maxEpisodeLimit}.
     *
     * @param episodeIds  episodeIds of media to download
     * @param application current application - needed for internal storage file access
     */
    private void downloadEpisodes(SparseArray<List<Integer>> episodeIds, Application application, Repository repository) {
        File internalAppDir = this.getInternalAppDir(application);
        File externalAppDir = this.getExternalAppDir();

        Collection<Collection<Integer>> episodePackages = getDownloadPackages(episodeIds, repository);

        List<ClientDownloadedEpisode> downloadedEpisodes = new ArrayList<>();

        for (Collection<Integer> episodePackage : episodePackages) {
            try {
                // packs of 5 episodes at max or so, to not 'download' too much at a time
                downloadedEpisodes.addAll(repository.downloadedEpisodes(episodePackage));

                if (downloadedEpisodes.size() > downloadedEpisodeLimit) {
                    this.saveDownloadedContent(downloadedEpisodes, episodeIds, internalAppDir, externalAppDir);

                    List<Integer> currentlySavedEpisodes = new ArrayList<>();

                    for (ClientDownloadedEpisode downloadedEpisode : downloadedEpisodes) {
                        currentlySavedEpisodes.add(downloadedEpisode.getEpisodeId());
                    }
                    downloadedEpisodes.clear();

                    repository.updateSaved(currentlySavedEpisodes, true);
                }
                // todo create a download tracking notification?
            } catch (IOException e) {
                e.printStackTrace();
                downloadedEpisodes.clear();
                // todo create notification indicating error
            }
        }
        if (!downloadedEpisodes.isEmpty()) {
            try {
                this.saveDownloadedContent(downloadedEpisodes, episodeIds, internalAppDir, externalAppDir);
                List<Integer> currentlySavedEpisodes = new ArrayList<>();

                for (ClientDownloadedEpisode downloadedEpisode : downloadedEpisodes) {
                    currentlySavedEpisodes.add(downloadedEpisode.getEpisodeId());
                }

                repository.updateSaved(currentlySavedEpisodes, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.mergeMediumFiles(getBooks(internalAppDir), getBooks(externalAppDir));
    }

    private void saveDownloadedContent(List<ClientDownloadedEpisode> downloadedEpisodes,
                                       SparseArray<List<Integer>> episodeIds, File internalAppDir,
                                       File externalAppDir) throws IOException {

        SparseArray<List<ClientDownloadedEpisode>> mediumDownloadedEpisodes = new SparseArray<>();

        for (ClientDownloadedEpisode episode : downloadedEpisodes) {

            for (int i = 0; i < episodeIds.size(); i++) {

                if (episodeIds.valueAt(i).contains(episode.getEpisodeId())) {
                    List<ClientDownloadedEpisode> episodeList = mediumDownloadedEpisodes.get(episodeIds.keyAt(i));

                    if (episodeList == null) {
                        episodeList = new ArrayList<>();
                        mediumDownloadedEpisodes.put(episodeIds.keyAt(i), episodeList);
                    }

                    episodeList.add(episode);
                    break;
                }
            }
        }

        SparseArray<File> internalBooks = getBooks(internalAppDir);
        SparseArray<File> externalBooks = getBooks(externalAppDir);

        int minMBSpaceAvailable = 100;
        boolean writeInternal = internalAppDir != null && this.getFreeMBSpace(internalAppDir) >= minMBSpaceAvailable;
        boolean writeExternal = externalAppDir != null && this.getFreeMBSpace(externalAppDir) >= minMBSpaceAvailable;

        for (int i = 0; i < mediumDownloadedEpisodes.size(); i++) {
            int key = mediumDownloadedEpisodes.keyAt(i);

            Book book;
            File file;

            if (writeInternal && internalBooks.indexOfKey(key) >= 0) {
                file = internalBooks.get(key);
                book = this.loadBook(file);
            } else if (writeExternal && externalBooks.indexOfKey(key) >= 0) {
                file = externalBooks.get(key);
                book = this.loadBook(file);
            } else {
                String fileName = key + ".epub";

                if (writeInternal) {
                    file = new File(internalAppDir, fileName);
                } else if (writeExternal) {
                    file = new File(externalAppDir, fileName);
                } else {
                    throw new IOException("Out of Storage Space: Less than 100 MB available");
                }
                book = new Book();
            }

            for (ClientDownloadedEpisode episode : mediumDownloadedEpisodes.valueAt(i)) {
                Resource resource = new Resource(toXhtml(episode).getBytes(), MediatypeService.XHTML);
                book.addSection(episode.getTitle(), resource);
            }
            new EpubWriter().write(book, new FileOutputStream(file));
        }
    }

    private String toXhtml(ClientDownloadedEpisode episode) {
        String content = episode.getContent();
        int titleIndex = content.indexOf(episode.getTitle());

        if (titleIndex < 0 || titleIndex > (content.length() / 3)) {
            content = "<h3>" + episode.getTitle() + "</h3>" + content;
        }

        if (content.matches("\\s*<html.*>(<head>.*</head>)?<body>.+</body></html>\\s*")) {
            return content;
        }
        return "<html><head></head><body id=\"" + episode.getEpisodeId() + "\">" + content + "</body></html>";
    }

    private Book loadBook(File file) throws IOException {
        return new EpubReader().readEpub(new FileInputStream(file));
    }

    private Collection<Collection<Integer>> getDownloadPackages(SparseArray<List<Integer>> episodeIds, Repository repository) {
        List<Integer> savedEpisodes = repository.getSavedEpisodes();

        Set<Integer> savedIds = new HashSet<>(savedEpisodes);

        Collection<Collection<Integer>> episodePackages = new ArrayList<>();

        Collection<Integer> currentPackage = new ArrayList<>(10);

        for (int i = 0; i < episodeIds.size(); i++) {
            List<Integer> ints = episodeIds.valueAt(i);

            for (int k = 0, intsSize = ints.size(); k < intsSize && k < this.episodeLimit; k++) {
                Integer episodeId = ints.get(k);

                if (!savedIds.contains(episodeId)) {
                    if (currentPackage.size() == maxPackageSize) {
                        episodePackages.add(currentPackage);
                        currentPackage = new ArrayList<>(maxPackageSize);
                    }
                    currentPackage.add(episodeId);
                }
            }
        }
        if (!currentPackage.isEmpty()) {
            episodePackages.add(currentPackage);
        }
        return episodePackages;
    }

    private void mergeMediumFiles(SparseArray<File> files1, SparseArray<File> files2) {
        SparseArray<List<File>> mediumBooks = new SparseArray<>();

        for (int i = 0; i < files1.size(); i++) {
            mediumBooks
                    .get(files1.keyAt(i), new ArrayList<>(2))
                    .add(files1.valueAt(i));
        }

        for (int i = 0; i < files2.size(); i++) {
            mediumBooks
                    .get(files2.keyAt(i), new ArrayList<>(2))
                    .add(files2.valueAt(i));
        }

        for (int i = 0; i < mediumBooks.size(); i++) {
            List<File> files = mediumBooks.valueAt(i);
            if (files.size() == 2) {
                this.mergeBooks(files.get(0), files.get(1));
            }
        }
    }

    private void mergeBooks(File file1, File file2) {
        // todo implement merging
    }

    private SparseArray<File> getBooks(File file) {
        if (file == null) {
            return new SparseArray<>();
        }
        Pattern pattern = Pattern.compile("^(\\d+)\\.epub$");
        File[] files = file.listFiles((dir, name) -> Pattern.matches(pattern.pattern(), name));

        SparseArray<File> mediumIdFileMap = new SparseArray<>();

        for (File bookFile : files) {
            Matcher matcher = pattern.matcher(bookFile.getName());

            if (!matcher.matches()) {
                continue;
            }
            String mediumIdString = matcher.group(1);
            int mediumId;

            try {
                mediumId = Integer.parseInt(mediumIdString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            mediumIdFileMap.put(mediumId, bookFile);
        }
        return mediumIdFileMap;
    }

    private long getFreeMBSpace(File file) {
        return file.getFreeSpace() / (1024L * 1024L);
    }

    private File getExternalAppDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }
        return createBookDirectory(Environment.getExternalStorageDirectory());
    }

    private File getInternalAppDir(Application application) {
        return createBookDirectory(application.getFilesDir());
    }

    private File createBookDirectory(File filesDir) {
        File file = new File(filesDir, "Enterprise Books");

        if (!file.exists()) {
            return file.mkdir() ? file : null;
        }

        return file;
    }
}
