package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.tools.HtmlToPlainText;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;

public class TextReaderFragmentText extends TextViewerFragment {
    private TextView textDisplay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader_text, container, false);
        this.swipeLayout = view.findViewById(R.id.swiper);
        this.textDisplay = view.findViewById(R.id.display);
        this.textDisplay.setMovementMethod(new ScrollingMovementMethod());
        swipeLayout.setOnRefreshListener(direction -> {
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
                    return;
                } else if (index < 0) {
                    // TODO: 26.07.2019 check with if there are more episodes and save them
                    showToast("You are already reading the first saved episode");
                    return;
                }
                this.currentlyReading = this.readableEpisodes.get(index);
            }
            new OpenEpisodeTask().execute();
        });
        this.loadZip();
        this.setHasOptionsMenu(true);
        return view;
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

    @Override
    void displayData(String data) {
        // this does not work really, can't scroll to the bottom
        // and displays characters like ' or ´ incorrectly
        CharSequence text;
        try {
            text = new HtmlToPlainText().getPlainText(Jsoup.parse(data).body());
//            text = HtmlCompat.fromHtml(data, HtmlCompat.FROM_HTML_MODE_COMPACT);
        } catch (Exception ignored) {
            text = data;
        }
        textDisplay.setText(text);
        textDisplay.scrollTo(0, 0);
    }
}
