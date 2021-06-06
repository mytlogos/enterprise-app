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
    private var pdfDisplay: PDFView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        pdfDisplay = view.findViewById(R.id.pdfView) as PDFView?
        setHasOptionsMenu(true)
        pdfDisplay!!.setOnClickListener { toggleReadingMode() }
        return view
    }

    override fun updateContent() {
        pdfDisplay!!
            .fromFile(File(currentBook))
            .defaultPage(0)
            .onPageScroll { page: Int, positionOffset: Float ->
                System.out.printf("Page: %d and Offset: %s%n",
                    page,
                    positionOffset)
            }
            .scrollHandle(DefaultScrollHandle(requireContext()))
            .load()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_reader_pdf
}