package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ExternalMediaListSetting
import com.mytlogos.enterprise.model.MediaListSetting
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.MediumType.`is`
import com.mytlogos.enterprise.model.MediumType.addMediumType
import com.mytlogos.enterprise.model.MediumType.removeMediumType
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.tools.FileTools.isAudioContentSupported
import com.mytlogos.enterprise.tools.FileTools.isImageContentSupported
import com.mytlogos.enterprise.tools.FileTools.isTextContentSupported
import com.mytlogos.enterprise.tools.FileTools.isVideoContentSupported
import com.mytlogos.enterprise.viewmodel.ListsViewModel

class ListSettings : BaseFragment() {
    private var listsViewModel: ListsViewModel? = null
    private var liveListSettings: LiveData<out MediaListSetting>? = null
    private var openItemsButton: Button? = null
    private var editName: EditText? = null
    private var textMedium: CheckBox? = null
    private var imageMedium: CheckBox? = null
    private var videoMedium: CheckBox? = null
    private var audioMedium: CheckBox? = null
    private var autoDownload: Switch? = null

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
        liveListSettings = listsViewModel!!.getListSettings(listId, isExternal)
        liveListSettings!!.observe(viewLifecycleOwner,
            { listSetting: MediaListSetting? -> handleNewListSetting(listSetting) })
        openItemsButton = view.findViewById(R.id.open_items_button)
        editName = view.findViewById(R.id.editName)
        textMedium = view.findViewById(R.id.text_medium)
        imageMedium = view.findViewById(R.id.image_medium)
        videoMedium = view.findViewById(R.id.video_medium)
        audioMedium = view.findViewById(R.id.audio_medium)
        autoDownload = view.findViewById(R.id.auto_download)
        checkSupportedMedia()
        openItemsButton!!.setOnClickListener {
            val listSetting = listSettings() ?: return@setOnClickListener
            val bundle = Bundle()
            bundle.putInt(ListMediumFragment.ID, listId)
            bundle.putString(ListMediumFragment.TITLE, listSetting.name)
            bundle.putBoolean(ListMediumFragment.EXTERNAL, isExternal)
            this.mainActivity.switchWindow(ListMediumFragment(), bundle, true)
        }
        editName!!.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            handleEditorEvent(editName,
                actionId,
                event)
        }
        addMediumListener(textMedium, MediumType.TEXT)
        addMediumListener(imageMedium, MediumType.IMAGE)
        addMediumListener(videoMedium, MediumType.VIDEO)
        addMediumListener(audioMedium, MediumType.AUDIO)
        autoDownload!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            handleAutoDownloadChanges(isChecked)
        }
        this.setTitle("List Settings")
        return view
    }

    private fun checkSupportedMedia() {
        autoDownload!!.isEnabled =
            (textMedium!!.isChecked && !isTextContentSupported
                    || autoDownload!!.isChecked && !isAudioContentSupported
                    || imageMedium!!.isChecked && !isImageContentSupported
                    || videoMedium!!.isChecked && !isVideoContentSupported)
    }

    private fun handleAutoDownloadChanges(isChecked: Boolean) {
        if (listSettings() == null) {
            return
        }
        if (listSettings()!!.toDownload != isChecked) {
            val settingListId = listSettings()!!.listId
            val toDownload: ToDownload = if (listSettings() is ExternalMediaListSetting) {
                ToDownload(false, null, null, settingListId)
            } else {
                ToDownload(false, null, settingListId, null)
            }
            listsViewModel!!.updateToDownload(isChecked, toDownload)
        }
    }

    private fun listSettings(): MediaListSetting? {
        return liveListSettings!!.value
    }

    private fun handleNewListSetting(listSetting: MediaListSetting?) {
        if (listSetting == null) {
            openItemsButton!!.isEnabled = false
            editName!!.inputType = InputType.TYPE_NULL
            textMedium!!.isEnabled = false
            imageMedium!!.isEnabled = false
            videoMedium!!.isEnabled = false
            audioMedium!!.isEnabled = false
            autoDownload!!.isEnabled = false
        } else {
            editName!!.setText(listSetting.name)
            textMedium!!.isChecked = `is`(listSetting.medium, MediumType.TEXT)
            imageMedium!!.isChecked = `is`(listSetting.medium, MediumType.IMAGE)
            videoMedium!!.isChecked = `is`(listSetting.medium, MediumType.VIDEO)
            audioMedium!!.isChecked = `is`(listSetting.medium, MediumType.AUDIO)
            autoDownload!!.isChecked = listSetting.toDownload
            if (listSetting.isNameMutable) {
                editName!!.inputType = InputType.TYPE_CLASS_TEXT
            }
            if (listSetting.isMediumMutable) {
                textMedium!!.isEnabled = true
                imageMedium!!.isEnabled = true
                videoMedium!!.isEnabled = true
                audioMedium!!.isEnabled = true
            }
            if (listSetting.isToDownloadMutable) {
                autoDownload!!.isEnabled = true
            }
        }
    }

    private fun addMediumListener(box: CheckBox?, mediumType: Int) {
        box!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (listSettings() == null || !listSettings()!!.isMediumMutable) {
                return@setOnCheckedChangeListener
            }
            checkSupportedMedia()
            val newMediumType: Int = if (isChecked) {
                addMediumType(listSettings()!!.medium, mediumType)
            } else {
                removeMediumType(listSettings()!!.medium, mediumType)
            }
            listsViewModel!!.updateListMedium(listSettings(), newMediumType)
        }
    }

    private fun handleEditorEvent(editName: EditText?, actionId: Int, event: KeyEvent?): Boolean {
        if (listSettings() != null && (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            // the user is done typing.
            val newName = editName!!.text.toString()
            if (newName.isEmpty()) {
                editName.setText(listSettings()!!.name)
            } else if (newName != listSettings()!!.name) {
                listsViewModel!!
                    .updateListName(listSettings(), newName)
                    .handle<Any?> { s: String?, throwable: Throwable? ->
                        if (this.context != null && s != null && s.isNotEmpty()) {
                            showToast(s)
                            editName.setText(listSettings()!!.name)
                        } else if (throwable != null) {
                            throwable.printStackTrace()
                            if (this.context != null) {
                                showToast("An Error occurred saving the new Name")
                            }
                        }
                        null
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