package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.background.RepositoryImpl;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.model.Release;
import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.viewmodel.TocEpisodeViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class TocFragment extends BaseListFragment<TocEpisode, TocEpisodeViewModel> {

    private static final String MEDIUM_ID = "mediumId";
    private int mediumId;
    private boolean inActionMode;
    private ActionType actionType;
    private final ActionMode.Callback callback = new ActionMode.Callback() {
        private RelativeLayout relativeLayout;
        private BottomNavigationView navigationView;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("ToC Actions");
            Objects.requireNonNull(getMainActivity().getSupportActionBar()).hide();
            this.navigationView = new BottomNavigationView(requireContext());
            this.relativeLayout = (RelativeLayout) getView();

            if (this.relativeLayout == null) {
                System.err.println("root view is null in TocFragment for ActionMode");
                showToast("An Error occurred while entering ActionMode");
                return false;
            }
            getLayoutInflater().inflate(R.layout.bottom_navigation, this.relativeLayout);
            mode.getMenuInflater().inflate(R.menu.toc_action_menu, menu);
            this.navigationView = this.relativeLayout.findViewById(R.id.navigation);
            this.navigationView.inflateMenu(R.menu.toc_menu);
            this.navigationView.setItemIconTintList(new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            requireContext().getResources().getColor(R.color.colorPrimary, null),
                            Color.WHITE
                    }
            ));
            this.navigationView.setOnNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.mark_read:
                        actionType = ActionType.MARK_READ;
                        break;
                    case R.id.mark_unread:
                        actionType = ActionType.MARK_UNREAD;
                        break;
                    case R.id.download:
                        actionType = ActionType.DOWNLOAD;
                        break;
                    case R.id.delete_local:
                        actionType = ActionType.DELETE_SAVED_EPISODE;
                        break;
                    case R.id.refresh:
                        actionType = ActionType.RELOAD;
                        break;
                    default:
                        showToast("Unknown Selected Item");
                        return false;
                }
                displayActionModeActions();
                return true;
            });
            getFlexibleAdapter().setMode(SelectableAdapter.Mode.MULTI);
            inActionMode = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            FlexibleAdapter<IFlexible> adapter = getFlexibleAdapter();
            switch (item.getItemId()) {
                case R.id.clear:
                    adapter.clearSelection();
                    return true;
                case R.id.select_between:
                    List<Integer> positions = adapter.getSelectedPositions();
                    int highest = -1;
                    int lowest = Integer.MAX_VALUE;

                    if (positions.isEmpty()) {
                        return true;
                    }
                    for (Integer position : positions) {
                        highest = Math.max(position, highest);
                        lowest = Math.min(position, lowest);
                    }
                    if (highest < 0) {
                        System.err.println("A selected positions which are not positive");
                        return true;
                    }
                    List<IFlexible> items = adapter.getCurrentItems();
                    List<FlexibleViewHolder> holders = new ArrayList<>(adapter.getAllBoundViewHolders());

                    for (int i = lowest; i <= highest && i < items.size(); i++) {
                        if (!adapter.isSelected(i)) {
                            adapter.toggleSelection(i);
                            // hacky fix as adapter.toggleSelection alone
                            // does not show animation and state visibly (no change seen)
                            for (FlexibleViewHolder holder : holders) {
                                int position = holder.getFlexibleAdapterPosition();

                                if (position == i) {
                                    holder.toggleActivation();
                                    break;
                                }
                            }
                        }
                    }
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Objects.requireNonNull(getMainActivity().getSupportActionBar()).show();
            this.relativeLayout.removeView(this.navigationView);
            actionType = null;
            getFlexibleAdapter().setMode(SelectableAdapter.Mode.IDLE);
            getFlexibleAdapter().clearSelection();
            inActionMode = false;
        }
    };


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TocFragment() {
    }

    public static TocFragment newInstance(int mediumId) {
        TocFragment fragment = new TocFragment();
        Bundle args = new Bundle();
        args.putInt(MEDIUM_ID, mediumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.requireArguments();
        mediumId = bundle.getInt(MEDIUM_ID);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(getListView());
        // TODO: 22.07.2019 set the mediumTitle
        this.setTitle("Table of Contents");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.medium_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_setting) {
            int mediumId = Objects.requireNonNull(this.getArguments()).getInt(MEDIUM_ID);
            MediumSettingFragment fragment = MediumSettingFragment.newInstance(mediumId);
            this.getMainActivity().switchWindow(fragment, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    int getPosition(String text) {
        int position = super.getPosition(text);
        LiveData<PagedList<TocEpisode>> list = this.getLivePagedList();
        PagedList<TocEpisode> value = list.getValue();

        if (value == null || position < 0) {
            return position;
        }
        Sortings sort = getViewModel().getSort();
        if (sort == Sortings.INDEX_DESC) {
            position = value.size() - position;
        } else if (sort != Sortings.INDEX_ASC) {
            showToast("Unknown Sort");
            position = -1;
        }
        return position;
    }

    @Override
    Class<TocEpisodeViewModel> getViewModelClass() {
        return TocEpisodeViewModel.class;
    }

    @Override
    LiveData<PagedList<TocEpisode>> createPagedListLiveData() {
        return this.getViewModel().getToc(this.mediumId);
    }

    @Override
    IFlexible createFlexible(TocEpisode tocEpisode) {
        return new ListItem(tocEpisode);
    }

    @Nullable
    @Override
    Filterable createFilterable() {
        return new Filterable() {
            @Override
            public Property[] getSearchFilterProperties() {
                return new Property[]{
                        new PositionProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.read;
                            }

                            @Override
                            public Integer get() {
                                return (int) getViewModel().getReadFilter();
                            }

                            @Override
                            public int[] positionalMapping() {
                                return new int[]{1, 0, -1};
                            }

                            @Override
                            public void set(Integer value) {
                                getViewModel().setReadFilter(value.byteValue());
                            }
                        },
                        new PositionProperty() {
                            @Override
                            public int getViewId() {
                                return R.id.saved;
                            }

                            @Override
                            public int[] positionalMapping() {
                                return new int[]{1, 0, -1};
                            }

                            @Override
                            public Integer get() {
                                return (int) getViewModel().getSavedFilter();
                            }

                            @Override
                            public void set(Integer value) {
                                getViewModel().setSavedFilter(value.byteValue());
                            }
                        },
                };
            }

            @Override
            public int getFilterLayout() {
                return R.layout.toc_filter;
            }
        };
    }

    @Override
    LinkedHashMap<String, Sortings> getSortMap() {
        LinkedHashMap<String, Sortings> hashMap = new LinkedHashMap<>();
        hashMap.put("Index Asc", Sortings.INDEX_ASC);
        hashMap.put("Index Desc", Sortings.INDEX_DESC);
        return hashMap;
    }

    @Override
    public boolean onItemClick(View view, int position) {
        if (this.inActionMode) {
            if (position != RecyclerView.NO_POSITION) {
                getFlexibleAdapter().toggleSelection(position);
                return true;
            }
            return false;
        }
        IFlexible flexible = getFlexibleAdapter().getItem(position);

        if (!(flexible instanceof ListItem)) {
            return false;
        }
        ListItem item = (ListItem) flexible;
        if (item.item.isSaved()) {
            CompletableFuture<Integer> task = TaskManager.runCompletableTask(() -> RepositoryImpl.getInstance().getMediumType(this.mediumId));
            task.whenComplete((type, throwable) -> {
                if (type != null) {
                    openLocal(item.item.getEpisodeId(), this.mediumId, type);
                }
            });
        } else {
            List<String> urls = item.item.getReleases().stream().map(Release::getUrl).collect(Collectors.toList());
            openInBrowser(urls);
        }
        return false;
    }

    @Override
    public void onItemLongClick(int position) {
        if (!inActionMode) {
            this.getMainActivity().startActionMode(callback);
            getFlexibleAdapter().addSelection(position);
        } else {
            if (position != RecyclerView.NO_POSITION) {
                getFlexibleAdapter().toggleSelection(position);
            }
        }
    }

    private void displayActionModeActions() {
        if (actionType == null) {
            System.err.println("not action type selected");
            showToast("An Error occurred, cannot open Action Popup");
            return;
        }

        List<TocEpisode> items = new ArrayList<>();

        for (Integer position : getFlexibleAdapter().getSelectedPositions()) {
            IFlexible item = getFlexibleAdapter().getItem(position);

            if (item instanceof ListItem) {
                items.add(((ListItem) item).item);
            }
        }
        if (items.isEmpty()) {
            return;
        }
        TocEpisode firstItem = items.get(0);

        Map<String, ActionCount> actionCountMap = new HashMap<>();
        for (ActionCount value : ActionCount.values()) {
            actionCountMap.put(value.title, value);
        }

        List<String> menuItems = new ArrayList<>(actionCountMap.keySet());

        String title;
        switch (this.actionType) {
            case RELOAD:
                title = "Refresh";
                break;
            case DOWNLOAD:
                title = "Download";
                break;
            case MARK_READ:
                title = "Mark read";
                break;
            case MARK_UNREAD:
                title = "Mark unread";
                break;
            case DELETE_SAVED_EPISODE:
                title = "Delete Saved";
                break;
            default:
                showToast("Unknown ActionType");
                return;
        }
        title += ":";

        if (items.size() == 1) {
            switch (this.actionType) {
                case DOWNLOAD:
                    if (firstItem.isSaved()) {
                        menuItems.remove(0);
                    }
                    break;
                case MARK_READ:
                    if (firstItem.getProgress() == 1) {
                        menuItems.remove(0);
                    }
                    break;
                case MARK_UNREAD:
                    if (firstItem.getProgress() < 1) {
                        menuItems.remove(0);
                    }
                    break;
                case DELETE_SAVED_EPISODE:
                    if (!firstItem.isSaved()) {
                        menuItems.remove(0);
                    }
                    break;
            }
        }
        new AlertDialog
                .Builder(requireContext())
                .setTitle(title)
                .setItems(menuItems.toArray(new String[0]), (dialog, which) -> {
                    String menuTitle = menuItems.get(which);
                    ActionCount count = actionCountMap.get(menuTitle);

                    if (count == null) {
                        showToast("Unknown MenuItem");
                    } else {
                        this.handle(items, actionType, count);
                    }
                })
                .show();
    }

    public boolean handle(List<TocEpisode> items, ActionType type, ActionCount count) {
        new ChangeEpisodeReadStatus(type, items, mediumId, count, this.getItems(), this.getContext(), getViewModel()).execute();
        return true;
    }

    private static class ChangeEpisodeReadStatus extends AsyncTask<Void, Void, Void> {
        private final ActionType type;
        private final List<TocEpisode> selected;
        private final int mediumId;
        private final ActionCount count;
        private final List<TocEpisode> episodes;
        @SuppressLint("StaticFieldLeak")
        private final Context context;
        private final TocEpisodeViewModel viewModel;
        private String errorMessage;

        private ChangeEpisodeReadStatus(ActionType type, List<TocEpisode> selected, int mediumId, ActionCount count, List<TocEpisode> episodes, Context context, TocEpisodeViewModel viewModel) {
            this.type = type;
            this.selected = selected;
            this.mediumId = mediumId;
            this.count = count;
            this.episodes = episodes;
            this.context = context;
            this.viewModel = viewModel;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mediumId <= 0 || selected == null || selected.isEmpty() || episodes == null) {
                return null;
            }
            try {
                Set<Integer> episodeIds = new HashSet<>();
                List<Double> indices = new ArrayList<>();

                for (TocEpisode tocEpisode : this.selected) {
                    episodeIds.add(tocEpisode.getEpisodeId());
                    double combiIndex = Double.parseDouble(tocEpisode.getTotalIndex() + "." + tocEpisode.getPartialIndex());
                    indices.add(combiIndex);
                }
                switch (this.type) {
                    case MARK_READ:
                        this.viewModel.updateRead(episodeIds, indices, this.count, this.mediumId, true);
                        break;
                    case MARK_UNREAD:
                        this.viewModel.updateRead(episodeIds, indices, this.count, this.mediumId, false);
                        break;
                    case DELETE_SAVED_EPISODE:
                        this.viewModel.deleteLocalEpisode(episodeIds, indices, this.count, this.mediumId);
                        break;
                    case DOWNLOAD:
                        this.viewModel.download(episodeIds, indices, this.count, this.mediumId);
                        break;
                    case RELOAD:
                        this.viewModel.reload(episodeIds, indices, this.count, this.mediumId);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Could not execute Action";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (errorMessage != null) {
                Toast.makeText(this.context, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private static class ListItem extends AbstractFlexibleItem<ViewHolder> {
        private final TocEpisode item;

        ListItem(@NonNull TocEpisode item) {
            this.item = item;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(true);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListItem listItem = (ListItem) o;

            return this.item.equals(listItem.item);
        }

        @Override
        public int hashCode() {
            return this.item.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.episode_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }


        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            String index;
            if (this.item.getPartialIndex() > 0) {
                index = String.format("#%d.%d", this.item.getTotalIndex(), this.item.getPartialIndex());
            } else {
                index = String.format("#%d", this.item.getTotalIndex());
            }
            Comparator<Release> comparator = (o1, o2) -> o1.getReleaseDate().compareTo(o2.getReleaseDate());
            Optional<Release> earliestRelease = this.item.getReleases().stream().min(comparator);
            Release release = earliestRelease.orElse(null);
            String topRight;

            if (release == null) {
                topRight = "Not available";
            } else {
                topRight = release.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss");
            }
            String title = this.item
                    .getReleases()
                    .stream()
                    .map(Release::getTitle)
                    .max((o1, o2) -> o1.length() - o2.length())
                    .orElse("Not available");
            boolean hasOnline = this.item.getReleases().stream().anyMatch(any -> any.getUrl() != null && !any.getUrl().isEmpty());
            boolean isLocked = this.item.getReleases().stream().allMatch(Release::isLocked);

            holder.textTopLeft.setText(index);
            holder.textTopRight.setText(topRight);
            holder.textContentView.setText(title);

            holder.episodeLockedIcon.setVisibility(isLocked ? View.VISIBLE : View.GONE);
            holder.episodeReadIcon.setAlpha(this.item.getProgress() == 1 ? 1 : 0.25f);
            holder.openLocalIcon.setAlpha(this.item.isSaved() ? 1 : 0.25f);
            holder.openBrowserIcon.setAlpha(hasOnline ? 1 : 0.25f);

            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE,             // normal background
                    Color.GRAY, // pressed background
                    Color.BLACK);                 // ripple color
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
        }

    }

    private static class ViewHolder extends FlexibleViewHolder {
        final View mView;
        final TextView textContentView;
        private final TextView textTopLeft;
        private final TextView textTopRight;
        private final ImageView episodeLockedIcon;
        private final ImageView episodeReadIcon;
        private final ImageView openBrowserIcon;
        private final ImageView openLocalIcon;

        ViewHolder(@NonNull View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mView = view;
            this.textTopLeft = view.findViewById(R.id.item_top_left);
            this.textTopRight = view.findViewById(R.id.item_top_right);
            this.episodeLockedIcon = view.findViewById(R.id.episode_locked);
            this.episodeReadIcon = view.findViewById(R.id.episode_read);
            this.openBrowserIcon = view.findViewById(R.id.open_in_browser);
            this.openLocalIcon = view.findViewById(R.id.open_local);
            this.textContentView = view.findViewById(R.id.content);
        }


        @Override
        protected boolean shouldAddSelectionInActionMode() {
            return true;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + textContentView.getText() + "'";
        }
    }


    public enum ActionType {
        MARK_READ,
        MARK_UNREAD,
        MARK_PREVIOUSLY_READ,
        MARK_PREVIOUSLY_UNREAD,
        MARK_ALL_READ,
        MARK_ALL_UNREAD,
        DELETE_SAVED_EPISODE,
        DELETE_PREVIOUSLY_SAVED_EPISODE,
        DELETE_ALL_SAVED_EPISODE,
        DOWNLOAD,
        RELOAD,
        ;
    }
}
