package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.ChapterPage;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.ImageContentTool;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

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

public class ImageViewerFragment extends BaseFragment {
    private static final String MEDIUM = "MEDIUM_FILE";
    private static final String START_EPISODE = "START_EPISODE";

    private int currentEpisode;
    private String currentBook;
    private List<ReadableEpisode> readableEpisodes = new ArrayList<>();
    private ReadableEpisode currentlyReading;
    private SwipyRefreshLayout swipeLayout;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.currentEpisode = getArguments().getInt(START_EPISODE);
            this.currentBook = getArguments().getString(MEDIUM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        swipeLayout = (SwipyRefreshLayout) inflater.inflate(R.layout.image_reader_fragment, container, false);
        RecyclerView recyclerView = swipeLayout.findViewById(R.id.list);

        this.adapter = new FlexibleAdapter<>(null);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), manager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        recyclerView.setAdapter(adapter);

        swipeLayout.setOnRefreshListener(direction -> {
            if (this.currentlyReading == null) {
                if (!this.readableEpisodes.isEmpty()) {
                    this.currentlyReading = this.readableEpisodes.get(0);
                }
            } else {
                int index = this.readableEpisodes.indexOf(currentlyReading);
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    index--;
                } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    index++;
                } else {
                    System.out.println("Unknown swipe direction in TextViewerFragment, neither top or bottom");
                    this.swipeLayout.setRefreshing(false);
                    return;
                }
                if (index >= this.readableEpisodes.size()) {
                    // TODO: 26.07.2019 check with if there are more episodes and save them
                    showToast("You are already reading the last saved item", Toast.LENGTH_LONG);
                    this.swipeLayout.setRefreshing(false);
                    return;
                } else if (index < 0) {
                    // TODO: 26.07.2019 check with if there are more episodes and save them
                    showToast("You are already reading the first saved item", Toast.LENGTH_LONG);
                    this.swipeLayout.setRefreshing(false);
                    return;
                }
                this.currentlyReading = this.readableEpisodes.get(index);
                this.updateImageList();
                this.swipeLayout.setRefreshing(false);
            }
        });
        this.loadEpisodes();
        return swipeLayout;
    }

    private void updateImageList() {
        if (this.currentlyReading != null) {
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

            if (currentlyReading.getPartialIndex() > 0) {
                setTitle(String.format("Episode %s.%s", currentlyReading.getTotalIndex(), currentlyReading.getPartialIndex()));
            } else {
                setTitle(String.format("Episode %s", currentlyReading.getTotalIndex()));
            }
        } else {
            // TODO: 06.08.2019 display an empty episode indicator?
            System.out.println("empty episode");
        }
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
                holder.imageView.setImageURI(null);
                holder.emptyText.setText(String.format("Page %s is missing", this.page));
            } else {
                holder.emptyText.setText(null);
                holder.imageView.setImageURI(Uri.parse(this.imagePath));
            }
        }
    }

    private static class ViewHolder extends FlexibleViewHolder {
        private final ImageView imageView;
        private final TextView emptyText;

        private ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            emptyText = view.findViewById(R.id.empty_view);
            imageView = view.findViewById(R.id.image);
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
                ImageContentTool bookTool = FileTools.getImageContentTool(getMainActivity().getApplication());
                Map<Integer, Set<ChapterPage>> chapterPages = bookTool.getEpisodePagePaths(currentBook);

                if (chapterPages == null || chapterPages.isEmpty()) {
                    return null;
                }
                Repository repository = RepositoryImpl.getInstance(getMainActivity().getApplication());

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
                swipeLayout.setRefreshing(false);
                updateImageList();
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
