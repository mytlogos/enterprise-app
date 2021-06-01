package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.target.Target
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.model.ChapterPage
import com.mytlogos.enterprise.model.SimpleEpisode
import com.mytlogos.enterprise.tools.FileTools.getImageContentTool
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import java.io.File
import java.security.MessageDigest
import java.util.*
import kotlin.math.abs

class ImageViewerFragment : ViewerFragment<ImageViewerFragment.ReadableEpisode?>() {
    private var adapter: FlexibleAdapter<IFlexible<ViewHolder>>? = null
    override val layoutRes: Int
        get() = R.layout.image_reader_fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        adapter = FlexibleAdapter(null)
        val manager = LinearLayoutManager(context)
        recyclerView.layoutManager = manager
        val decoration = DividerItemDecoration(requireContext(), manager.orientation)
        recyclerView.addItemDecoration(decoration)
        adapter!!.addListener(FlexibleAdapter.OnItemClickListener { _: View?, _: Int ->
            toggleReadingMode()
            false
        })
        recyclerView.adapter = adapter
        recyclerView.setOnScrollChangeListener { _: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            onScroll(scrollX,
                scrollY,
                oldScrollX,
                oldScrollY)
        }
        loadEpisodes()
        return view
    }

    override val scrolledViewId: Int
        get() = R.id.list
    override val currentProgress: Float
        get() = if (currentlyReading != null) currentlyReading!!.progress else 0.0f

    override fun saveProgress(progress: Float) {
        if (currentEpisode > 0) {
            instance.updateProgress(currentEpisode, progress)
        }
    }

    override fun onScroll(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScroll(scrollX, scrollY, oldScrollX, oldScrollY)
        if (currentlyReading == null) {
            return
        }
        val progress = calculateProgressByScroll(scrollY, scrollX)
        updateProgress(progress)
    }

    override fun calculateProgressByScroll(scrollY: Int, scrollX: Int): Float {
        val childCount = adapter!!.recyclerView.childCount
        if (childCount == 0) {
            return 0.0f
        }
        val visibleChildren: MutableList<View> = ArrayList()
        for (i in 0 until childCount) {
            val child = adapter!!.recyclerView.getChildAt(i) ?: continue
            val height = child.height
            if (height > 0) {
                visibleChildren.add(child)
            }
        }
        if (visibleChildren.isEmpty()) {
            return 0.0f
        }
        var lastVisibleRect: Rect? = null
        var lastVisibleView: View? = null
        for (i in visibleChildren.indices.reversed()) {
            val rect = Rect()
            val view = visibleChildren[i]
            view.getLocalVisibleRect(rect)
            if (rect.bottom > 0) {
                lastVisibleRect = rect
                lastVisibleView = view
                break
            }
        }
        if (lastVisibleRect == null || lastVisibleView!!.height <= 0) {
            return 0.0f
        }
        var lastVisiblePosition = -1
        for (holder in adapter!!.allBoundViewHolders) {
            if (holder.frontView === lastVisibleView) {
                lastVisiblePosition = holder.flexibleAdapterPosition
                break
            }
        }
        val itemCount = adapter!!.itemCount
        var viewedItems = 0
        if (lastVisiblePosition > 0) {
            viewedItems = lastVisiblePosition
        }
        val viewedProgress = viewedItems / itemCount.toFloat()
        // todo height may be negative (doc), but when?
        //  check how the case where it is negative should be treated
        val lastItemProgress = abs(lastVisibleRect.height()) / (lastVisibleView.height
            .toFloat() * itemCount)
        return viewedProgress + lastItemProgress
    }

    override fun updateContent() {
        if (currentlyReading != null) {
            currentEpisode = currentlyReading!!.episodeId
            val flexibles: MutableList<IFlexible<ViewHolder>> = ArrayList()
            for (i in 1 until currentlyReading!!.pageMap.size + 1) {
                val page = currentlyReading!!.pageMap[i]
                val item: ImageItem = if (page == null) {
                    ImageItem("", i)
                } else {
                    ImageItem(page.path, i)
                }
                flexibles.add(item)
            }
            adapter!!.updateDataSet(flexibles)
            if (currentlyReading!!.partialIndex > 0) {
                setTitle(String.format("Episode %s.%s",
                    currentlyReading!!.totalIndex,
                    currentlyReading!!.partialIndex))
            } else {
                setTitle(String.format("Episode %s", currentlyReading!!.totalIndex))
            }
        } else {
            // TODO: 06.08.2019 display an empty episode indicator?
            println("empty episode")
        }
        onLoadFinished()
    }

    private class ImageItem(imagePath: String, page: Int) : AbstractFlexibleItem<ViewHolder>() {
        private val imagePath: String?
        private val page: Int
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val imageItem = other as ImageItem
            return imagePath == imageItem.imagePath
        }

        override fun hashCode(): Int {
            return imagePath?.hashCode() ?: 0
        }

        override fun getLayoutRes(): Int {
            return R.layout.image_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>
        ): ViewHolder {
            return ViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            val empty = imagePath!!.isEmpty()
            holder.emptyText.visibility = if (empty) View.VISIBLE else View.GONE
            holder.imageView.visibility = if (empty) View.GONE else View.VISIBLE
            if (empty) {
                Glide.with(holder.itemView).clear(holder.imageView)
                //                holder.imageView.setImageURI(null);
                holder.emptyText.text = String.format("Page %s is missing", page)
            } else {
                holder.emptyText.text = null
                Glide
                    .with(holder.itemView)
                    .load(Uri.fromFile(File(imagePath)))
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
                    .into(holder.imageView)
                //                holder.imageView.setImageURI(Uri.fromFile(new File(this.imagePath)));
            }
        }

        init {
            this.imagePath = imagePath
            this.page = page
        }
    }

    private class CropBitMap : BitmapTransformation() {
        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap? {
            return null
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {}
    }

    private class ViewHolder(view: View, adapter: FlexibleAdapter<*>) :
        FlexibleViewHolder(view, adapter) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val emptyText: TextView = view.findViewById(R.id.empty_view)

        init {
            emptyText.setOnClickListener(this)
            imageView.setOnClickListener(this)
        }
    }

    class ReadableEpisode(
        episode: SimpleEpisode,
        @field:SuppressLint("UseSparseArrays") val pageMap: Map<Int, ChapterPage>
    ) : SimpleEpisode(episode.episodeId, episode.totalIndex, episode.partialIndex, episode.progress)

    private fun loadEpisodes() {
        @SuppressLint("StaticFieldLeak") val task: AsyncTask<Void, Void, Void> =
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg voids: Void?): Void? {
                    val bookTool = getImageContentTool(mainActivity.application)
                    val chapterPages: Map<Int, Set<ChapterPage>> =
                        bookTool.getEpisodePagePaths(currentBook)

                    if (chapterPages.isEmpty()) {
                        return null
                    }
                    val repository = getInstance(mainActivity.application)
                    val episodes = repository.getSimpleEpisodes(chapterPages.keys)
                    for (simpleEpisode in episodes) {
                        val episodeId = simpleEpisode.episodeId
                        val pages = chapterPages[episodeId]
                        if (pages == null || pages.isEmpty()) {
                            System.err.println("Could not find file for episodeId: $episodeId")
                            continue
                        }
                        @SuppressLint("UseSparseArrays") val pageMap: MutableMap<Int, ChapterPage> =
                            HashMap()
                        var max = 0
                        for (page in pages) {
                            pageMap[page.page] = page
                            if (max < page.page) {
                                max = page.page
                            }
                        }
                        for (i in 1 until max) {
                            pageMap.putIfAbsent(i, ChapterPage(episodeId, i, ""))
                        }
                        val readableEpisode = ReadableEpisode(simpleEpisode, pageMap)
                        if (episodeId == currentEpisode) {
                            currentlyReading = readableEpisode
                        }
                        readableEpisodes.add(readableEpisode)
                    }
                    return null
                }

                @SuppressLint("DefaultLocale")
                override fun onPostExecute(data: Void?) {
                    updateContent()
                }

                override fun onCancelled() {
                    showToastError("Could not load Book")
                }
            }
        task.execute()
    }

    private fun showToastError(s: String) {
        mainActivity.runOnUiThread { showToast(s) }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ImageViewerFragment.
         */
        fun newInstance(startEpisode: Int, zipFile: String?): ImageViewerFragment {
            val fragment = ImageViewerFragment()
            val args = Bundle()
            args.putInt(START_EPISODE, startEpisode)
            args.putString(MEDIUM, zipFile)
            fragment.arguments = args
            return fragment
        }
    }
}