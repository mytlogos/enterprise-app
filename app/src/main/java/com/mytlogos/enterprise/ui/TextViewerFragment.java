package com.mytlogos.enterprise.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.TextContentTool;

import org.jsoup.Jsoup;

import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextViewerFragment extends ViewerFragment<TextViewerFragment.ReadableEpisode> {
    private TextView textDisplay;
    private ScrollView scrollView;

    private int currentEpisode;
    private String currentBook;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TextViewerFragment.
     */
    public static TextViewerFragment newInstance(int startEpisode, String zipFile) {
        TextViewerFragment fragment = new TextViewerFragment();
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

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.textDisplay = view.findViewById(R.id.display);
        this.scrollView = view.findViewById(R.id.scroller);
        this.textDisplay.setMovementMethod(new ScrollingMovementMethod());
        this.setHasOptionsMenu(true);
        this.scrollView.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        this.onScroll(scrollX, scrollY, oldScrollX, oldScrollY)
        );
        this.textDisplay.setOnClickListener(v -> this.toggleReadingMode());
        this.loadZip();
        return view;
    }

    @Override
    int getLayoutRes() {
        return R.layout.fragment_reader_text;
    }

    @Override
    void updateContent() {
        new OpenEpisodeTask().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bundle bundle = new Bundle();
        bundle.putInt(START_EPISODE, currentEpisode);
        bundle.putString(MEDIUM, currentBook);
        this.setArguments(bundle);
    }

    static class ReadableEpisode extends SimpleEpisode {
        private final String file;

        ReadableEpisode(SimpleEpisode episode, String file) {
            super(episode.getEpisodeId(), episode.getTotalIndex(), episode.getPartialIndex(), episode.getProgress());
            this.file = file;
        }
    }

    private void displayData(String data) {
        // this does not work really, can't scroll to the bottom
        // and displays characters like ' or ´ incorrectly
        CharSequence text;
        try {
//            text = new HtmlToPlainText().getPlainText(Jsoup.parse(data).body());
            String html = Jsoup.parse(data).body().html();
            text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        } catch (Exception ignored) {
            text = data;
        }
        textDisplay.setText(text);
        scrollView.scrollTo(0, 0);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.text_viewer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.font) {
            changeFont();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void changeFont() {
        Activity context = requireActivity();
        @SuppressLint("InflateParams")
        View view = context.getLayoutInflater().inflate(R.layout.change_font, null);
        EditText fontTextView = view.findViewById(R.id.font_size);
        fontTextView.setText(this.textDisplay.getTextSize() + "");

        view.findViewById(R.id.increment_font).setOnClickListener(v -> {
            try {
                int currentFont = Integer.parseInt(fontTextView.getText().toString());
                currentFont++;

                if (currentFont > 40) {
                    currentFont = 14;
                }
                fontTextView.setText(currentFont + "");
                this.textDisplay.setTextSize(currentFont);
            } catch (NumberFormatException e) {
                fontTextView.setText("0");
            }
        });
        view.findViewById(R.id.decrement_font).setOnClickListener(v -> {
            try {
                int currentFont = Integer.parseInt(fontTextView.getText().toString());
                currentFont--;

                if (currentFont < 2) {
                    currentFont = 14;
                }

                fontTextView.setText(currentFont + "");
                this.textDisplay.setTextSize(currentFont);
            } catch (NumberFormatException e) {
                fontTextView.setText("0");
            }
        });
        new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Modify Font")
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    class OpenEpisodeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            TextContentTool bookTool = FileTools.getTextContentTool();
            return bookTool.openEpisode(currentBook, currentlyReading.file);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String data) {
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

    private void loadZip() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
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
                return bookTool.openEpisode(currentBook, currentlyReading.file);
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onPostExecute(String data) {
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
        requireActivity().runOnUiThread(() -> showToast(s));
    }
}
