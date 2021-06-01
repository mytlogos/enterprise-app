package com.mytlogos.enterprise.ui

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.viewmodel.AddListViewModel

class AddList : BaseFragment() {
    private var mViewModel: AddListViewModel? = null
    private var autoDownload: Switch? = null
    private var audioMedium: CheckBox? = null
    private var videoMedium: CheckBox? = null
    private var imageMedium: CheckBox? = null
    private var textMedium: CheckBox? = null
    private var editName: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        val name = editName!!.text.toString().trim { it <= ' ' }
        if (name.isEmpty()) {
            showToast("No Name")
            return
        }
        var medium = 0
        when {
            textMedium!!.isChecked -> medium = medium or MediumType.TEXT
            audioMedium!!.isChecked -> medium = medium or MediumType.AUDIO
            imageMedium!!.isChecked -> medium = medium or MediumType.IMAGE
            videoMedium!!.isChecked -> medium = medium or MediumType.VIDEO
        }
        val uuid = (RepositoryImpl.instance as RepositoryImpl).getUserNow()!!.uuid

        val mediaList = MediaList(
            uuid,
            0,
            name,
            medium,
            0
        )
        val task = AddListTask(this, mediaList, autoDownload!!.isChecked)
        task.execute()
        this.mainActivity.showLoading(true)
    }

    private class AddListTask(
        private val addList: AddList,
        private val mediaList: MediaList,
        private val autoDownload: Boolean
    ) : AsyncTask<Void?, Void?, Void?>() {
        private var errorMessage: String? = null

        override fun doInBackground(vararg voids: Void?): Void? {
            val name = mediaList.name
            if (addList.mViewModel!!.exists(name)) {
                errorMessage = String.format("List with the name '%s' exists already", name)
                return null
            }
            try {
                addList.mViewModel!!.addList(mediaList, autoDownload)
            } catch (e: Throwable) {
                e.printStackTrace()
                errorMessage = "List could not be created"
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (errorMessage != null) {
                addList.showToast(errorMessage)
            }
            addList.mainActivity.showLoading(false)
            addList.mainActivity.onBackPressed()
        }
    }

    companion object {
        fun newInstance(): AddList {
            return AddList()
        }
    }
}