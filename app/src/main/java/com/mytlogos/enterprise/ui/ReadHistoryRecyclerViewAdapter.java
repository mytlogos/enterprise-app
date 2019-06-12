package com.mytlogos.enterprise.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a Object and makes a call to the
 * specified {@link ReadHistoryFragment.ReadHistoryClickListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ReadHistoryRecyclerViewAdapter extends RecyclerView.Adapter<ReadHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<Object> mValues;
    private final ReadHistoryFragment.ReadHistoryClickListener mListener;

    public ReadHistoryRecyclerViewAdapter(List<Object> items, ReadHistoryFragment.ReadHistoryClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.readhistory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).toString());
        holder.mContentView.setText(mValues.get(position).toString());

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
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Object mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_meta);
            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
