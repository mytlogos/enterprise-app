package com.mytlogos.enterprise.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mytlogos.enterprise.MainActivity;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.SettingsActivity;
import com.mytlogos.enterprise.viewmodel.UserViewModel;

import java.util.Objects;

public class Home extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        this.addClickListener(view, R.id.chapter, new UnreadChapterFragment());
        this.addClickListener(view, R.id.news, new NewsFragment());
        this.addClickListener(view, R.id.history, new ReadHistoryFragment());
        this.addClickListener(view, R.id.statistics, new Statistics());
        this.addClickListener(view, R.id.settings, SettingsActivity.class);
        this.addClickListener(view, R.id.logout, MainActivity::logout);

        UserViewModel viewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        TextView unreadChapter = view.findViewById(R.id.unread_chapter);
        TextView unreadNews = view.findViewById(R.id.unread_news);
        TextView readHistory = view.findViewById(R.id.read_history);

        viewModel.getUser().observe(this, user -> {
            int unReadNewsCount = user == null ? 0 : user.unreadNewsCount();
            int unReadChapterCount = user == null ? 0 : user.unreadChapterCount();
            int readTodayCount = user == null ? 0 : user.readTodayCount();

            unreadChapter.setText(getString(R.string.unread_chapter_value, unReadChapterCount));
            unreadNews.setText(getString(R.string.unread_news_value, unReadNewsCount));
            readHistory.setText(getString(R.string.current_read, readTodayCount));
        });
        return view;
    }

    @SuppressWarnings("SameParameterValue")
    private void addClickListener(@NonNull View view, int viewId, Class<?> activityClass) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) Objects.requireNonNull(getActivity());
            Intent intent = new Intent(activity, activityClass);
            startActivity(intent);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void addClickListener(@NonNull View view, int viewId, ClickListener listener) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) Objects.requireNonNull(getActivity());
            listener.onClick(activity);
        });
    }

    private void addClickListener(@NonNull View view, int viewId, Fragment fragment) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) Objects.requireNonNull(getActivity());
            activity.switchWindow(fragment, true);
        });
    }

    private interface ClickListener {
        void onClick(MainActivity activity);
    }

}
