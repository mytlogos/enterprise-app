package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TextOnlyListAdapter<E> extends ArrayAdapter<E> {
    private final Function<E, String> extractor;

    TextOnlyListAdapter(@NonNull Fragment fragment, LiveData<List<E>> liveData, Function<E, String> extractor) {
        super(fragment.requireContext(), R.layout.text_only_item);
        this.extractor = extractor;
        liveData.observe(fragment, mediaLists -> {
            if (mediaLists == null) {
                mediaLists = new ArrayList<>();
            }
            this.clear();
            this.addAll(mediaLists);
        });
    }

    TextOnlyListAdapter(@NonNull Context context, Function<E, String> extractor) {
        super(context, R.layout.text_only_item);
        this.extractor = extractor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        return bindViewToItem(position, view);
    }

    private View bindViewToItem(int position, TextView view) {
        E item = this.getItem(position);

        if (item instanceof String) {
            // if it is a string, it was already correctly bound by arrayAdapter
            return view;
        }
        if (this.extractor == null) {
            return view;
        }
        if (item != null) {
            view.setText(extractor.apply(item));
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        return bindViewToItem(position, view);
    }
}
