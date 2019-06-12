package com.mytlogos.enterprise.ui;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.mytlogos.enterprise.MainActivity;

import java.util.Objects;

public class BaseFragment extends Fragment {

    protected void setTitle(String title) {
        this.getMainActivity().setTitle(title);
    }

    protected void setTitle(@StringRes int title) {
        this.getMainActivity().setTitle(title);
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) Objects.requireNonNull(this.getActivity());
    }
}
