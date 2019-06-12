package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.viewmodel.AddMediumViewModel;

public class AddMedium extends BaseFragment {

    private AddMediumViewModel mViewModel;

    public static AddMedium newInstance() {
        return new AddMedium();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        this.setTitle("Add Medium");

        return inflater.inflate(R.layout.add_medium_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

}
