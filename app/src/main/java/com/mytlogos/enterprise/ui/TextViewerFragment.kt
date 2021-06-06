package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.SimpleEpisode
import com.mytlogos.enterprise.tools.FileTools.textContentTool

/**
 * A simple [Fragment] subclass.
 * Use the [TextViewerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class TextViewerFragment : ViewerFragment<TextViewerFragment.ReadableEpisode>() {
    private var textDisplay: TextView? = null
    private var scrollView: ScrollView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = super.onCreateView(inflater, container, savedInstanceState)
        textDisplay = view.findViewById(R.id.display) as TextView?
        this.scrollView = view.findViewById(R.id.scroller) as ScrollView?
        textDisplay!!.movementMethod = ScrollingMovementMethod()
        setHasOptionsMenu(true)
        /*this.scrollView.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        this.onScroll(scrollX, scrollY, oldScrollX, oldScrollY)
        );*/textDisplay!!.setOnClickListener { toggleReadingMode() }
        loadZip()
        return view
    }

    override val scrolledViewId: Int
        get() = R.id.scroller
    override val currentProgress: Float
        get() = if (currentlyReading != null) currentlyReading!!.progress else 0.0f

    override fun saveProgress(progress: Float) {
        if (currentEpisode > 0) {
            instance.updateProgress(currentEpisode, progress)
        }
    }

    override val layoutRes: Int
        get() = R.layout.fragment_reader_text

    override fun updateContent() {
        OpenEpisodeTask().execute()
    }

    class ReadableEpisode(episode: SimpleEpisode, val file: String?) :
        SimpleEpisode(episode.episodeId, episode.totalIndex, episode.partialIndex, episode.progress)

    @SuppressLint("DefaultLocale")
    private fun displayData(data: CharSequence) {
        if (currentlyReading != null) {
            currentEpisode = currentlyReading!!.episodeId
            if (currentlyReading!!.partialIndex > 0) {
                setTitle(String.format("Episode %d.%d",
                    currentlyReading!!.totalIndex,
                    currentlyReading!!.partialIndex))
            } else {
                setTitle(String.format("Episode %d", currentlyReading!!.totalIndex))
            }
        } else {
            setTitle("No Episode found")
        }
        // this does not work really, can't scroll to the bottom
        // and displays characters like ' or ´ incorrectly
        textDisplay!!.text = data
        this.scrollView!!.scrollTo(0, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.text_viewer_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.font) {
            changeFont()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun changeFont() {
        val context: Activity = requireActivity()
        @SuppressLint("InflateParams") val view =
            context.layoutInflater.inflate(R.layout.change_font, null)
        val fontTextView = view.findViewById(R.id.font_size) as EditText
        fontTextView.setText(textDisplay!!.textSize.toString() + "")
        (view.findViewById(R.id.increment_font) as View).setOnClickListener { v: View? ->
            try {
                var currentFont = fontTextView.text.toString().toInt()
                currentFont++
                if (currentFont > 40) {
                    currentFont = 14
                }
                fontTextView.setText(currentFont.toString() + "")
                textDisplay!!.textSize = currentFont.toFloat()
            } catch (e: NumberFormatException) {
                fontTextView.setText("0")
            }
        }
        (view.findViewById(R.id.decrement_font) as View).setOnClickListener { v: View? ->
            try {
                var currentFont = fontTextView.text.toString().toInt()
                currentFont--
                if (currentFont < 2) {
                    currentFont = 14
                }
                fontTextView.setText(currentFont.toString() + "")
                textDisplay!!.textSize = currentFont.toFloat()
            } catch (e: NumberFormatException) {
                fontTextView.setText("0")
            }
        }
        AlertDialog.Builder(context)
            .setView(view)
            .setTitle("Modify Font")
            .show()
    }

    private fun processData(input: String): CharSequence {
        var data: String? = input
        if (data != null && data.length < 200) {
            showToast(data)
            data = null
        }
        return if (data == null) {
            "No Content Found."
        } else {
            try {
                HtmlCompat.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } catch (ignored: Exception) {
                data
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class OpenEpisodeTask : AsyncTask<Void, Void, CharSequence>() {
        override fun doInBackground(vararg voids: Void?): CharSequence {
            val bookTool = textContentTool
            val data = bookTool.openEpisode(currentBook, currentlyReading!!.file)
            return processData(data)
        }

        override fun onPostExecute(data: CharSequence) {
            displayData(data)
            onLoadFinished()
        }
    }

    private fun loadZip() {
        @SuppressLint("StaticFieldLeak") val task: AsyncTask<Void, Void, CharSequence> =
            object : AsyncTask<Void, Void, CharSequence>() {
                override fun doInBackground(vararg voids: Void): CharSequence {
                    val bookTool = textContentTool
                    val episodeFileMap: Map<Int, String> = bookTool.getEpisodePaths(
                        currentBook)
                    if (episodeFileMap.isEmpty()) {
                        return ""
                    }
                    val repository = getInstance(mainActivity.application)
                    val episodes = repository.getSimpleEpisodes(episodeFileMap.keys)
                    for (simpleEpisode in episodes) {
                        val episodeId = simpleEpisode.episodeId
                        val file = episodeFileMap[episodeId]
                        if (file == null || file.isEmpty()) {
                            System.err.println("Could not find file for episodeId: $episodeId")
                            continue
                        }
                        val readableEpisode = ReadableEpisode(simpleEpisode, file)
                        if (episodeId == currentEpisode) {
                            currentlyReading = readableEpisode
                        }
                        readableEpisodes.add(readableEpisode)
                    }
                    if (currentlyReading == null || currentlyReading!!.file == null || currentlyReading!!.file!!.isEmpty()) {
                        return "Selected Episode is not available"
                    }
                    val data = bookTool.openEpisode(currentBook, currentlyReading!!.file)
                    return processData(data)
                }

                @SuppressLint("DefaultLocale")
                override fun onPostExecute(data: CharSequence) {
                    displayData(data)
                    onLoadFinished()
                }

                override fun onCancelled() {
                    showToastError("Could not load Book")
                }
            }
        task.execute()
    }

    private fun showToastError(s: String) {
        requireActivity().runOnUiThread { showToast(s) }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TextViewerFragment.
         */
        fun newInstance(startEpisode: Int, zipFile: String?): TextViewerFragment {
            val fragment: TextViewerFragment = if (zipFile != null && zipFile.endsWith(".pdf")) {
                PdfViewerFragment()
            } else {
                TextViewerFragment()
            }
            val args = Bundle()
            args.putInt(START_EPISODE, startEpisode)
            args.putString(MEDIUM, zipFile)
            fragment.arguments = args
            return fragment
        }
    }
}