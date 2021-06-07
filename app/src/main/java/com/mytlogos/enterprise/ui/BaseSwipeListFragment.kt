package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.AndroidViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mytlogos.enterprise.R

abstract class BaseSwipeListFragment<Value : Any, ViewModel : AndroidViewModel> :
    BaseListFragment<Value, ViewModel>() {
    override lateinit var listContainer: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val localListContainer: SwipeRefreshLayout = view.findViewById(listContainerId) as SwipeRefreshLayout
        listContainer = localListContainer
        localListContainer.setOnRefreshListener { onSwipeRefresh() }
        return view
    }

    @get:LayoutRes
    override val layoutId: Int
        get() = R.layout.swipe_list
    override val listContainerId: Int
        get() = R.id.swiper

    abstract fun onSwipeRefresh()
}