package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.tools.ContentTool
import com.mytlogos.enterprise.tools.getAudioContentTool
import com.mytlogos.enterprise.tools.getImageContentTool
import com.mytlogos.enterprise.tools.getSupportedContentTools
import com.mytlogos.enterprise.tools.getTextContentTool
import com.mytlogos.enterprise.tools.getVideoContentTool
import com.mytlogos.enterprise.tools.humanReadableByteCount
import com.mytlogos.enterprise.worker.CheckSavedWorker.Companion.checkLocal
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.PieChartView
import java.io.File
import java.util.*

class SpaceViewFragment : BaseFragment() {
    private val sliceValueSpaceDataNodeMap: MutableMap<SliceValue, SpaceDataNode> = HashMap()
    private val textNode = SpaceDataNode("Books", 0)
    private val audioNode = SpaceDataNode("Audio", 0)
    private val videoNode = SpaceDataNode("Video", 0)
    private val imageNode = SpaceDataNode("Images", 0)
    private val root = SpaceDataNode("Media")

    private var currentNode: SpaceDataNode? = null
    private var selectedNode: SpaceDataNode? = null
    private var viewedNode: SpaceDataNode? = null

    private lateinit var chart: PieChartView
    private lateinit var view: SwipeRefreshLayout
    private lateinit var selectedTitle: TextView
    private lateinit var title: TextView
    private lateinit var clearBtn: Button
    private lateinit var viewSelected: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(
            R.layout.space_view_fragment,
            container,
            false
        ) as SwipeRefreshLayout

        chart = view.findViewById(R.id.chart)
        selectedTitle = view.findViewById(R.id.selected_title)
        title = view.findViewById(R.id.title)
        (view.findViewById(R.id.container) as View).setOnClickListener { deselectNode() }

        val previousViewed = view.findViewById(R.id.previous_view) as TextView
        previousViewed.setOnClickListener {
            if (viewedNode == null || viewedNode!!.parent == null) {
                return@setOnClickListener
            }
            sliceValueSpaceDataNodeMap.clear()
            viewedNode = viewedNode!!.parent
            deselectNode()
            updateData(viewedNode)
            previousViewed.visibility =
                if (currentNode!!.parent == null) View.GONE else View.VISIBLE
            previousViewed.text =
                if (currentNode!!.parent == null) null else currentNode!!.parent!!.name
        }

