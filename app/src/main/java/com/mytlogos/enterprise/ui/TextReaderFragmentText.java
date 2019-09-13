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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;

import com.mytlogos.enterprise.R;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;

public class TextReaderFragmentText extends TextViewerFragment {
    private TextView textDisplay;
    private ScrollView scrollView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader_text, container, false);
        this.swipeLayout = view.findViewById(R.id.swiper);
        this.textDisplay = view.findViewById(R.id.display);
        this.scrollView = view.findViewById(R.id.scroller);
        this.textDisplay.setMovementMethod(new ScrollingMovementMethod());
        this.swipeLayout.setOnRefreshListener(this::navigateEpisode);
        this.loadZip();
        this.navigationView = view.findViewById(R.id.navigation);
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
        this.setHasOptionsMenu(true);
        this.scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY < oldScrollY) {
                // if scrolling up
                this.enableReadingMode(false);
            } else if (scrollY > oldScrollY) {
                // if scrolling down
                this.enableReadingMode(true);
            }
        });
        this.textDisplay.setOnClickListener(v -> this.toggleReadingMode());
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
    CharSequence processData(String data) {
        if (data != null && data.length() < 200) {
            showToast(data);
            data = null;
        }
        if (data == null) {
            data = "No Content Found";
        } else {
            try {
//            text = new HtmlToPlainText().getPlainText(Jsoup.parse(data).body());
                String html = Jsoup.parse(data).body().html();
                return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    @Override
    void displayData(CharSequence data) {
        textDisplay.setText(data);
        scrollView.scrollTo(0, 0);
    }
}
