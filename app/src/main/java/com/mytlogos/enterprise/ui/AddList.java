package com.mytlogos.enterprise.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.viewmodel.AddListViewModel;

public class AddList extends BaseFragment {

    private AddListViewModel mViewModel;
    private Switch autoDownload;
    private CheckBox audioMedium;
    private CheckBox videoMedium;
    private CheckBox imageMedium;
    private CheckBox textMedium;
    private EditText editName;

    public static AddList newInstance() {
        return new AddList();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_list_fragment, container, false);
        this.editName = view.findViewById(R.id.editName);
        this.textMedium = view.findViewById(R.id.text_medium);
        this.imageMedium = view.findViewById(R.id.image_medium);
        this.videoMedium = view.findViewById(R.id.video_medium);
        this.audioMedium = view.findViewById(R.id.audio_medium);
        this.autoDownload = view.findViewById(R.id.auto_download);

        Button addBtn = view.findViewById(R.id.add_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_button);

        this.mViewModel = ViewModelProviders.of(this).get(AddListViewModel.class);

        cancelBtn.setOnClickListener(v -> this.getMainActivity().onBackPressed());
        addBtn.setOnClickListener(v -> this.addList());
        this.editName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard();
            }
        });
        this.setTitle("Add List");
        return view;
    }

    private void addList() {
        String name = editName.getText().toString().trim();

        if (name.isEmpty()) {
            showToast("No Name");
            return;
        }
        int medium = 0;
        if (this.textMedium.isChecked()) {
            medium |= MediumType.TEXT;
        } else if (this.audioMedium.isChecked()) {
            medium |= MediumType.AUDIO;
        } else if (this.imageMedium.isChecked()) {
            medium |= MediumType.IMAGE;
        } else if (this.videoMedium.isChecked()) {
            medium |= MediumType.VIDEO;
        }
        MediaList mediaList = new MediaList(
                null,
                0,
                name,
                medium,
                0
        );
        AddListTask task = new AddListTask(this, mediaList, autoDownload.isChecked());
        task.execute();
        this.getMainActivity().showLoading(true);
    }

    private static class AddListTask extends AsyncTask<Void, Void, Void> {
        private final AddList addList;
        private final MediaList mediaList;
        private final boolean autoDownload;
        private String errorMessage;

        private AddListTask(AddList addList, MediaList mediaList, boolean autoDownload) {
            this.addList = addList;
            this.mediaList = mediaList;
            this.autoDownload = autoDownload;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String name = this.mediaList.getName();
            if (this.addList.mViewModel.exists(name)) {
                this.errorMessage = String.format("List with the name '%s' exists already", name);
                return null;
            }
            try {
                this.addList.mViewModel.addList(this.mediaList, this.autoDownload);
            } catch (Throwable e) {
                e.printStackTrace();
                this.errorMessage = "List could not be created";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (this.errorMessage != null) {
                this.addList.showToast(this.errorMessage);
            }
            this.addList.getMainActivity().showLoading(false);
            this.addList.getMainActivity().onBackPressed();
        }
    }
}
