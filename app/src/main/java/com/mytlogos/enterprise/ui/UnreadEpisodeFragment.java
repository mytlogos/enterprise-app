package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.UnreadEpisode;
import com.mytlogos.enterprise.viewmodel.UnreadEpisodeViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 */
public class UnreadEpisodeFragment extends BaseFragment {

    private UnreadEpisodeViewModel viewModel;
    private LiveData<List<UnreadEpisode>> liveUnreadEpisodes;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UnreadEpisodeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.list);

        // Set the adapter
        Context context = view.getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        FlexibleAdapter<IFlexible> flexibleAdapter = new FlexibleAdapter<>(null)
                .setStickyHeaders(true)
                .setDisplayHeadersAtStartUp(true);

        recyclerView.setAdapter(flexibleAdapter);

        this.viewModel = ViewModelProviders.of(this).get(UnreadEpisodeViewModel.class);

        this.liveUnreadEpisodes = this.viewModel.getUnreadEpisodes();
        this.liveUnreadEpisodes.observe(this, unreadEpisodes -> {
            if (unreadEpisodes != null) {
                List<IFlexible> items = new ArrayList<>();

                for (UnreadEpisode episode : unreadEpisodes) {
                    items.add(new UnreadEpisodeItem(episode, this));
                }

                flexibleAdapter.updateDataSet(items);
            }
        });
        this.setTitle("Unread Chapters");
        return view;
    }

    private static class SectionableUnreadEpisodeItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final UnreadEpisode unreadEpisode;
        private final Fragment fragment;

        SectionableUnreadEpisodeItem(@NonNull UnreadEpisode unreadEpisode, Fragment fragment) {
            super(new HeaderItem(unreadEpisode.getTitle(), unreadEpisode.getMediumId()));
            this.unreadEpisode = unreadEpisode;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableUnreadEpisodeItem)) return false;

            SectionableUnreadEpisodeItem other = (SectionableUnreadEpisodeItem) o;
            return this.unreadEpisode.getEpisodeId() == other.unreadEpisode.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.unreadEpisode.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.unreadEpisode;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(this.unreadEpisode.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.novelView.setText(this.unreadEpisode.getMediumTitle());
            holder.contentView.setText(this.unreadEpisode.getTitle());
            holder.optionsButtonView.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(this.fragment.getContext(), holder.optionsButtonView);

                if (holder.mItem.isSaved()) {
                    popupMenu
                            .getMenu()
                            .add("Open Local")
                            .setOnMenuItemClickListener(item -> {
                                System.out.println("i am opening locally");
                                return true;
                            });
                }
                popupMenu
                        .getMenu()
                        .add("Open in Browser")
                        .setOnMenuItemClickListener(item -> {
                            String url = this.unreadEpisode.getUrl();

                            if (url == null || url.isEmpty()) {
                                Toast
                                        .makeText(
                                                this.fragment.getContext(),
                                                "No Link available",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show();
                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                            PackageManager manager = Objects.requireNonNull(this.fragment.getActivity()).getPackageManager();

                            if (intent.resolveActivity(manager) != null) {
                                this.fragment.startActivity(intent);
                            } else {
                                Toast
                                        .makeText(
                                                this.fragment.getContext(),
                                                "No Browser available",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show();
                            }
                            System.out.println("i am opening in browser");
                            return true;
                        });
                popupMenu.show();
            });
        }
    }

    private static class UnreadEpisodeItem extends AbstractFlexibleItem<ViewHolder> {
        private final UnreadEpisode unreadEpisode;
        private final Fragment fragment;

        UnreadEpisodeItem(@NonNull UnreadEpisode unreadEpisode, Fragment fragment) {
            this.unreadEpisode = unreadEpisode;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableUnreadEpisodeItem)) return false;

            SectionableUnreadEpisodeItem other = (SectionableUnreadEpisodeItem) o;
            return this.unreadEpisode.getEpisodeId() == other.unreadEpisode.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.unreadEpisode.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.unreadEpisode;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(this.unreadEpisode.getReleaseDate().toString("dd.MM.yyyy HH:mm:ss"));
            holder.novelView.setText(this.unreadEpisode.getMediumTitle());
            holder.contentView.setText(this.unreadEpisode.getTitle());

            holder.optionsButtonView.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(this.fragment.getContext(), holder.optionsButtonView);

                if (holder.mItem.isSaved()) {
                    popupMenu
                            .getMenu()
                            .add("Open Local")
                            .setOnMenuItemClickListener(item -> {
                                System.out.println("i am opening locally");
                                return true;
                            });
                }
                popupMenu
                        .getMenu()
                        .add("Open in Browser")
                        .setOnMenuItemClickListener(item -> {
                            String url = this.unreadEpisode.getUrl();

                            if (url == null || url.isEmpty()) {
                                Toast
                                        .makeText(
                                                this.fragment.getContext(),
                                                "No Link available",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show();
                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                            PackageManager manager = Objects.requireNonNull(this.fragment.getActivity()).getPackageManager();

                            if (intent.resolveActivity(manager) != null) {
                                this.fragment.startActivity(intent);
                            } else {
                                Toast
                                        .makeText(
                                                this.fragment.getContext(),
                                                "No Browser available",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show();
                            }
                            System.out.println("i am opening in browser");
                            return true;
                        });
                popupMenu.show();
            });
        }
    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {

        private final String title;
        private final int mediumId;

        private HeaderItem(String title, int mediumId) {
            this.title = title;
            this.mediumId = mediumId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderItem)) return false;

            HeaderItem other = (HeaderItem) o;
            return this.mediumId == other.mediumId;
        }

        @Override
        public int hashCode() {
            return mediumId;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.flexible_header;
        }

        @Override
        public HeaderViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new HeaderViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, HeaderViewHolder holder, int position, List<Object> payloads) {
            holder.textView.setText(this.title);
        }
    }

    private static class HeaderViewHolder extends FlexibleViewHolder {
        private TextView textView;

        HeaderViewHolder(@NonNull View itemView, FlexibleAdapter<IFlexible> adapter) {
            super(itemView, adapter, true);
            textView = itemView.findViewById(R.id.text);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView novelView;
        private final ImageButton optionsButtonView;
        UnreadEpisode mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            metaView = view.findViewById(R.id.item_meta);
            novelView = view.findViewById(R.id.item_novel);
            contentView = view.findViewById(R.id.content);
            optionsButtonView = view.findViewById(R.id.item_options_button);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface UnreadChapterClickListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Object item);
    }
}
