package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
                    Toast.makeText(this.getContext(), "You are already reading the last saved episode", Toast.LENGTH_SHORT).show();
                    return;
                } else if (index < 0) {
                    // TODO: 26.07.2019 check with if there are more episodes and save them
                    Toast.makeText(this.getContext(), "You are already reading the first saved episode", Toast.LENGTH_SHORT).show();
                    return;
                }
                this.currentlyReading = this.readableEpisodes.get(index);
            }
            new OpenEpisodeTask().execute();
        });
        this.loadZip();
        return view;
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
