package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import java.util.Objects;

public class MediumSettings extends BaseFragment {
    private ListsViewModel mediumViewModel;
    private LiveData<MediumSetting> liveMediumSettings;
    private Button openTocButton;
    private EditText editName;
    private CheckBox textMedium;
    private CheckBox imageMedium;
    private CheckBox videoMedium;
    private CheckBox audioMedium;
    private Switch autoDownload;
    public static final String ID = "id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_settings, container, false);

        Bundle arguments = Objects.requireNonNull(this.getArguments());
        int mediumId = arguments.getInt(ID);

        this.mediumViewModel = ViewModelProviders.of(this).get(ListsViewModel.class);
        this.liveMediumSettings = mediumViewModel.getMediumSettings(mediumId);

        this.liveMediumSettings.observe(this, this::handleNewMediumSetting);

        this.openTocButton = view.findViewById(R.id.open_items_button);
        this.editName = view.findViewById(R.id.editName);
        this.textMedium = view.findViewById(R.id.text_medium);
        this.imageMedium = view.findViewById(R.id.image_medium);
        this.videoMedium = view.findViewById(R.id.video_medium);
        this.audioMedium = view.findViewById(R.id.audio_medium);
        this.autoDownload = view.findViewById(R.id.auto_download);

        this.openTocButton.setOnClickListener(v -> {
            if (this.mediumSettings() == null) {
                return;
            }
            // todo add new fragment with only these mediaItems
        });

        this.editName.setOnEditorActionListener((v, actionId, event) -> handleEditorEvent(editName, actionId, event));

        addMediumListener(this.textMedium, MediumType.TEXT);
        addMediumListener(this.imageMedium, MediumType.IMAGE);
        addMediumListener(this.videoMedium, MediumType.VIDEO);
        addMediumListener(this.audioMedium, MediumType.AUDIO);

        this.autoDownload.setOnCheckedChangeListener((buttonView, isChecked) -> handleAutoDownloadChanges(isChecked));
        this.setTitle("List Settings");
        return view;
    }

    private void handleAutoDownloadChanges(boolean isChecked) {
        MediumSetting mediumSetting = this.mediumSettings();

        if (mediumSetting == null) {
            return;
        }
        if (mediumSetting.isToDownload() != isChecked) {
            int settingMediumId = mediumSetting.getMediumId();

            ToDownload toDownload = new ToDownload(false, settingMediumId, null, null);
            this.mediumViewModel.updateToDownload(isChecked, toDownload);
        }
    }

    private MediumSetting mediumSettings() {
        return this.liveMediumSettings.getValue();
    }

    private void handleNewMediumSetting(MediumSetting mediumSetting) {
        if (mediumSetting == null) {
            this.openTocButton.setEnabled(false);
            this.editName.setInputType(InputType.TYPE_NULL);

            this.textMedium.setEnabled(false);
            this.imageMedium.setEnabled(false);
            this.videoMedium.setEnabled(false);
            this.audioMedium.setEnabled(false);

            this.autoDownload.setEnabled(false);
        } else {
            this.editName.setText(mediumSetting.getTitle());
            this.editName.setInputType(InputType.TYPE_CLASS_TEXT);

            this.textMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.TEXT));
            this.imageMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.IMAGE));
            this.videoMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.VIDEO));
            this.audioMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.AUDIO));

            this.autoDownload.setChecked(mediumSetting.isToDownload());

            this.autoDownload.setEnabled(true);
            this.textMedium.setEnabled(true);
            this.imageMedium.setEnabled(true);
            this.videoMedium.setEnabled(true);
            this.audioMedium.setEnabled(true);
        }
    }

    private void addMediumListener(CheckBox box, int mediumType) {
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.mediumSettings() == null) {
                return;
            }

            if (isChecked) {
                MediumSetting setting = new MediumSetting
                        .MediumSettingBuilder(this.mediumSettings())
                        .setMedium(mediumType)
                        .createMediumSetting();

                this.mediumViewModel.updateMedium(setting);
            }

        });

    }

    private boolean handleEditorEvent(EditText editName, int actionId, KeyEvent event) {
        MediumSetting currentMediumSetting = this.mediumSettings();

        if ((currentMediumSetting != null) && ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                (actionId == EditorInfo.IME_ACTION_DONE) ||
                ((event != null) &&
                        (event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {
            // the user is done typing.

            String newTitle = editName.getText().toString();

            if (newTitle.isEmpty()) {
                editName.setText(currentMediumSetting.getTitle());

            } else if (!newTitle.equals(currentMediumSetting.getTitle())) {

                MediumSetting setting = new MediumSetting
                        .MediumSettingBuilder(currentMediumSetting)
                        .setTitle(newTitle)
                        .createMediumSetting();

                this.mediumViewModel
                        .updateMedium(setting)
                        .handle((s, throwable) -> {
                            if (this.getContext() != null && s != null && !s.isEmpty()) {
                                Toast.makeText(this.getContext(), s, Toast.LENGTH_SHORT).show();
                                editName.setText(currentMediumSetting.getTitle());

                            } else if (throwable != null) {
                                throwable.printStackTrace();
                                if (this.getContext() != null) {
                                    Toast
                                            .makeText(
                                                    this.getContext(),
                                                    "An Error occurred saving the new Title",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show();
                                }
                            }
                            return null;
                        });
            }
            return true;
        }
        return false;
    }
}
