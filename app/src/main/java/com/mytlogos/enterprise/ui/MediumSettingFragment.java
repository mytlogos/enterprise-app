package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.TimeAgo;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import org.joda.time.DateTime;

public class MediumSettingFragment extends BaseFragment {
    private ListsViewModel mediumViewModel;
    private LiveData<MediumSetting> liveMediumSettings;
    private Button openTocButton;
    private EditText editName;
    private RadioButton textMedium;
    private RadioButton imageMedium;
    private RadioButton videoMedium;
    private RadioButton audioMedium;
    private Switch autoDownload;
    private TextView series;
    private TextView universe;
    private TextView currentRead;
    private TextView lastEpisode;
    private TextView lastUpdated;
    private TextView average_release;
    private TextView author;
    private TextView artist;
    private TextView stateTl;
    private TextView stateOrigin;
    private TextView countryOfOrigin;
    private TextView languageOfOrigin;
    private TextView lang;
    private TextView additionalInfoBox;
    private TextView releaseRateBox;
    private View additionalInfoContainer;
    private static final String ID = "mediumId";

    static MediumSettingFragment newInstance(int id) {
        MediumSettingFragment settings = new MediumSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ID, id);
        settings.setArguments(bundle);
        return settings;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medium_settings, container, false);

        Bundle arguments = this.getArguments();

        if (arguments == null) {
            return view;
        }
        int mediumId = arguments.getInt(ID);

        this.mediumViewModel = new ViewModelProvider(this).get(ListsViewModel.class);
        this.liveMediumSettings = mediumViewModel.getMediumSettings(mediumId);

        this.liveMediumSettings.observe(getViewLifecycleOwner(), this::handleNewMediumSetting);

        this.openTocButton = view.findViewById(R.id.open_items_button);
        this.editName = view.findViewById(R.id.editName);
        this.textMedium = view.findViewById(R.id.text_medium);
        this.imageMedium = view.findViewById(R.id.image_medium);
        this.videoMedium = view.findViewById(R.id.video_medium);
        this.audioMedium = view.findViewById(R.id.audio_medium);
        this.autoDownload = view.findViewById(R.id.auto_download);
        this.series = view.findViewById(R.id.series);
        this.universe = view.findViewById(R.id.universe);
        this.currentRead = view.findViewById(R.id.currentRead);
        this.lastEpisode = view.findViewById(R.id.lastEpisode);
        this.lastUpdated = view.findViewById(R.id.lastUpdated);
        this.average_release = view.findViewById(R.id.average_release);
        this.author = view.findViewById(R.id.author);
        this.artist = view.findViewById(R.id.artist);
        this.stateTl = view.findViewById(R.id.stateTl);
        this.stateOrigin = view.findViewById(R.id.stateOrigin);
        this.countryOfOrigin = view.findViewById(R.id.countryOfOrigin);
        this.languageOfOrigin = view.findViewById(R.id.languageOfOrigin);
        this.lang = view.findViewById(R.id.lang);
        this.additionalInfoBox = view.findViewById(R.id.additional_info_box);
        this.additionalInfoContainer = view.findViewById(R.id.additional_info_container);
        this.releaseRateBox = view.findViewById(R.id.release_rate_box);
        this.checkSupportedMedia();


        this.openTocButton.setOnClickListener(v -> {
            if (this.mediumSettings() == null) {
                return;
            }
            this.getMainActivity().switchWindow(TocFragment.newInstance(mediumId), true);
        });
        this.additionalInfoBox.setOnClickListener(v -> toggleBox(this.additionalInfoContainer, this.additionalInfoBox));

        this.editName.setOnEditorActionListener((v, actionId, event) -> handleEditorEvent(this.editName, actionId, event));

        addMediumListener(this.textMedium, MediumType.TEXT);
        addMediumListener(this.imageMedium, MediumType.IMAGE);
        addMediumListener(this.videoMedium, MediumType.VIDEO);
        addMediumListener(this.audioMedium, MediumType.AUDIO);

        this.autoDownload.setOnCheckedChangeListener((buttonView, isChecked) -> handleAutoDownloadChanges(isChecked));
        this.setTitle("Medium Settings");
        return view;
    }

    private void toggleBox(View container, TextView box) {
        int visibility;
        int drawable;
        if (container.getVisibility() == View.GONE) {
            drawable = R.drawable.ic_minus_box_dark;
            visibility = View.VISIBLE;
        } else {
            drawable = R.drawable.ic_plus_box_dark;
            visibility = View.GONE;
        }
        container.setVisibility(visibility);
        box.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0);
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
        MediumSetting mediumSetting = this.mediumSettings();

        if (mediumSetting == null) {
            return;
        }
        if (mediumSetting.getToDownload() != isChecked) {
            int settingMediumId = mediumSetting.getMediumId();

            ToDownload toDownload = new ToDownload(false, settingMediumId, null, null);
            this.mediumViewModel.updateToDownload(isChecked, toDownload);
        }
    }

    private MediumSetting mediumSettings() {
        return this.liveMediumSettings.getValue();
    }

    @SuppressLint("SetTextI18n")
    private void handleNewMediumSetting(MediumSetting mediumSetting) {
        if (mediumSetting == null) {
            this.openTocButton.setEnabled(false);
            this.editName.setInputType(InputType.TYPE_NULL);

            this.textMedium.setEnabled(false);
            this.imageMedium.setEnabled(false);
            this.videoMedium.setEnabled(false);
            this.audioMedium.setEnabled(false);

            this.autoDownload.setEnabled(false);

            this.series.setText(R.string.not_available);
            this.universe.setText(R.string.not_available);
            this.currentRead.setText(R.string.not_available);
            this.lastEpisode.setText(R.string.not_available);
            this.lastUpdated.setText(R.string.not_available);
            this.average_release.setText(R.string.not_available);
            this.author.setText(R.string.not_available);
            this.artist.setText(R.string.not_available);
            this.stateTl.setText(R.string.not_available);
            this.stateOrigin.setText(R.string.not_available);
            this.countryOfOrigin.setText(R.string.not_available);
            this.languageOfOrigin.setText(R.string.not_available);
            this.lang.setText(R.string.not_available);
        } else {
            this.setTitle("Settings - " + mediumSetting.getTitle());
            this.editName.setText(mediumSetting.getTitle());
            this.editName.setInputType(InputType.TYPE_CLASS_TEXT);

            this.textMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.TEXT));
            this.imageMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.IMAGE));
            this.videoMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.VIDEO));
            this.audioMedium.setChecked(MediumType.is(mediumSetting.getMedium(), MediumType.AUDIO));

            this.autoDownload.setChecked(mediumSetting.getToDownload());

            this.autoDownload.setEnabled(true);
            this.textMedium.setEnabled(true);
            this.imageMedium.setEnabled(true);
            this.videoMedium.setEnabled(true);
            this.audioMedium.setEnabled(true);


            this.series.setText(this.defaultText(mediumSetting.getSeries()));
            this.universe.setText(this.defaultText(mediumSetting.getUniverse()));
            // TODO: 15.09.2019 check whether this id or index and change method name to reflect that
            this.currentRead.setText(Integer.toString(mediumSetting.getCurrentReadEpisode()));
            this.lastEpisode.setText(Integer.toString(mediumSetting.getLastEpisode()));
            this.lastUpdated.setText(this.defaultText(TimeAgo.toRelative(mediumSetting.getLastUpdated(), DateTime.now())));
            // TODO: 15.09.2019 calculate average
            this.average_release.setText(R.string.not_available);
            this.author.setText(this.defaultText(mediumSetting.getAuthor()));
            this.artist.setText(this.defaultText(mediumSetting.getArtist()));
            this.stateTl.setText((Integer.toString(mediumSetting.getStateTL())));
            this.stateOrigin.setText(Integer.toString(mediumSetting.getStateOrigin()));
            this.countryOfOrigin.setText(this.defaultText(mediumSetting.getCountryOfOrigin()));
            this.languageOfOrigin.setText(this.defaultText(mediumSetting.getLanguageOfOrigin()));
            this.lang.setText(this.defaultText(mediumSetting.getLang()));
        }
    }

    private String defaultText(String text) {
        return text == null || text.isEmpty() ? "N/A" : text;
    }

    private void addMediumListener(RadioButton button, int mediumType) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (this.mediumSettings() == null) {
                return;
            }
            this.checkSupportedMedia();

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
                                showToast(s);
                                editName.setText(currentMediumSetting.getTitle());

                            } else if (throwable != null) {
                                throwable.printStackTrace();

                                if (this.getContext() != null) {
                                    showToast("An Error occurred saving the new Title");
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
