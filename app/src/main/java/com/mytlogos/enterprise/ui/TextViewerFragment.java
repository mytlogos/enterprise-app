package com.mytlogos.enterprise.ui;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.ScrollHideHelper;
import com.mytlogos.enterprise.tools.TextContentTool;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextViewerFragment extends BaseFragment {
    private static final String MEDIUM = "MEDIUM_FILE";
    private static final String START_EPISODE = "START_EPISODE";

    private int currentEpisode;
    private String currentBook;
    private WebView webView;
    private DateTime lastReadingModeChange = null;
    List<ReadableEpisode> readableEpisodes = new ArrayList<>();
    ReadableEpisode currentlyReading;
    SwipyRefreshLayout swipeLayout;
    BottomNavigationView navigationView;
    View appbar;
    final ScrollHideHelper scrollHideHelper = new ScrollHideHelper();


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TextViewerFragment.
     */
    public static TextViewerFragment newInstance(int startEpisode, String zipFile) {
        TextViewerFragment fragment = new TextReaderFragmentText();
        Bundle args = new Bundle();
        args.putInt(START_EPISODE, startEpisode);
        args.putString(MEDIUM, zipFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.currentEpisode = getArguments().getInt(START_EPISODE);
            this.currentBook = getArguments().getString(MEDIUM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.text_reader_fragment, container, false);
        this.webView = view.findViewById(R.id.web_view);
        this.swipeLayout = view.findViewById(R.id.swiper);
        this.swipeLayout.setOnRefreshListener(this::navigateEpisode);
        this.navigationView = view.findViewById(R.id.navigation);
        this.appbar = requireActivity().findViewById(R.id.appbar);
        this.navigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.left_nav) {
                navigateEpisode(SwipyRefreshLayoutDirection.TOP);
            } else if (item.getItemId() == R.id.right_nav) {
                navigateEpisode(SwipyRefreshLayoutDirection.BOTTOM);
            } else {
                System.out.println("unknown MenuItem for Text Navigation: " + item.getItemId());
                showToast("Unknown MenuItem");
            }
            return true;
        });
        this.webView.setOnClickListener(v -> this.toggleReadingMode());
        this.loadZip();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.scrollHideHelper.showGroups(this.navigationView, null, this.appbar, null);
    }

    void toggleReadingMode() {
        this.scrollHideHelper.toggleGroups(this.navigationView, null, this.appbar, null);
    }

    void navigateEpisode(SwipyRefreshLayoutDirection direction) {
        if (this.currentlyReading == null) {
            if (this.readableEpisodes.isEmpty()) {
                return;
            } else {
                this.currentlyReading = this.readableEpisodes.get(0);
            }
        } else {
            int index = this.readableEpisodes.indexOf(currentlyReading);
            if (direction == SwipyRefreshLayoutDirection.TOP) {
                index--;
            } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                index++;
            } else {
                System.out.println("Unknown swipe direction in TextViewerFragment, neither top or bottom");
                return;
            }
            if (index >= this.readableEpisodes.size()) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the last saved episode");
                this.swipeLayout.setRefreshing(false);
                return;
            } else if (index < 0) {
                // TODO: 26.07.2019 check with if there are more episodes and save them
                showToast("You are already reading the first saved episode");
                this.swipeLayout.setRefreshing(false);
                return;
            }
            this.currentlyReading = this.readableEpisodes.get(index);
        }
        new OpenEpisodeTask().execute();
    }

    static class ReadableEpisode extends SimpleEpisode {
        private final String file;

        ReadableEpisode(SimpleEpisode episode, String file) {
            super(episode.getEpisodeId(), episode.getTotalIndex(), episode.getPartialIndex(), episode.getProgress());
            this.file = file;
        }
    }

    void displayData(CharSequence data) {
        if (data instanceof String) {
            webView.loadData(
                    (String) data,
                    "text/html; charset=utf-8",
                    "UTF-8"
            );
        } else {
            showToast("Error while loading Episode, Contact Dev and hit him");
        }
    }

    CharSequence processData(String data) {
        if (data != null && data.length() < 200) {
            showToast(data);
            data = null;
        }
        if (data == null) {
            data = "<html><head></head><body>No Content Found.</body></html>";
        } else {
            // TODO: 15.06.2019 escape # with %23 (chromium complains about it, but is # even there?
            data = data.replaceAll("#", "%23");
        }
        return data;
    }

    @SuppressLint("StaticFieldLeak")
    class OpenEpisodeTask extends AsyncTask<Void, Void, CharSequence> {

        @Override
        protected CharSequence doInBackground(Void... voids) {
            TextContentTool bookTool = FileTools.getTextContentTool();
            String data = bookTool.openEpisode(currentBook, currentlyReading.file);
            return processData(data);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(CharSequence data) {
            if (currentlyReading != null) {
                if (currentlyReading.getPartialIndex() > 0) {
                    setTitle(String.format("Episode %d.%d", currentlyReading.getTotalIndex(), currentlyReading.getPartialIndex()));
                } else {
                    setTitle(String.format("Episode %d", currentlyReading.getTotalIndex()));
                }
            } else {
                setTitle("No Episode found");
            }
            displayData(data);
            swipeLayout.setRefreshing(false);
        }
    }

    void loadZip() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, CharSequence> task = new AsyncTask<Void, Void, CharSequence>() {

            @Override
            protected CharSequence doInBackground(Void... voids) {
                TextContentTool bookTool = FileTools.getTextContentTool();
                Map<Integer, String> episodeFileMap = bookTool.getEpisodePaths(TextViewerFragment.this.currentBook);

                if (episodeFileMap == null || episodeFileMap.isEmpty()) {
                    return "";
                }
                Repository repository = RepositoryImpl.getInstance(getMainActivity().getApplication());
                List<SimpleEpisode> episodes = repository.getSimpleEpisodes(episodeFileMap.keySet());

                for (SimpleEpisode simpleEpisode : episodes) {
                    int episodeId = simpleEpisode.getEpisodeId();
                    String file = episodeFileMap.get(episodeId);

                    if (file == null || file.isEmpty()) {
                        System.err.println("Could not find file for episodeId: " + episodeId);
                        continue;
                    }
                    ReadableEpisode readableEpisode = new ReadableEpisode(simpleEpisode, file);

                    if (episodeId == TextViewerFragment.this.currentEpisode) {
                        currentlyReading = readableEpisode;
                    }

                    readableEpisodes.add(readableEpisode);
                }

                if (currentlyReading == null || currentlyReading.file == null || currentlyReading.file.isEmpty()) {
                    return "Selected Episode is not available";
                }
                String data = bookTool.openEpisode(currentBook, currentlyReading.file);
                return processData(data);
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onPostExecute(CharSequence data) {
                if (currentlyReading != null) {
                    if (currentlyReading.getPartialIndex() > 0) {
                        setTitle(String.format("Episode %d.%d", currentlyReading.getTotalIndex(), currentlyReading.getPartialIndex()));
                    } else {
                        setTitle(String.format("Episode %d", currentlyReading.getTotalIndex()));
                    }
                } else {
                    setTitle("No Episode found");
                }
                displayData(data);
                swipeLayout.setRefreshing(false);
            }

            @Override
            protected void onCancelled() {
                showToastError("Could not load Book");
            }
        };
        task.execute();
    }

    private void showToastError(String s) {
        getMainActivity().runOnUiThread(() -> showToast(s));
    }
}
