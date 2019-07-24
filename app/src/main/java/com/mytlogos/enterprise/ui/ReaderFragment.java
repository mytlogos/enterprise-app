package com.mytlogos.enterprise.ui;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mytlogos.enterprise.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderFragment extends BaseFragment {
    private static final String BOOK = "BOOK_FILE";
    private static final String START_EPISODE = "START_EPISODE";

    private int currentEpisode;
    private String currentBook;
    private WebView webView;
    private Map<Integer, String> currentBookMap;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReaderFragment.
     */
    public static ReaderFragment newInstance(int startEpisode, String zipFile) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putInt(START_EPISODE, startEpisode);
        args.putString(BOOK, zipFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.currentEpisode = getArguments().getInt(START_EPISODE);
            this.currentBook = getArguments().getString(BOOK);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        this.webView = view.findViewById(R.id.web_view);
        this.loadZip();
        return view;
    }

    private void loadZip() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                currentBookMap = readZipFile(ReaderFragment.this.currentBook);

                if (currentBookMap == null) {
                    return "";
                }
                String episodeFile = currentBookMap.get(currentEpisode);
                return openEpisode(currentBook, episodeFile);
            }

            @Override
            protected void onPostExecute(String data) {
                if (data == null || data.isEmpty()) {
                    data = "<html><head></head><body>No Content Found.</body></html>";
                }
                webView.loadData(
                        data,
                        "text/html; charset=utf-8",
                        "UTF-8"
                );
            }

            @Override
            protected void onCancelled() {
                showToastError("Could not load Book");
            }
        };
        task.execute();
    }

    private String openEpisode(String zipFileLink, String episodeFile) {
        if (zipFileLink == null || !zipFileLink.endsWith(".epub")) {
            showToastError("Invalid File Link");
            return "";
        }
        if (episodeFile == null || !episodeFile.endsWith(".xhtml")) {
            showToastError("Invalid EpisodeFile Link");
            return "";
        }
        try (ZipFile file = new ZipFile(zipFileLink)) {
            ZipEntry entry = file.getEntry(episodeFile);

            if (entry == null) {
                showToastError("Invalid Episode Link");
                return "";
            }
            // TODO: 15.06.2019 escape # with %23 (chromium complains about it, but is # even there?
            StringBuilder builder = new BufferedReader(new InputStreamReader(file.getInputStream(entry)))
                    .lines()
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            showToastError("Error while reading Book");
            return "";
        }
    }

    private void showToastError(String s) {
        getMainActivity().runOnUiThread(() -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
    }

    private Map<Integer, String> readZipFile(String fileLink) {
        if (fileLink == null || !fileLink.endsWith(".epub")) {
            showToastError("Invalid File Link");
            return null;
        }
        try (ZipFile file = new ZipFile(fileLink)) {
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
                    System.out.println(folder);
                }
            }
            if (folder == null) {
                return null;
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
            showToastError("Error while reading Book");
            return null;
        }
    }
}
