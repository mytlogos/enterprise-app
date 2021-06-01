package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.Target;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.ChapterPage;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.ImageContentTool;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ImageViewerFragment extends ViewerFragment<ImageViewerFragment.ReadableEpisode> {
    private FlexibleAdapter<IFlexible<ViewHolder>> adapter;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ImageViewerFragment.
     */
    public static ImageViewerFragment newInstance(int startEpisode, String zipFile) {
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putInt(START_EPISODE, startEpisode);
        args.putString(MEDIUM, zipFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    int getLayoutRes() {
        return R.layout.image_reader_fragment;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.list);

        this.adapter = new FlexibleAdapter<>(null);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), manager.getOrientation());
        recyclerView.addItemDecoration(decoration);
        this.adapter.addListener((FlexibleAdapter.OnItemClickListener) (view1, position) -> {
            this.toggleReadingMode();
            return false;
        });

        recyclerView.setAdapter(this.adapter);
        recyclerView.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        this.onScroll(scrollX, scrollY, oldScrollX, oldScrollY)
        );
        this.loadEpisodes();
        return view;
    }

    @Override
    int getScrolledViewId() {
        return R.id.list;
    }

    @Override
    float getCurrentProgress() {
        return this.currentlyReading != null ? this.currentlyReading.getProgress() : 0;
    }

    @Override
    void saveProgress(float progress) {
        if (this.currentEpisode > 0) {
            RepositoryImpl.getInstance().updateProgress(this.currentEpisode, progress);
        }
    }

    @Override
    void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScroll(scrollX, scrollY, oldScrollX, oldScrollY);
        if (this.currentlyReading == null) {
            return;
        }
        float progress = this.calculateProgressByScroll(scrollY, scrollX);
        this.updateProgress(progress);
    }

    @Override
    float calculateProgressByScroll(int scrollY, int scrollX) {
        int childCount = this.adapter.getRecyclerView().getChildCount();

        if (childCount == 0) {
            return 0;
        }
        List<View> visibleChildren = new ArrayList<>();

        for (int i = 0; i < childCount; i++) {
            View child = this.adapter.getRecyclerView().getChildAt(i);
            if (child == null) {
                continue;
            }
            int height = child.getHeight();

            if (height > 0) {
                visibleChildren.add(child);
            }
        }
        if (visibleChildren.isEmpty()) {
            return 0;
        }

        Rect lastVisibleRect = null;
        View lastVisibleView = null;

        for (int i = visibleChildren.size() - 1; i >= 0; i--) {
            Rect rect = new Rect();

            View view = visibleChildren.get(i);
            view.getLocalVisibleRect(rect);

            if (rect.bottom > 0) {
                lastVisibleRect = rect;
                lastVisibleView = view;
                break;
            }
        }

        if (lastVisibleRect == null || lastVisibleView.getHeight() <= 0) {
            return 0;
        }
        int lastVisiblePosition = -1;
        for (FlexibleViewHolder holder : this.adapter.getAllBoundViewHolders()) {
            if (holder.getFrontView() == lastVisibleView) {
                lastVisiblePosition = holder.getFlexibleAdapterPosition();
                break;
            }
        }
        int itemCount = this.adapter.getItemCount();
        int viewedItems = 0;
        if (lastVisiblePosition > 0) {
            viewedItems = lastVisiblePosition;
        }
        float viewedProgress = viewedItems / ((float) itemCount);
        // todo height may be negative (doc), but when?
        //  check how the case where it is negative should be treated
        float lastItemProgress = Math.abs(lastVisibleRect.height()) / ((float) lastVisibleView.getHeight() * itemCount);
        return viewedProgress + lastItemProgress;
    }

    @Override
    void updateContent() {
        if (this.currentlyReading != null) {
            this.currentEpisode = this.currentlyReading.getEpisodeId();

            List<IFlexible<ViewHolder>> flexibles = new ArrayList<>();

            for (int i = 1; i < this.currentlyReading.pageMap.size() + 1; i++) {
                ChapterPage page = this.currentlyReading.pageMap.get(i);

                ImageItem item;
                if (page == null) {
                    item = new ImageItem("", i);
                } else {
                    item = new ImageItem(page.getPath(), i);
                }
                flexibles.add(item);
            }
            this.adapter.updateDataSet(flexibles);

            if (this.currentlyReading.getPartialIndex() > 0) {
                setTitle(String.format("Episode %s.%s", this.currentlyReading.getTotalIndex(), this.currentlyReading.getPartialIndex()));
            } else {
                setTitle(String.format("Episode %s", this.currentlyReading.getTotalIndex()));
            }
        } else {
            // TODO: 06.08.2019 display an empty episode indicator?
            System.out.println("empty episode");
        }
        onLoadFinished();
    }


    private static class ImageItem extends AbstractFlexibleItem<ViewHolder> {
        private final String imagePath;
        private final int page;

        private ImageItem(String imagePath, int page) {
            this.imagePath = imagePath;
            this.page = page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImageItem imageItem = (ImageItem) o;

            return Objects.equals(imagePath, imageItem.imagePath);
        }

        @Override
        public int hashCode() {
            return imagePath != null ? imagePath.hashCode() : 0;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.image_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            boolean empty = this.imagePath.isEmpty();

            holder.emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
            holder.imageView.setVisibility(empty ? View.GONE : View.VISIBLE);

            if (empty) {
                Glide.with(holder.itemView).clear(holder.imageView);
//                holder.imageView.setImageURI(null);
                holder.emptyText.setText(String.format("Page %s is missing", this.page));
            } else {
                holder.emptyText.setText(null);
                Glide
                        .with(holder.itemView)
                        .load(Uri.fromFile(new File(this.imagePath)))
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL)
                        .into(holder.imageView);
//                holder.imageView.setImageURI(Uri.fromFile(new File(this.imagePath)));
            }
        }
    }

    private static class CropBitMap extends BitmapTransformation {
        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            return null;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

        }
    }


    private static class ViewHolder extends FlexibleViewHolder {
        private final ImageView imageView;
        private final TextView emptyText;

        private ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.emptyText = view.findViewById(R.id.empty_view);
            this.imageView = view.findViewById(R.id.image);
            this.emptyText.setOnClickListener(this);
            this.imageView.setOnClickListener(this);
        }
    }


    static class ReadableEpisode extends SimpleEpisode {
        @SuppressLint("UseSparseArrays")
        private final Map<Integer, ChapterPage> pageMap;

        ReadableEpisode(SimpleEpisode episode, Map<Integer, ChapterPage> pageMap) {
            super(episode.getEpisodeId(), episode.getTotalIndex(), episode.getPartialIndex(), episode.getProgress());
            this.pageMap = pageMap;
        }
    }

    private void loadEpisodes() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ImageContentTool bookTool = FileTools.INSTANCE.getImageContentTool(getMainActivity().getApplication());
                Map<Integer, Set<ChapterPage>> chapterPages = bookTool.getEpisodePagePaths(currentBook);

                if (chapterPages == null || chapterPages.isEmpty()) {
                    return null;
                }
                Repository repository = RepositoryImpl.Companion.getInstance(getMainActivity().getApplication());

                List<SimpleEpisode> episodes = repository.getSimpleEpisodes(chapterPages.keySet());

                for (SimpleEpisode simpleEpisode : episodes) {
                    int episodeId = simpleEpisode.getEpisodeId();
                    Set<ChapterPage> pages = chapterPages.get(episodeId);

                    if (pages == null || pages.isEmpty()) {
                        System.err.println("Could not find file for episodeId: " + episodeId);
                        continue;
                    }

                    @SuppressLint("UseSparseArrays")
                    Map<Integer, ChapterPage> pageMap = new HashMap<>();
                    int max = 0;

                    for (ChapterPage page : pages) {
                        pageMap.put(page.getPage(), page);

                        if (max < page.getPage()) {
                            max = page.getPage();
                        }
                    }
                    for (int i = 1; i < max; i++) {
                        pageMap.putIfAbsent(i, new ChapterPage(episodeId, i, ""));
                    }
                    ReadableEpisode readableEpisode = new ReadableEpisode(simpleEpisode, pageMap);

                    if (episodeId == currentEpisode) {
                        currentlyReading = readableEpisode;
                    }

                    readableEpisodes.add(readableEpisode);
                }
                return null;
            }

            @SuppressLint("DefaultLocale")
            @Override
            protected void onPostExecute(Void data) {
                updateContent();
            }

            @Override
            protected void onCancelled() {
                showToastError("Could not load Book");
            }
        };
        task.execute();
    }

    private void showToastError(String s) {
        getMainActivity().runOnUiThread(() -> showToast(s));
    }
}
