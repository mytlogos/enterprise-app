package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.isAudioContentSupported
import com.mytlogos.enterprise.tools.isImageContentSupported
import com.mytlogos.enterprise.tools.isTextContentSupported
import com.mytlogos.enterprise.tools.isVideoContentSupported
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import kotlinx.coroutines.launch

class ListSettings : BaseFragment() {
    private lateinit var listsViewModel: ListsViewModel
    private lateinit var liveListSettings: LiveData<out MediaListSetting>
    private lateinit var openItemsButton: Button
    private lateinit var editName: EditText
    private lateinit var textMedium: CheckBox
    private lateinit var imageMedium: CheckBox
    private lateinit var videoMedium: CheckBox
    private lateinit var audioMedium: CheckBox
    private lateinit var autoDownload: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.list_settings, container, false)
        val arguments = requireArguments()
        val listId = arguments.getInt(ID)
        val isExternal = arguments.getBoolean(EXTERNAL)

        listsViewModel = ViewModelProvider(this).get(ListsViewModel::class.java)
        liveListSettings = listsViewModel.getListSettings(listId, isExternal)
        liveListSettings.observe(viewLifecycleOwner) {
                listSetting: MediaListSetting? -> handleNewListSetting(listSetting)
        }

        openItemsButton = view.findViewById(R.id.open_items_button)
        editName = view.findViewById(R.id.editName)
        textMedium = view.findViewById(R.id.text_medium)
        imageMedium = view.findViewById(R.id.image_medium)
        videoMedium = view.findViewById(R.id.video_medium)
        audioMedium = view.findViewById(R.id.audio_medium)
        autoDownload = view.findViewById(R.id.auto_download)

        checkSupportedMedia()

        openItemsButton.setOnClickListener {
            val listSetting = listSettings() ?: return@setOnClickListener

            val bundle = Bundle()
            bundle.putInt(ListMediumFragment.ID, listId)
            bundle.putString(ListMediumFragment.TITLE, listSetting.name)
            bundle.putBoolean(ListMediumFragment.EXTERNAL, isExternal)

            this.mainActivity.switchWindow(ListMediumFragment(), bundle, true)
        }
        editName.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            handleEditorEvent(editName, actionId, event)
        }

        addMediumListener(textMedium, TEXT)
        addMediumListener(imageMedium, IMAGE)
        addMediumListener(videoMedium, VIDEO)
        addMediumListener(audioMedium, AUDIO)

        autoDownload.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            handleAutoDownloadChanges(isChecked)
        }
        this.setTitle("List Settings")
        return view
    }

    private fun checkSupportedMedia() {
        autoDownload.isEnabled =
            (textMedium.isChecked && !isTextContentSupported
                    || autoDownload.isChecked && !isAudioContentSupported
                    || imageMedium.isChecked && !isImageContentSupported
                    || videoMedium.isChecked && !isVideoContentSupported)
    }

    private fun handleAutoDownloadChanges(isChecked: Boolean) {
        val listSettings = listSettings() ?: return

        if (listSettings.toDownload != isChecked) {
            val settingListId = listSettings.listId

            val toDownload: ToDownload = if (listSettings is ExternalMediaListSetting) {
                ToDownload(false, externalListId = settingListId)
            } else {
                ToDownload(false, listId = settingListId)
            }
            listsViewModel.updateToDownload(isChecked, toDownload)
        }
    }

    private fun listSettings(): MediaListSetting? {
        return liveListSettings.value
    }

    private fun handleNewListSetting(listSetting: MediaListSetting?) {
        if (listSetting == null) {
            openItemsButton.isEnabled = false
            editName.inputType = InputType.TYPE_NULL
            textMedium.isEnabled = false
            imageMedium.isEnabled = false
            videoMedium.isEnabled = false
            audioMedium.isEnabled = false
            autoDownload.isEnabled = false
        } else {
            editName.setText(listSetting.name)
            textMedium.isChecked = isType(listSetting.medium, TEXT)
            imageMedium.isChecked = isType(listSetting.medium, IMAGE)
            videoMedium.isChecked = isType(listSetting.medium, VIDEO)
            audioMedium.isChecked = isType(listSetting.medium, AUDIO)
            autoDownload.isChecked = listSetting.toDownload

            if (listSetting.isNameMutable) {
                editName.inputType = InputType.TYPE_CLASS_TEXT
            }
            if (listSetting.isMediumMutable) {
                textMedium.isEnabled = true
                imageMedium.isEnabled = true
                videoMedium.isEnabled = true
                audioMedium.isEnabled = true
            }
            if (listSetting.isToDownloadMutable) {
                autoDownload.isEnabled = true
            }
        }
    }

    private fun addMediumListener(box: CheckBox, mediumType: Int) {
        box.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val listSettings = listSettings()

            if (listSettings == null || !listSettings.isMediumMutable) {
                return@setOnCheckedChangeListener
            }
            checkSupportedMedia()

            val newMediumType = if (isChecked) {
                addMediumType(listSettings.medium, mediumType)
            } else {
                removeMediumType(listSettings.medium, mediumType)
            }
            lifecycleScope.launch {
                val result = runCatching {
                    listsViewModel.updateListMedium(listSettings, newMediumType)
                }

                if (result.isSuccess && result.getOrDefault("").isEmpty()) {
                    showToast(result.getOrDefault(""))
                } else if (result.isFailure) {
                    result.exceptionOrNull()?.printStackTrace()
                    showToast("An Error occurred saving the new Medium Type")
                }
            }
        }
    }

    private fun handleEditorEvent(editName: EditText, actionId: Int, event: KeyEvent?): Boolean {
        val listSettings = listSettings()

        if (listSettings != null && (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            // the user is done typing.
            val newName = editName.text.toString()

            if (newName.isEmpty()) {
                editName.setText(listSettings.name)
            } else if (newName != listSettings.name) {
                lifecycleScope.launch {
                    val result = runCatching {
                        listsViewModel.updateListName(listSettings, newName)
                    }

                    if (result.isSuccess && result.getOrDefault("").isEmpty()) {
                        showToast(result.getOrDefault(""))
                        editName.setText(listSettings.name)
                    } else if (result.isFailure) {
                        result.exceptionOrNull()?.printStackTrace()
                        showToast("An Error occurred saving the new Name")
                    }
                }
            }
            return true
        }
        return false
    }

    companion object {
        private const val ID = "id"
        private const val EXTERNAL = "external"
    }
}