        viewSelected = view.findViewById(R.id.view_selected) as Button
        viewSelected.setOnClickListener {
            if (selectedNode == null) {
                return@setOnClickListener
            }
            viewedNode = selectedNode
            deselectNode()
            updateData(viewedNode)
            previousViewed.visibility = View.VISIBLE
            previousViewed.text = currentNode!!.parent!!.name
        }
        clearBtn = view.findViewById(R.id.clear_all_local_btn) as Button
        clearBtn.setOnClickListener {
            val node = (if (selectedNode == null) currentNode else selectedNode)
                ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("""
                    Do you really want to delete 
                    '${node.hierarchyName()}' ?
                    """.trimIndent()
                )
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int -> clearNode(node) }
                .setNegativeButton("No", null)
                .show()
        }
        view.isRefreshing = true
        GatherDataTask().execute()

        chart.isValueSelectionEnabled = true
        chart.onValueTouchListener = object : PieChartOnValueSelectListener {
            override fun onValueSelected(arcIndex: Int, value: SliceValue) {
                val node = sliceValueSpaceDataNodeMap[value] ?: return
                selectedNode = node
                viewSelected.isEnabled = node.children.isNotEmpty()
                selectedTitle.text = String.format("%s (%s MB)", node.name, node.sizeMB)
                selectedTitle.visibility = View.VISIBLE
                clearBtn.text = String.format("Clear %s", String(value.labelAsChars))
                clearBtn.isEnabled = true
            }

            override fun onValueDeselected() {
                deselectNode()
            }
        }
        view.setOnRefreshListener { GatherDataTask().execute() }
        return view
    }

    private fun clearNode(node: SpaceDataNode) {
        val application = requireActivity().application
        val task: AsyncTask<Void, Void, String>

        when {
            node == root -> {
                task = ClearRootTask(getSupportedContentTools(application), this.requireContext())
            }
            node is MediumNode -> {
                val tool = getContentTool(node.parent)
                task = ClearMediaTask(node.id, tool, this.requireContext())
            }
            node is EpisodeNode -> {
                val id = node.id
                val episodeParent = node.parent
                if (episodeParent !is MediumNode) {
                    throw Error("episode node which is no child of medium node")
                }

                val parent = episodeParent.parent

                val tool = getContentTool(parent)
                task = ClearEpisodeTask(episodeParent.id, id, tool, this.requireContext())
            }
            node.parent == null -> {
                val tool = getContentTool(node)
                task = ClearRootTask(setOf(tool), this.requireContext())
            }
            else -> {
                System.err.println("unknown node, neither root, sub root, medium or episode node")
                return
            }
        }
        task.execute()
    }

    private class ClearMediaTask(
        private val mediumId: Int,
        private val tool: ContentTool,
        @field:SuppressLint("StaticFieldLeak") private val context: Context
    ) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg voids: Void?): String? {
            try {
                tool.removeMedia(mediumId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            checkLocal(context)
        }
    }

    private class ClearEpisodeTask(
        private val mediumId: Int, private val episodeId: Int, private val tool: ContentTool,
        @field:SuppressLint("StaticFieldLeak") private val context: Context?
    ) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg voids: Void?): String? {
            try {
                tool.removeMediaEpisodes(mediumId, setOf(episodeId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            checkLocal(context)
        }
    }

    private class ClearRootTask(
        private val toolSet: Set<ContentTool>,
        @field:SuppressLint("StaticFieldLeak") private val context: Context?
    ) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg voids: Void?): String? {
            for (tool in toolSet) {
                try {
                    tool.removeAll()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }

        override fun onPostExecute(s: String?) {
            checkLocal(context)
        }
    }

    private fun getContentTool(node: SpaceDataNode?): ContentTool {
        val application = requireActivity().application
        return when (node) {
            textNode -> getTextContentTool(application)
            videoNode -> getVideoContentTool(application)
            imageNode -> getImageContentTool(application)
            audioNode -> getAudioContentTool(application)
            else -> throw IllegalArgumentException("not a media sub root node")
        }
    }

    private fun deselectNode() {
        selectedNode = null
        viewSelected.isEnabled = false
        clearBtn.isEnabled = currentNode != null
        clearBtn.text = if (currentNode == null) "No Value" else "Clear " + currentNode!!.name
        selectedTitle.text = null
        selectedTitle.visibility = View.GONE
    }

    private open class SpaceDataNode(val name: String, private val size: Long = 0) {
        val children: MutableList<SpaceDataNode> = ArrayList()
        var parent: SpaceDataNode? = null

        fun hierarchyName(): String {
            return if (parent == null) name else parent!!.hierarchyName() + "/" + name
        }

        fun addChild(vararg child: SpaceDataNode) {
            for (node in child) {
                children.add(node)
                node.parent = this
            }
        }

        fun getSize(): Long {
            var total: Long = 0
            for (child in ArrayList(children)) {
                total += child.getSize()
            }
            return total + size
        }

        val sizeMB: Long
            get() = getSize() / (1024 * 1024)
    }

    private class EpisodeNode(name: String, size: Long, val id: Int) : SpaceDataNode(name, size)
    private class MediumNode(name: String, size: Long, val id: Int) : SpaceDataNode(name, size)

    @SuppressLint("StaticFieldLeak")
    private inner class GatherDataTask : AsyncTask<Void, SpaceDataNode, SpaceDataNode>() {
        override fun doInBackground(vararg voids: Void?): SpaceDataNode? {
            textNode.children.clear()
            audioNode.children.clear()
            videoNode.children.clear()
            imageNode.children.clear()
            val application = requireActivity().application
            runBlocking {
                coroutineScope {
                    launch { gatherData(false, getTextContentTool(application), textNode) }
                    launch { gatherData(true, getTextContentTool(application), textNode) }
                    launch { gatherData(false, getAudioContentTool(application), audioNode) }
                    launch { gatherData(true, getAudioContentTool(application), audioNode) }
                    launch { gatherData(false, getImageContentTool(application), imageNode) }
                    launch { gatherData(true, getImageContentTool(application), imageNode) }
                    launch { gatherData(false, getVideoContentTool(application), videoNode) }
                    launch { gatherData(true, getVideoContentTool(application), videoNode) }
                }
            }
            return null
        }

        private suspend fun gatherData(
            externalSpace: Boolean,
            contentTool: ContentTool,
            node: SpaceDataNode
        ) {
            if (!contentTool.isSupported) {
                return
            }
            val books: Map<Int, File> = contentTool.getItemContainers(externalSpace)

            for ((mediumId, value) in books) {
                val bookFile = books[mediumId] ?: continue
                val medium = instance.getSpaceMedium(mediumId)
                val episodePaths = contentTool.getEpisodePaths(value.absolutePath)
                val simpleEpisodes = instance.getSimpleEpisodes(episodePaths.keys)

                val mediumNode: SpaceDataNode = MediumNode(
                    medium.title,
                    bookFile.length(),
                    mediumId
                )
                for (episode in simpleEpisodes) {
                    mediumNode.addChild(EpisodeNode(
                        episode.formattedTitle,
                        contentTool.getEpisodeSize(value, episode.episodeId, episodePaths),
                        episode.episodeId
                    ))
                }
                if (mediumNode.children.isNotEmpty()) {
                    node.addChild(mediumNode)
                }
                publishProgress()
            }
        }

        override fun onProgressUpdate(vararg values: SpaceDataNode) {
            val size = root.getSize().toDouble()
            if (size == 0.0) {
                return
            }
            sliceValueSpaceDataNodeMap.clear()
            updateData(root)
        }

        override fun onPostExecute(spaceDataNode: SpaceDataNode?) {
            val size = root.getSize().toDouble()
            if (size == 0.0) {
                view.isRefreshing = false
                return
            }
            sliceValueSpaceDataNodeMap.clear()
            updateData(root)
            view.isRefreshing = false
        }
    }

    private fun updateData(node: SpaceDataNode?) {
        if (node == null) {
            throw NullPointerException("node is null")
        }
        val children: List<SpaceDataNode> = node.children
        val values: MutableList<SliceValue> = ArrayList()
        for (i in children.indices) {
            val child = children[i]
            val value = getValue(child, node.getSize().toDouble(), i, children.size)
            values.add(value)
        }
        val data = getData(values, node.getSize())
        chart.pieChartData = data
        title.text = node.name
        currentNode = node
    }

    private fun getData(sliceValues: List<SliceValue>, size: Long): PieChartData {
        val data = PieChartData()
        data.setHasLabels(true).valueLabelTextSize = 14
        val readableByteCount = humanReadableByteCount(size, true)
        data.setValues(sliceValues)
            .setHasCenterCircle(true)
            .setCenterText1("Usage: \n$readableByteCount").centerText1FontSize = 17
        return data
    }

    private fun getValue(
        child: SpaceDataNode,
        byteSize: Double,
        index: Int,
        size: Int
    ): SliceValue {
        val value = SliceValue()
        value.setLabel(child.name)
        value.value = (child.getSize() / byteSize).toFloat()
        if (index == 0 && size == 1) {
            value.color = Color.GRAY
        } else {
            var red = -510 / size * index + 255
            var blue = 510 / size * index - 255
            var green = (if (index < size / 2) blue else red) + 255

            red = if (red < 0) 0 else if (red > 255) 255 else red
            green = if (green < 0) 0 else if (green > 255) 255 else green
            blue = if (blue < 0) 0 else if (blue > 255) 255 else blue

            value.color = Color.rgb(red, green, blue)
        }
        sliceValueSpaceDataNodeMap[value] = child
        return value
    }

    init {
        this.root.addChild(textNode, audioNode, videoNode, imageNode)
    }
}