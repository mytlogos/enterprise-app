package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.TimeAgo
import com.mytlogos.enterprise.model.MediumSetting
import com.mytlogos.enterprise.model.MediumSetting.MediumSettingBuilder
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.MediumType.isType
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.tools.FileTools.isAudioContentSupported
import com.mytlogos.enterprise.tools.FileTools.isImageContentSupported
import com.mytlogos.enterprise.tools.FileTools.isTextContentSupported
import com.mytlogos.enterprise.tools.FileTools.isVideoContentSupported
import com.mytlogos.enterprise.viewmodel.ListsViewModel
import org.joda.time.DateTime

class MediumSettingFragment : BaseFragment() {
    private var mediumViewModel: ListsViewModel? = null
    private var liveMediumSettings: LiveData<MediumSetting>? = null
    private var openTocButton: Button? = null
    private var editName: EditText? = null
    private var textMedium: RadioButton? = null
    private var imageMedium: RadioButton? = null
    private var videoMedium: RadioButton? = null
    private var audioMedium: RadioButton? = null
    private var autoDownload: Switch? = null
    private var series: TextView? = null
    private var universe: TextView? = null
    private var currentRead: TextView? = null
    private var lastEpisode: TextView? = null
    private var lastUpdated: TextView? = null
    private var average_release: TextView? = null
    private var author: TextView? = null
    private var artist: TextView? = null
    private var stateTl: TextView? = null
    private var stateOrigin: TextView? = null
    private var countryOfOrigin: TextView? = null
    private var languageOfOrigin: TextView? = null
    private var lang: TextView? = null
    private var additionalInfoBox: TextView? = null
    private var releaseRateBox: TextView? = null
    private var additionalInfoContainer: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.medium_settings, container, false)
        val arguments = this.arguments ?: return view
        val mediumId = arguments.getInt(ID)
        mediumViewModel = ViewModelProvider(this).get(ListsViewModel::class.java)
        liveMediumSettings = mediumViewModel!!.getMediumSettings(mediumId)
        liveMediumSettings!!.observe(viewLifecycleOwner,
            { mediumSetting: MediumSetting? -> handleNewMediumSetting(mediumSetting) })
        openTocButton = view.findViewById(R.id.open_items_button) as Button?
        editName = view.findViewById(R.id.editName) as EditText?
        textMedium = view.findViewById(R.id.text_medium) as RadioButton?
        imageMedium = view.findViewById(R.id.image_medium) as RadioButton?
        videoMedium = view.findViewById(R.id.video_medium) as RadioButton?
        audioMedium = view.findViewById(R.id.audio_medium) as RadioButton?
        autoDownload = view.findViewById(R.id.auto_download) as Switch?
        series = view.findViewById(R.id.series) as TextView?
        universe = view.findViewById(R.id.universe) as TextView?
        currentRead = view.findViewById(R.id.currentRead) as TextView?
        lastEpisode = view.findViewById(R.id.lastEpisode) as TextView?
        lastUpdated = view.findViewById(R.id.lastUpdated) as TextView?
        average_release = view.findViewById(R.id.average_release) as TextView?
        author = view.findViewById(R.id.author) as TextView?
        artist = view.findViewById(R.id.artist) as TextView?
        stateTl = view.findViewById(R.id.stateTl) as TextView?
        stateOrigin = view.findViewById(R.id.stateOrigin) as TextView?
        countryOfOrigin = view.findViewById(R.id.countryOfOrigin) as TextView?
        languageOfOrigin = view.findViewById(R.id.languageOfOrigin) as TextView?
        lang = view.findViewById(R.id.lang) as TextView?
        additionalInfoBox = view.findViewById(R.id.additional_info_box) as TextView?
        additionalInfoContainer = view.findViewById(R.id.additional_info_container)
        releaseRateBox = view.findViewById(R.id.release_rate_box) as TextView
        checkSupportedMedia()
        openTocButton!!.setOnClickListener {
            if (mediumSettings() == null) {
                return@setOnClickListener
            }
            this.mainActivity.switchWindow(TocFragment.newInstance(mediumId), true)
        }
        additionalInfoBox!!.setOnClickListener {
            toggleBox(
                additionalInfoContainer, additionalInfoBox)
        }
        editName!!.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            handleEditorEvent(
                editName, actionId, event)
        }
        addMediumListener(textMedium, MediumType.TEXT)
        addMediumListener(imageMedium, MediumType.IMAGE)
        addMediumListener(videoMedium, MediumType.VIDEO)
        addMediumListener(audioMedium, MediumType.AUDIO)
        autoDownload!!.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            handleAutoDownloadChanges(isChecked)
        }
        this.setTitle("Medium Settings")
        return view
    }

    private fun toggleBox(container: View?, box: TextView?) {
        val visibility: Int
        val drawable: Int
        if (container!!.visibility == View.GONE) {
            drawable = R.drawable.ic_minus_box_dark
            visibility = View.VISIBLE
        } else {
            drawable = R.drawable.ic_plus_box_dark
            visibility = View.GONE
        }
        container.visibility = visibility
        box!!.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun checkSupportedMedia() {
        autoDownload!!.isEnabled = (textMedium!!.isChecked && !isTextContentSupported
                || autoDownload!!.isChecked && !isAudioContentSupported
                || imageMedium!!.isChecked && !isImageContentSupported
                || videoMedium!!.isChecked && !isVideoContentSupported)
    }

    private fun handleAutoDownloadChanges(isChecked: Boolean) {
        val mediumSetting = mediumSettings() ?: return
        if (mediumSetting.toDownload != isChecked) {
            val settingMediumId = mediumSetting.mediumId
            val toDownload = ToDownload(false, settingMediumId, null, null)
            mediumViewModel!!.updateToDownload(isChecked, toDownload)
        }
    }

    private fun mediumSettings(): MediumSetting? {
        return liveMediumSettings!!.value
    }

    @SuppressLint("SetTextI18n")
    private fun handleNewMediumSetting(mediumSetting: MediumSetting?) {
        if (mediumSetting == null) {
            openTocButton!!.isEnabled = false
            editName!!.inputType = InputType.TYPE_NULL
            textMedium!!.isEnabled = false
            imageMedium!!.isEnabled = false
            videoMedium!!.isEnabled = false
            audioMedium!!.isEnabled = false
            autoDownload!!.isEnabled = false
            series!!.setText(R.string.not_available)
            universe!!.setText(R.string.not_available)
            currentRead!!.setText(R.string.not_available)
            lastEpisode!!.setText(R.string.not_available)
            lastUpdated!!.setText(R.string.not_available)
            average_release!!.setText(R.string.not_available)
            author!!.setText(R.string.not_available)
            artist!!.setText(R.string.not_available)
            stateTl!!.setText(R.string.not_available)
            stateOrigin!!.setText(R.string.not_available)
            countryOfOrigin!!.setText(R.string.not_available)
            languageOfOrigin!!.setText(R.string.not_available)
            lang!!.setText(R.string.not_available)
        } else {
            this.setTitle("Settings - " + mediumSetting.getTitle())
            editName!!.setText(mediumSetting.getTitle())
            editName!!.inputType = InputType.TYPE_CLASS_TEXT
            textMedium!!.isChecked = isType(mediumSetting.medium, MediumType.TEXT)
            imageMedium!!.isChecked = isType(mediumSetting.medium, MediumType.IMAGE)
            videoMedium!!.isChecked = isType(mediumSetting.medium, MediumType.VIDEO)
            audioMedium!!.isChecked = isType(mediumSetting.medium, MediumType.AUDIO)
            autoDownload!!.isChecked = mediumSetting.toDownload
            autoDownload!!.isEnabled = true
            textMedium!!.isEnabled = true
            imageMedium!!.isEnabled = true
            videoMedium!!.isEnabled = true
            audioMedium!!.isEnabled = true
            series!!.text = defaultText(mediumSetting.getSeries())
            universe!!.text = defaultText(mediumSetting.getUniverse())
            // TODO: 15.09.2019 check whether this id or index and change method name to reflect that
            currentRead!!.text = Integer.toString(mediumSetting.currentReadEpisode)
            lastEpisode!!.text = Integer.toString(mediumSetting.lastEpisode)
            lastUpdated!!.text = defaultText(TimeAgo.toRelative(mediumSetting.getLastUpdated(),
                DateTime.now()))
            // TODO: 15.09.2019 calculate average
            average_release!!.setText(R.string.not_available)
            author!!.text = defaultText(mediumSetting.getAuthor())
            artist!!.text = defaultText(mediumSetting.getArtist())
            stateTl!!.text = Integer.toString(mediumSetting.stateTL)
            stateOrigin!!.text = Integer.toString(mediumSetting.stateOrigin)
            countryOfOrigin!!.text = defaultText(mediumSetting.getCountryOfOrigin())
            languageOfOrigin!!.text = defaultText(mediumSetting.getLanguageOfOrigin())
            lang!!.text = defaultText(mediumSetting.getLang())
        }
    }

    private fun defaultText(text: String?): String {
        return if (text == null || text.isEmpty()) "N/A" else text
    }

    private fun addMediumListener(button: RadioButton?, mediumType: Int) {
        button!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (mediumSettings() == null) {
                return@setOnCheckedChangeListener
            }
            checkSupportedMedia()
            if (isChecked) {
                val setting = MediumSettingBuilder(mediumSettings()!!)
                    .setMedium(mediumType)
                    .createMediumSetting()
                mediumViewModel!!.updateMedium(setting)
            }
        }
    }

    private fun handleEditorEvent(editName: EditText?, actionId: Int, event: KeyEvent?): Boolean {
        val currentMediumSetting = mediumSettings()
        if (currentMediumSetting != null && (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER)
        ) {
            // the user is done typing.
            val newTitle = editName!!.text.toString()
            if (newTitle.isEmpty()) {
                editName.setText(currentMediumSetting.getTitle())
            } else if (newTitle != currentMediumSetting.getTitle()) {
                val setting = MediumSettingBuilder(currentMediumSetting)
                    .setTitle(newTitle)
                    .createMediumSetting()
                mediumViewModel!!
                    .updateMedium(setting)
                    .handle<Any?> { s: String?, throwable: Throwable? ->
                        if (this.context != null && s != null && !s.isEmpty()) {
                            showToast(s)
                            editName.setText(currentMediumSetting.getTitle())
                        } else if (throwable != null) {
                            throwable.printStackTrace()
                            if (this.context != null) {
                                showToast("An Error occurred saving the new Title")
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
        private const val ID = "mediumId"
        fun newInstance(id: Int): MediumSettingFragment {
            val settings = MediumSettingFragment()
            val bundle = Bundle()
            bundle.putInt(ID, id)
            settings.arguments = bundle
            return settings
        }
    }
}