package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.mytlogos.enterprise.MainActivity;
import com.mytlogos.enterprise.R;

import java.util.List;
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

    void openInBrowser(String url, Context context) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "No Link available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        PackageManager manager = Objects.requireNonNull(this.getActivity()).getPackageManager();

        if (intent.resolveActivity(manager) != null) {
            this.startActivity(intent);
        } else {
            Toast.makeText(context, "No Browser available", Toast.LENGTH_SHORT).show();
        }
    }

    boolean checkEmptyList(List<?> list, View root, View listView) {
        TextView textView = root.findViewById(R.id.empty_view);

        // TODO: 22.07.2019 this one not seem to work
        //  it doesn't throw an error, but also does not display any text if empty
        if (list == null || list.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            return true;
        }
        textView.setVisibility(View.GONE);
        return false;
    }
}
