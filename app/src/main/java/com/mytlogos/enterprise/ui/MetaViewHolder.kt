package com.mytlogos.enterprise.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mytlogos.enterprise.R;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

class MetaViewHolder extends FlexibleViewHolder {
    final View mView;
    final TextView mainText;
    final TextView topLeftText;
    final TextView topRightText;

    MetaViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter);
        mView = view;
        topLeftText = view.findViewById(R.id.item_top_left);
        topRightText = view.findViewById(R.id.item_top_right);
        mainText = view.findViewById(R.id.content);

    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " '" + mainText.getText() + "'";
    }
}
