package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.mytlogos.enterprise.R
import java.io.File

internal class PdfViewerFragment : TextViewerFragment() {
    private lateinit var pdfDisplay: PDFView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        pdfDisplay = view.findViewById(R.id.pdfView)
        setHasOptionsMenu(true)
        pdfDisplay.setOnClickListener { toggleReadingMode() }
        return view
    }

    override fun updateContent() {
        pdfDisplay
            .fromFile(File(currentBook))
            .defaultPage(0)
            .onPageScroll { page: Int, positionOffset: Float -> println("Page: $page and Offset: $positionOffset") }
            .scrollHandle(DefaultScrollHandle(requireContext()))
            .load()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_reader_pdf
}