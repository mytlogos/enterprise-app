package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ExternalMediaList;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.ui.dummy.DummyContent.DummyItem;
import com.mytlogos.enterprise.viewmodel.ListsViewModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ListsFragment extends BaseFragment {

    private ListsViewModel viewModel;
    private LiveData<List<MediaList>> liveLists;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListsFragment() {
    }

    private static String getDenominator(MediaList list) {
        String denominator;

        if (list instanceof ExternalMediaList) {
            String host = URI.create(((ExternalMediaList) list).getUrl()).getHost();
            Matcher matcher = Pattern.compile("(www\\.)?(.+)").matcher(host);

            if (matcher.matches()) {
                denominator = matcher
                        .group(2);
            } else {
                denominator = host;
            }
        } else {
            denominator = "Intern";
        }
        return denominator;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            this.getMainActivity().switchWindow(new AddList(), true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
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
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiper);

        this.viewModel = ViewModelProviders.of(this).get(ListsViewModel.class);

        this.liveLists = this.viewModel.getLists();
        this.liveLists.observe(this, lists -> {

            if (checkEmptyList(lists, view, swipeRefreshLayout)) {
                return;
            }

            List<IFlexible> items = new ArrayList<>();

            for (MediaList mediaList : lists) {
                items.add(new ListItem(mediaList, this));
            }

            flexibleAdapter.updateDataSet(items);
        });
        this.setTitle("Lists");
        return view;
    }

    private static class SectionableListItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final MediaList list;
        private final BaseFragment fragment;

        SectionableListItem(@NonNull MediaList list, BaseFragment fragment) {
            super(new HeaderItem(getDenominator(list)));
            this.list = list;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableListItem)) return false;

            SectionableListItem other = (SectionableListItem) o;
            return this.list.getListId() == other.list.getListId();
        }

        @Override
        public int hashCode() {
            return this.list.getListId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.list_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.list;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(String.format("%d Items", this.list.getSize()));
            holder.denominatorView.setText(getDenominator(list));
            holder.contentView.setText(this.list.getName());

            holder.mView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt(ListMediumFragment.ID, this.list.getListId());
                bundle.putString(ListMediumFragment.TITLE, this.list.getName());
                bundle.putBoolean(ListMediumFragment.EXTERNAL, this.list instanceof ExternalMediaList);
                fragment.getMainActivity().switchWindow(new ListMediumFragment(), bundle, true);
            });
        }

    }

    private static class ListItem extends AbstractFlexibleItem<ViewHolder> {
        private final MediaList list;
        private final BaseFragment fragment;

        ListItem(@NonNull MediaList list, BaseFragment fragment) {
            this.list = list;
            this.fragment = fragment;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableListItem)) return false;

            SectionableListItem other = (SectionableListItem) o;
            return this.list.getListId() == other.list.getListId();
        }

        @Override
        public int hashCode() {
            return this.list.getListId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.news_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.list;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.metaView.setText(String.format("%d Items", this.list.getSize()));
            holder.denominatorView.setText(getDenominator(list));
            holder.contentView.setText(this.list.getName());

            holder.mView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt(ListMediumFragment.ID, this.list.getListId());
                bundle.putString(ListMediumFragment.TITLE, this.list.getName());
                bundle.putBoolean(ListMediumFragment.EXTERNAL, this.list instanceof ExternalMediaList);
                fragment.getMainActivity().switchWindow(new ListMediumFragment(), bundle, true);
            });

            /*
            holder.mView.setOnClickListener(v -> {
                String url = this.unreadEpisode.getUrl();
                if (url == null || url.isEmpty()) {
                    Toast
                            .makeText(this.fragment.getContext(), "No Link available", Toast.LENGTH_SHORT)
                            .show();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                PackageManager manager = Objects.requireNonNull(this.fragment.getActivity()).getPackageManager();

                if (intent.resolveActivity(manager) != null) {
                    this.fragment.startActivity(intent);
                } else {
                    Toast
                            .makeText(this.fragment.getContext(), "No Browser available", Toast.LENGTH_SHORT)
                            .show();
                }
            });
*/
        }

    }

    private static class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {

        private final String title;

        private HeaderItem(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HeaderItem that = (HeaderItem) o;

            return title.equals(that.title);
        }

        @Override
        public int hashCode() {
            return title.hashCode();
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
        private final TextView denominatorView;
        MediaList mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            metaView = view.findViewById(R.id.item_top_left);
            denominatorView = view.findViewById(R.id.item_top_right);
            contentView = view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
