package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.viewmodel.AddMediumViewModel

class AddMedium : BaseFragment() {
    private lateinit var mViewModel: AddMediumViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.add_medium_fragment, container, false)
        this.setTitle("Add Medium")
        mViewModel = ViewModelProvider(this).get(AddMediumViewModel::class.java)
        return view
    }

    companion object {
        fun newInstance(): AddMedium {
            return AddMedium()
        }
    }
}