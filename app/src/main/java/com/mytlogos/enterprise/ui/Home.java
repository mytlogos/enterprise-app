package com.mytlogos.enterprise.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mytlogos.enterprise.MainActivity;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.SettingsActivity;
import com.mytlogos.enterprise.viewmodel.UserViewModel;

public class Home extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        this.addClickListener(view, R.id.chapter, new UnreadEpisodeFragment());
        this.addClickListener(view, R.id.news, new NewsFragment());
        this.addClickListener(view, R.id.history, new ReadHistoryFragment());
        this.addClickListener(view, R.id.list, new ListsFragment());
        this.addClickListener(view, R.id.medium, new MediumFragment());
        this.addClickListener(view, R.id.mediaInWait, new MediaInWaitListFragment());
        this.addClickListener(view, R.id.statistics, new Statistics());
        this.addClickListener(view, R.id.notifications, new NotificationFragment());
        this.addClickListener(view, R.id.external_user, new ExternalUserListFragment());
        this.addClickListener(view, R.id.settings, SettingsActivity.class);
        this.addClickListener(view, R.id.logout, MainActivity::logout);

        UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        TextView unreadChapter = view.findViewById(R.id.unread_chapter);
        TextView unreadNews = view.findViewById(R.id.unread_news);
        TextView readToday = view.findViewById(R.id.read_today);
        TextView readTotal = view.findViewById(R.id.read_total);
        TextView internalLists = view.findViewById(R.id.internal_lists);
        TextView externalLists = view.findViewById(R.id.external_lists);
        TextView audioMedia = view.findViewById(R.id.audio_medium);
        TextView videoMedia = view.findViewById(R.id.video_medium);
        TextView textMedia = view.findViewById(R.id.text_medium);
        TextView imageMedia = view.findViewById(R.id.image_medium);
        TextView unusedMedia = view.findViewById(R.id.unused_media);
        TextView externalUser = view.findViewById(R.id.external_user_count);

        viewModel.getHomeStatsLiveData().observe(this, user -> {
            if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                String name = user.getName();
                this.setTitle("Home - " + name);
            } else {
                this.setTitle("Home - " + "Not logged in");
            }

            unreadChapter.setText(getString(R.string.unread_chapter_value, user != null ? user.getUnreadChapterCount() : 0));
            unreadNews.setText(getString(R.string.unread_news_value, user != null ? user.getUnreadNewsCount() : 0));
            readToday.setText(getString(R.string.current_read, user != null ? user.getReadTodayCount() : 0));
            readTotal.setText(getString(R.string.total_read, user != null ? user.getReadTotalCount() : 0));
            internalLists.setText(getString(R.string.internal_lists, user != null ? user.getInternalLists() : 0));
            externalLists.setText(getString(R.string.external_lists, user != null ? user.getExternalLists() : 0));
            externalUser.setText(getString(R.string.external_user_count, user != null ? user.getExternalUser() : 0));
            audioMedia.setText(getString(R.string.audio_media, user != null ? user.getAudioMedia() : 0));
            videoMedia.setText(getString(R.string.video_media, user != null ? user.getVideoMedia() : 0));
            textMedia.setText(getString(R.string.text_media, user != null ? user.getTextMedia() : 0));
            imageMedia.setText(getString(R.string.image_media, user != null ? user.getImageMedia() : 0));
            unusedMedia.setText(getString(R.string.unused_media, user != null ? user.getUnusedMedia() : 0));
        });

        this.setTitle("Home");
        return view;
    }

    @SuppressWarnings("SameParameterValue")
    private void addClickListener(@NonNull View view, int viewId, Class<?> activityClass) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = getMainActivity();
            Intent intent = new Intent(activity, activityClass);
            startActivity(intent);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void addClickListener(@NonNull View view, int viewId, ClickListener listener) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = getMainActivity();
            listener.onClick(activity);
        });
    }

    private void addClickListener(@NonNull View view, int viewId, Fragment fragment) {
        View group = view.findViewById(viewId);
        group.setOnClickListener(v -> {
            MainActivity activity = getMainActivity();
            activity.switchWindow(fragment, true);
        });
    }

    private interface ClickListener {
        void onClick(MainActivity activity);
    }

}
