package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.mytlogos.enterprise.R;

import java.io.File;

class PdfViewerFragment extends TextViewerFragment {
    private PDFView pdfDisplay;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.pdfDisplay = view.findViewById(R.id.pdfView);
        this.setHasOptionsMenu(true);
        this.pdfDisplay.setOnClickListener(v -> this.toggleReadingMode());
        return view;
    }

    @Override
    void updateContent() {
        this.pdfDisplay
                .fromFile(new File(this.currentBook))
                .defaultPage(0)
                .onPageScroll((page, positionOffset) -> System.out.printf("Page: %d and Offset: %s%n", page, positionOffset))
                .scrollHandle(new DefaultScrollHandle(this.requireContext()))
                .load();
    }

    @Override
    int getLayoutRes() {
        return R.layout.fragment_reader_pdf;
    }
}
