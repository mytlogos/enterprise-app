package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.viewmodel.AddListViewModel
import kotlinx.coroutines.launch

class AddList : BaseFragment() {
    private lateinit var mViewModel: AddListViewModel
    private lateinit var autoDownload: SwitchCompat
    private lateinit var audioMedium: CheckBox
    private lateinit var videoMedium: CheckBox
    private lateinit var imageMedium: CheckBox
    private lateinit var textMedium: CheckBox
    private lateinit var editName: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.add_list_fragment, container, false)

        textMedium = view.findViewById(R.id.text_medium)
        imageMedium = view.findViewById(R.id.image_medium)
        videoMedium = view.findViewById(R.id.video_medium)
        audioMedium = view.findViewById(R.id.audio_medium)
        autoDownload = view.findViewById(R.id.auto_download)

        val addBtn = view.findViewById<Button>(R.id.add_btn)
        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)

        mViewModel = ViewModelProvider(this).get(AddListViewModel::class.java)
        cancelBtn.setOnClickListener { this.mainActivity.onBackPressed() }
        addBtn.setOnClickListener { addList() }

        val localEditName = view.findViewById<EditText>(R.id.editName)
        editName = localEditName

        localEditName.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                hideKeyboard()
            }
        }
        this.setTitle("Add List")
        return view
    }

    private fun addList() {
        val name = editName.text.toString().trim { it <= ' ' }
        if (name.isEmpty()) {
            showToast("No Name")
            return
        }
        var medium = 0
        when {
            textMedium.isChecked -> medium = medium or MediumType.TEXT
            audioMedium.isChecked -> medium = medium or MediumType.AUDIO
            imageMedium.isChecked -> medium = medium or MediumType.IMAGE
            videoMedium.isChecked -> medium = medium or MediumType.VIDEO
        }
        lifecycleScope.launch {
            val userNow = (RepositoryImpl.instance as RepositoryImpl).getUserNow()

            if (userNow == null) {
                showToast("User not authenticated")
                return@launch
            }

            val mediaList = MediaList(
                userNow.uuid,
                0,
                name,
                medium,
                0
            )
            mainActivity.showLoading(true)

            if (mViewModel.exists(name)) {
                showToast("List with the name '$name' exists already")
                return@launch
            }

            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                mViewModel.addList(mediaList, autoDownload.isChecked)
            } catch (e: Throwable) {
                e.printStackTrace()
                showToast("List could not be created")
            }

            mainActivity.showLoading(false)
            mainActivity.onBackPressed()
        }
    }

    companion object {
        fun newInstance(): AddList {
            return AddList()
        }
    }
}