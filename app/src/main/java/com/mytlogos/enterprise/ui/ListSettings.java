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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ExternalMediaListSetting;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import java.util.Objects;

public class ListSettings extends BaseFragment {
    private static final String ID = "id";
    private static final String EXTERNAL = "external";

    private ListsViewModel listsViewModel;
    private LiveData<? extends MediaListSetting> liveListSettings;
    private Button openItemsButton;
    private EditText editName;
    private CheckBox textMedium;
    private CheckBox imageMedium;
    private CheckBox videoMedium;
    private CheckBox audioMedium;
    private Switch autoDownload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_settings, container, false);

        Bundle arguments = Objects.requireNonNull(this.getArguments());
        int listId = arguments.getInt(ID);
        boolean isExternal = arguments.getBoolean(EXTERNAL);

        this.listsViewModel = ViewModelProviders.of(this).get(ListsViewModel.class);
        this.liveListSettings = listsViewModel.getListSettings(listId, isExternal);

        this.liveListSettings.observe(this, this::handleNewListSetting);

        this.openItemsButton = view.findViewById(R.id.open_items_button);
        this.editName = view.findViewById(R.id.editName);
        this.textMedium = view.findViewById(R.id.text_medium);
        this.imageMedium = view.findViewById(R.id.image_medium);
        this.videoMedium = view.findViewById(R.id.video_medium);
        this.audioMedium = view.findViewById(R.id.audio_medium);
        this.autoDownload = view.findViewById(R.id.auto_download);
        checkSupportedMedia();

        this.openItemsButton.setOnClickListener(v -> {
            MediaListSetting listSetting = this.listSettings();
            if (listSetting == null) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(ListMediumFragment.ID, listId);
            bundle.putString(ListMediumFragment.TITLE, listSetting.getName());
            bundle.putBoolean(ListMediumFragment.EXTERNAL, isExternal);
            this.getMainActivity().switchWindow(new ListMediumFragment(), bundle, true);
        });

        this.editName.setOnEditorActionListener((v, actionId, event) -> handleEditorEvent(editName, actionId, event));

        addMediumListener(textMedium, MediumType.TEXT);
        addMediumListener(imageMedium, MediumType.IMAGE);
        addMediumListener(videoMedium, MediumType.VIDEO);
        addMediumListener(audioMedium, MediumType.AUDIO);

        this.autoDownload.setOnCheckedChangeListener((buttonView, isChecked) -> handleAutoDownloadChanges(isChecked));
        this.setTitle("List Settings");
        return view;
    }

    private void checkSupportedMedia() {
        this.autoDownload.setEnabled(
                (this.textMedium.isChecked() && !FileTools.isTextContentSupported())
                        || (this.autoDownload.isChecked() && !FileTools.isAudioContentSupported())
                        || (this.imageMedium.isChecked() && !FileTools.isImageContentSupported())
                        || (this.videoMedium.isChecked() && !FileTools.isVideoContentSupported())
        );
    }

    private void handleAutoDownloadChanges(boolean isChecked) {
        if (this.listSettings() == null) {
            return;
        }
        if (this.listSettings().isToDownload() != isChecked) {
            int settingListId = this.listSettings().getListId();

            ToDownload toDownload;
            if (this.listSettings() instanceof ExternalMediaListSetting) {
                toDownload = new ToDownload(false, null, null, settingListId);
            } else {
                toDownload = new ToDownload(false, null, settingListId, null);
            }
            this.listsViewModel.updateToDownload(isChecked, toDownload);
        }
    }

    private MediaListSetting listSettings() {
        return this.liveListSettings.getValue();
    }

    private void handleNewListSetting(MediaListSetting listSetting) {
        if (listSetting == null) {
            this.openItemsButton.setEnabled(false);
            this.editName.setInputType(InputType.TYPE_NULL);
            this.textMedium.setEnabled(false);
            this.imageMedium.setEnabled(false);
            this.videoMedium.setEnabled(false);
            this.audioMedium.setEnabled(false);
            this.autoDownload.setEnabled(false);
        } else {
            this.editName.setText(listSetting.getName());
            this.textMedium.setChecked(MediumType.is(listSetting.getMedium(), MediumType.TEXT));
            this.imageMedium.setChecked(MediumType.is(listSetting.getMedium(), MediumType.IMAGE));
            this.videoMedium.setChecked(MediumType.is(listSetting.getMedium(), MediumType.VIDEO));
            this.audioMedium.setChecked(MediumType.is(listSetting.getMedium(), MediumType.AUDIO));
            this.autoDownload.setChecked(listSetting.isToDownload());

            if (listSetting.isNameMutable()) {
                this.editName.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            if (listSetting.isMediumMutable()) {
                this.textMedium.setEnabled(true);
                this.imageMedium.setEnabled(true);
                this.videoMedium.setEnabled(true);
                this.audioMedium.setEnabled(true);

            }

            if (listSetting.isToDownloadMutable()) {
                this.autoDownload.setEnabled(true);
            }
        }
    }

    private void addMediumListener(CheckBox box, int mediumType) {
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.listSettings() == null || !this.listSettings().isMediumMutable()) {
                return;
            }
            checkSupportedMedia();

            int newMediumType;

            if (isChecked) {
                newMediumType = MediumType.addMediumType(this.listSettings().getMedium(), mediumType);
            } else {
                newMediumType = MediumType.removeMediumType(this.listSettings().getMedium(), mediumType);
            }

            this.listsViewModel.updateListMedium(this.listSettings(), newMediumType);
        });

    }

    private boolean handleEditorEvent(EditText editName, int actionId, KeyEvent event) {
        if ((this.listSettings() != null) && ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
                (actionId == EditorInfo.IME_ACTION_DONE) ||
                ((event != null) &&
                        (event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {
            // the user is done typing.

            String newName = editName.getText().toString();

            if (newName.isEmpty()) {
                editName.setText(this.listSettings().getName());

            } else if (!newName.equals(this.listSettings().getName())) {

                this.listsViewModel
                        .updateListName(this.listSettings(), newName)
                        .handle((s, throwable) -> {
                            if (this.getContext() != null && s != null && !s.isEmpty()) {
                                showToast(s);
                                editName.setText(this.listSettings().getName());

                            } else if (throwable != null) {
                                throwable.printStackTrace();
                                if (this.getContext() != null) {
                                    showToast("An Error occurred saving the new Name");
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
