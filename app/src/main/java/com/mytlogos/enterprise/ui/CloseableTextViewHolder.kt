package com.mytlogos.enterprise.ui;

import android.view.View;
import android.widget.TextView;

import com.mytlogos.enterprise.R;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

class CloseableTextViewHolder extends FlexibleViewHolder {
    TextView textView;

    CloseableTextViewHolder(View view, FlexibleAdapter adapter) {
        super(view, adapter);
        this.textView = view.findViewById(R.id.text);
        View closeButton = view.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> adapter.removeItem(this.getFlexibleAdapterPosition()));
    }
}
