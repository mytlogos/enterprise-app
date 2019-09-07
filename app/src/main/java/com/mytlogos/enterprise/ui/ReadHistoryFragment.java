package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ReadEpisode;
import com.mytlogos.enterprise.model.Release;
import com.mytlogos.enterprise.viewmodel.ReadEpisodeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A fragment representing a list of Items.
 */
public class ReadHistoryFragment extends BaseListFragment<ReadEpisode, ReadEpisodeViewModel> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReadHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.setTitle("Read History");
        return view;
    }

    @Override
    Class<ReadEpisodeViewModel> getViewModelClass() {
        return ReadEpisodeViewModel.class;
    }

    @Override
    LiveData<PagedList<ReadEpisode>> createPagedListLiveData() {
        return getViewModel().getReadEpisodes();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<ReadEpisode> list) {
        List<IFlexible> items = new ArrayList<>();

        for (ReadEpisode episode : list) {
            if (episode == null) {
                break;
            }
            items.add(new ReadEpisodeItem(episode, this));
        }

        return items;
    }

    private static class SectionableReadEpisodeItem extends AbstractSectionableItem<ViewHolder, HeaderItem> {
        private final ReadEpisode displayReadEpisode;

        SectionableReadEpisodeItem(@NonNull ReadEpisode displayReadEpisode) {
            super(new HeaderItem(displayReadEpisode
                    .getReleases()
                    .stream()
                    .max(Comparator.comparingInt(value -> value.getTitle().length()))
                    .map(Release::getTitle)
                    .orElse("Not available"),
                    displayReadEpisode.getMediumId())
            );
            this.displayReadEpisode = displayReadEpisode;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableReadEpisodeItem)) return false;

            SectionableReadEpisodeItem other = (SectionableReadEpisodeItem) o;
            return this.displayReadEpisode.getEpisodeId() == other.displayReadEpisode.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.displayReadEpisode.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            holder.mItem = this.displayReadEpisode;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.novelView.setText(this.displayReadEpisode.getMediumTitle());
            holder.contentView.setText(this.displayReadEpisode
                    .getReleases()
                    .stream()
                    .max(Comparator.comparingInt(value -> value.getTitle().length()))
                    .map(Release::getTitle)
                    .orElse("Not available"));
        }
    }

    private static class ReadEpisodeItem extends AbstractFlexibleItem<ViewHolder> {
        private final ReadEpisode displayReadEpisode;

        ReadEpisodeItem(@NonNull ReadEpisode displayReadEpisode, Fragment fragment) {
            this.displayReadEpisode = displayReadEpisode;
            this.setDraggable(false);
            this.setSwipeable(false);
            this.setSelectable(false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectionableReadEpisodeItem)) return false;

            SectionableReadEpisodeItem other = (SectionableReadEpisodeItem) o;
            return this.displayReadEpisode.getEpisodeId() == other.displayReadEpisode.getEpisodeId();
        }

        @Override
        public int hashCode() {
            return this.displayReadEpisode.getEpisodeId();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.unreadchapter_item;
        }

        @Override
        public ViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolder(view, adapter);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {
            ReadEpisode episode = this.displayReadEpisode;
            holder.mItem = episode;
            // transform news id (int) to a string,
            // because it would expect a resource id if it is an int
            holder.novelView.setText(episode.getMediumTitle());

            String title = this.displayReadEpisode
                    .getReleases()
                    .stream()
                    .max(Comparator.comparingInt(value -> value.getTitle().length()))
                    .map(Release::getTitle)
                    .orElse("Not available");

            if (episode.getPartialIndex() > 0) {
                title = String.format("#%d.%d - %s", episode.getTotalIndex(), episode.getPartialIndex(), title);
            } else {
                title = String.format("#%d - %s", episode.getTotalIndex(), title);
            }
            holder.contentView.setText(title);
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

    private static class ViewHolder extends FlexibleViewHolder {
        final View mView;
        final TextView contentView;
        private final TextView metaView;
        private final TextView novelView;
        private final ImageButton optionsButtonView;
        private ReadEpisode mItem;

        ViewHolder(@NonNull View view, FlexibleAdapter adapter) {
            super(view, adapter);
            mView = view;
            metaView = view.findViewById(R.id.item_top_left);
            novelView = view.findViewById(R.id.item_top_right);
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
    public interface ReadChapterClickListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Object item);
    }
}
