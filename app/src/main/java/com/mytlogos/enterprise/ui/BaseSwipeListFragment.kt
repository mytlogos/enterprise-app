package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mytlogos.enterprise.R;

abstract class BaseSwipeListFragment<Value, ViewModel extends AndroidViewModel> extends BaseListFragment<Value, ViewModel> {

    private SwipeRefreshLayout listContainer;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        listContainer = view.findViewById(getListContainerId());
        listContainer.setOnRefreshListener(this::onSwipeRefresh);
        return view;
    }

    @LayoutRes
    @Override
    public int getLayoutId() {
        return R.layout.swipe_list;
    }

    @Override
    public SwipeRefreshLayout getListContainer() {
        return listContainer;
    }

    @Override
    int getListContainerId() {
        return R.id.swiper;
    }

    abstract void onSwipeRefresh();
}
