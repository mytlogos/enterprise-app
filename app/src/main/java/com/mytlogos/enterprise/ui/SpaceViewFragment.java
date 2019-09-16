package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.model.SpaceMedium;
import com.mytlogos.enterprise.service.CheckSavedWorker;
import com.mytlogos.enterprise.tools.ContentTool;
import com.mytlogos.enterprise.tools.FileTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class SpaceViewFragment extends BaseFragment {

    private PieChartView chart;
    private SwipeRefreshLayout view;

    private Map<SliceValue, SpaceDataNode> sliceValueSpaceDataNodeMap = new HashMap<>();
    private final SpaceDataNode textNode = new SpaceDataNode("Books", 0);
    private final SpaceDataNode audioNode = new SpaceDataNode("Audio", 0);
    private final SpaceDataNode videoNode = new SpaceDataNode("Video", 0);
    private final SpaceDataNode imageNode = new SpaceDataNode("Images", 0);
    private SpaceDataNode root = new SpaceDataNode("Media");

    private SpaceDataNode currentNode;
    private SpaceDataNode selectedNode;
    private SpaceDataNode viewedNode;
    private TextView selectedTitle;
    private TextView title;
    private Button clearBtn;
    private Button viewSelected;

    public SpaceViewFragment() {
        this.root.addChild(textNode, audioNode, videoNode, imageNode);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (SwipeRefreshLayout) inflater.inflate(R.layout.space_view_fragment, container, false);
        chart = view.findViewById(R.id.chart);
        selectedTitle = view.findViewById(R.id.selected_title);
        title = view.findViewById(R.id.title);

        view.findViewById(R.id.container).setOnClickListener(v -> deselectNode());

        Button previousViewed = view.findViewById(R.id.previous_view);

        previousViewed.setOnClickListener(v -> {
            if (viewedNode == null || viewedNode.parent == null) {
                return;
            }
            sliceValueSpaceDataNodeMap.clear();
            viewedNode = viewedNode.parent;
            deselectNode();
            updateData(viewedNode);
            previousViewed.setVisibility(currentNode.parent == null ? View.GONE : View.VISIBLE);
            previousViewed.setText(currentNode.parent == null ? null : currentNode.parent.name);
        });

        viewSelected = view.findViewById(R.id.view_selected);
        viewSelected.setOnClickListener(v -> {
            if (selectedNode == null) {
                return;
            }
            viewedNode = selectedNode;
            deselectNode();
            updateData(viewedNode);
            previousViewed.setVisibility(View.VISIBLE);
            previousViewed.setText(currentNode.parent.name);
        });

        clearBtn = view.findViewById(R.id.clear_all_local_btn);
        clearBtn.setOnClickListener(v -> {
            SpaceDataNode node = selectedNode == null ? currentNode : selectedNode;
            if (node == null) {
                return;
            }
            new AlertDialog
                    .Builder(requireContext())
                    .setTitle("Do you really want to delete \n'" + node.hierarchyName() + "' ?")
                    .setPositiveButton("Yes", (dialog, which) -> clearNode(node))
                    .setNegativeButton("No", null)
                    .show();
        });

        view.setRefreshing(true);
        new GatherDataTask().execute();

        chart.setValueSelectionEnabled(true);
        chart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                SpaceDataNode node = sliceValueSpaceDataNodeMap.get(value);

                if (node == null) {
                    return;
                }
                selectedNode = node;
                viewSelected.setEnabled(!node.children.isEmpty());
                selectedTitle.setText(String.format("%s (%s MB)", node.name, node.getSizeMB()));
                selectedTitle.setVisibility(View.VISIBLE);

                clearBtn.setText(String.format("Clear %s", new String(value.getLabelAsChars())));
                clearBtn.setEnabled(true);
            }

            @Override
            public void onValueDeselected() {
                deselectNode();
            }
        });
        view.setOnRefreshListener(() -> new GatherDataTask().execute());
        return view;
    }

    private void clearNode(@NonNull SpaceDataNode node) {
        Application application = requireActivity().getApplication();

        AsyncTask<Void, Void, String> task;
        if (node.equals(root)) {
            task = new ClearRootTask(FileTools.getSupportedContentTools(application), this.getContext());

        } else if (node instanceof MediumNode) {
            ContentTool tool = getContentTool(node.parent);
            task = new ClearMediaTask(((MediumNode) node).id, tool, this.getContext());

        } else if (node instanceof EpisodeNode) {
            int id = ((EpisodeNode) node).id;
            SpaceDataNode episodeParent = node.parent;
            SpaceDataNode parent = episodeParent.parent;

            if (!(episodeParent instanceof MediumNode)) {
                throw new Error("episode node which is no child of medium node");
            }
            ContentTool tool = getContentTool(parent);
            task = new ClearEpisodeTask(((MediumNode) episodeParent).id, id, tool, this.getContext());

        } else if (node.parent == null) {
            ContentTool tool = getContentTool(node);
            task = new ClearRootTask(Collections.singleton(tool), this.getContext());

        } else {
            System.err.println("unknown node, neither root, sub root, medium or episode node");
            return;
        }
        task.execute();
    }

    private static class ClearMediaTask extends AsyncTask<Void, Void, String> {
        private final int mediumId;
        private final ContentTool tool;
        @SuppressLint("StaticFieldLeak")
        private final Context context;

        private ClearMediaTask(int mediumId, ContentTool tool, Context context) {
            this.mediumId = mediumId;
            this.tool = tool;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                tool.removeMedia(mediumId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            CheckSavedWorker.checkLocal(this.context);
        }
    }

    private static class ClearEpisodeTask extends AsyncTask<Void, Void, String> {
        private final int mediumId;
        private final int episodeId;
        private final ContentTool tool;
        @SuppressLint("StaticFieldLeak")
        private final Context context;

        private ClearEpisodeTask(int mediumId, int episodeId, ContentTool tool, Context context) {
            this.mediumId = mediumId;
            this.episodeId = episodeId;
            this.tool = tool;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                tool.removeMediaEpisodes(mediumId, Collections.singleton(episodeId));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            CheckSavedWorker.checkLocal(this.context);
        }
    }

    private static class ClearRootTask extends AsyncTask<Void, Void, String> {
        private final Set<ContentTool> toolSet;
        @SuppressLint("StaticFieldLeak")
        private final Context context;

        private ClearRootTask(Set<ContentTool> toolSet, Context context) {
            this.toolSet = toolSet;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            for (ContentTool tool : toolSet) {
                try {
                    tool.removeAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            CheckSavedWorker.checkLocal(this.context);
        }
    }

    private ContentTool getContentTool(SpaceDataNode node) {
        Application application = requireActivity().getApplication();

        if (node.equals(textNode)) {
            return FileTools.getTextContentTool(application);
        } else if (node.equals(videoNode)) {
            return FileTools.getVideoContentTool(application);
        } else if (node.equals(imageNode)) {
            return FileTools.getImageContentTool(application);
        } else if (node.equals(audioNode)) {
            return FileTools.getAudioContentTool(application);
        } else {
            throw new IllegalArgumentException("not a media sub root node");
        }
    }

    private void deselectNode() {
        selectedNode = null;
        viewSelected.setEnabled(false);
        clearBtn.setEnabled(currentNode != null);
        clearBtn.setText(currentNode == null ? "No Value" : "Clear " + currentNode.name);
        selectedTitle.setText(null);
        selectedTitle.setVisibility(View.GONE);
    }


    private static class SpaceDataNode {
        private final String name;
        private final long size;
        private final List<SpaceDataNode> children = new ArrayList<>();
        private SpaceDataNode parent;

        private SpaceDataNode(String name, long size) {
            this.name = name;
            this.size = size;
        }

        private SpaceDataNode(String name) {
            this(name, 0);
        }

        private String hierarchyName() {
            return this.parent == null ? this.name : this.parent.hierarchyName() + "/" + this.name;
        }

        private void addChild(SpaceDataNode... child) {
            for (SpaceDataNode node : child) {
                this.children.add(node);
                node.parent = this;
            }
        }

        private long getSize() {
            long total = 0;

            for (SpaceDataNode child : new ArrayList<>(children)) {
                total += child.getSize();
            }
            return total + this.size;
        }

        private long getSizeMB() {
            return this.getSize() / (1024 * 1024);
        }
    }

    private static class EpisodeNode extends SpaceDataNode {
        private final int id;

        private EpisodeNode(String name, long size, int id) {
            super(name, size);
            this.id = id;
        }
    }

    private static class MediumNode extends SpaceDataNode {
        private final int id;

        private MediumNode(String name, long size, int mediumId) {
            super(name, size);
            this.id = mediumId;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class GatherDataTask extends AsyncTask<Void, SpaceDataNode, SpaceDataNode> {

        @Override
        protected SpaceDataNode doInBackground(Void... voids) {
            textNode.children.clear();
            audioNode.children.clear();
            videoNode.children.clear();
            imageNode.children.clear();

            Application application = requireActivity().getApplication();

            gatherData(false, FileTools.getTextContentTool(application), textNode);
            gatherData(true, FileTools.getTextContentTool(application), textNode);

            gatherData(false, FileTools.getAudioContentTool(application), audioNode);
            gatherData(true, FileTools.getAudioContentTool(application), audioNode);

            gatherData(false, FileTools.getImageContentTool(application), imageNode);
            gatherData(true, FileTools.getImageContentTool(application), imageNode);

            gatherData(false, FileTools.getVideoContentTool(application), videoNode);
            gatherData(true, FileTools.getVideoContentTool(application), videoNode);
            return null;
        }

        private void gatherData(boolean externalSpace, ContentTool contentTool, SpaceDataNode node) {
            if (!contentTool.isSupported()) {
                return;
            }
            Map<Integer, File> books = contentTool.getItemContainers(externalSpace);

            for (Map.Entry<Integer, File> entry : books.entrySet()) {
                int mediumId = entry.getKey();
                File bookFile = books.get(mediumId);

                if (bookFile == null) {
                    continue;
                }
                SpaceMedium medium = RepositoryImpl.getInstance().getSpaceMedium(mediumId);
                Map<Integer, String> episodePaths = contentTool.getEpisodePaths(entry.getValue().getAbsolutePath());

                List<SimpleEpisode> simpleEpisodes = RepositoryImpl.getInstance().getSimpleEpisodes(episodePaths.keySet());

                SpaceDataNode mediumNode = new MediumNode(
                        medium.getTitle(),
                        bookFile.length(),
                        mediumId
                );
                for (SimpleEpisode episode : simpleEpisodes) {
                    mediumNode.addChild(new EpisodeNode(
                            episode.getFormattedTitle(),
                            contentTool.getEpisodeSize(entry.getValue(), episode.getEpisodeId(), episodePaths),
                            episode.getEpisodeId()
                    ));
                }
                if (!mediumNode.children.isEmpty()) {
                    node.addChild(mediumNode);
                }
                this.publishProgress();
            }
        }

        @Override
        protected void onProgressUpdate(SpaceDataNode... values) {
            double size = root.getSize();

            if (size == 0) {
                return;
            }
            sliceValueSpaceDataNodeMap.clear();
            updateData(root);
        }

        @Override
        protected void onPostExecute(SpaceDataNode spaceDataNode) {

            double size = root.getSize();

            if (size == 0) {
                view.setRefreshing(false);
                return;
            }
            sliceValueSpaceDataNodeMap.clear();
            updateData(root);
            view.setRefreshing(false);
        }
    }

    private void updateData(SpaceDataNode node) {
        if (node == null) {
            throw new NullPointerException("node is nullt");
        }
        List<SpaceDataNode> children = node.children;
        List<SliceValue> values = new ArrayList<>();

        for (int i = 0; i < children.size(); i++) {
            SpaceDataNode child = children.get(i);
            SliceValue value = getValue(child, node.getSize(), i, children.size());
            values.add(value);
        }
        PieChartData data = getData(values, node.getSize());
        chart.setPieChartData(data);
        title.setText(node.name);
        currentNode = node;
    }

    private PieChartData getData(List<SliceValue> sliceValues, long size) {
        PieChartData data = new PieChartData();
        data.setHasLabels(true).setValueLabelTextSize(14);
        String readableByteCount = FileTools.humanReadableByteCount(size, true);

        data.setValues(sliceValues)
                .setHasCenterCircle(true)
                .setCenterText1("Usage: \n\r" + readableByteCount)
                .setCenterText1FontSize(17);

        return data;
    }

    private SliceValue getValue(SpaceDataNode child, double byteSize, int index, int size) {
        SliceValue value = new SliceValue();
        value.setLabel(child.name);
        value.setValue((float) (child.getSize() / byteSize));

        if (index == 0 && size == 1) {
            value.setColor(Color.GRAY);
        } else {
            int red = (-510 / size) * index + 255;
            int blue = (510 / size) * index - 255;
            int green = (index < (size / 2) ? blue : red) + 255;

            red = red < 0 ? 0 : red > 255 ? 255 : red;
            green = green < 0 ? 0 : green > 255 ? 255 : green;
            blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
            value.setColor(Color.rgb(red, green, blue));
        }

        sliceValueSpaceDataNodeMap.put(value, child);
        return value;
    }
}
