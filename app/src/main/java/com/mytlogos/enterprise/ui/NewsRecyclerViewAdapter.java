package com.mytlogos.enterprise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.ui.NewsFragment.NewsClickListener;

import java.util.List;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link News} and makes a call to the
 * specified {@link NewsClickListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private final NewsClickListener mListener;
    private final Context context;
    private List<News> news;

    NewsRecyclerViewAdapter(List<News> items, NewsClickListener listener, Context context) {
        this.news = items;
        this.mListener = listener;
        this.context = context;
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
                .inflate(R.layout.fragment_news_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        News news = this.news.get(position);
        holder.mItem = news;
        // transform news id (int) to a string,
        // because it would expect a resource id if it is an int
        holder.indexView.setText(String.format("%d.", position + 1));
        holder.metaView.setText(news.getTimeStampString());

        int drawableId = news.isRead() ? R.drawable.ic_read : R.drawable.ic_not_read;
        Drawable drawable = Objects.requireNonNull(ContextCompat.getDrawable(this.context, drawableId));
        int bound = (int) (drawable.getIntrinsicWidth() * 0.5);
        drawable.setBounds(0, 0, bound, bound);
        holder.metaView.setCompoundDrawables(null, null, drawable, null);
        holder.metaView.setCompoundDrawablePadding(5);

        holder.contentView.setText(news.getTitle());


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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView indexView;
        final TextView contentView;
        private final TextView metaView;
        News mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            indexView = view.findViewById(R.id.item_index);
            metaView = view.findViewById(R.id.item_meta);
            contentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
