package com.mytlogos.enterprise.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.ui.NewsFragment.NewsClickListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link News} and makes a call to the
 * specified {@link NewsClickListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private final NewsClickListener mListener;
    private List<News> news;

    NewsRecyclerViewAdapter(List<News> items, NewsClickListener listener) {
        news = items;
        mListener = listener;
    }

    public void setValue(List<News> news) {
        this.news = news;
        // todo make more specific notification?
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = news.get(position);
        holder.mIdView.setText(news.get(position).getNewsId());
        holder.mContentView.setText(news.get(position).getTitle());
        holder.mDateView.setText(news.get(position).getTimeStampString());

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final TextView mContentView;
        final TextView mDateView;
        News mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            mDateView = view.findViewById(R.id.time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
