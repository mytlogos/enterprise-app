package com.mytlogos.enterprise.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.mytlogos.enterprise.MainActivity;
import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.MediumType;
import com.mytlogos.enterprise.tools.ContentTool;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.Utils;

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

    void openInBrowser(String url) {
        if (url == null || url.isEmpty()) {
            this.showToast("No Link available");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        PackageManager manager = Objects.requireNonNull(this.getActivity()).getPackageManager();

        if (intent.resolveActivity(manager) != null) {
            this.startActivity(intent);
        } else {
            this.showToast("No Browser available");
        }
    }

    void openInBrowser(List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        if (urls.size() == 1) {
            this.openInBrowser(urls.get(0));
        } else {
            String[] domains = urls.stream().map(Utils::getDomain).toArray(String[]::new);
            new AlertDialog
                    .Builder(requireContext())
                    .setItems(domains, (dialog, which) -> {
                        if (which >= 0 && which < urls.size()) {
                            String url = urls.get(which);
                            this.openInBrowser(url);
                        }
                    })
                    .show();
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

    void showToast(CharSequence msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    void showToast(CharSequence msg, int duration) {
        Toast.makeText(requireContext(), msg, duration).show();
    }

    /**
     * Copied and modified a little from
     * <a href="https://stackoverflow.com/a/17789187">
     * Close/hide the Android Soft Keyboard
     * </a>
     */
    void hideKeyboard() {
        FragmentActivity activity = requireActivity();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = Objects.requireNonNull(getView()).getRootView();
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    void openLocal(int episodeId, int mediumId, int mediumType) {
        Application application = getMainActivity().getApplication();

        Fragment fragment;
        ContentTool tool = FileTools.getContentTool(mediumType, application);

        if (!tool.isSupported()) {
            showToast("This medium type is not yet supported");
            return;
        }
        String path = tool.getItemPath(mediumId);

        if (path.isEmpty()) {
            this.showToast("No Medium Found");
            return;
        }

        switch (mediumType) {
            case MediumType.TEXT:
                fragment = TextViewerFragment.newInstance(episodeId, path);
                break;
            case MediumType.AUDIO:
                fragment = AudioViewerFragment.newInstance(episodeId, path);
                break;
            case MediumType.IMAGE:
                fragment = ImageViewerFragment.newInstance(episodeId, path);
                break;
            case MediumType.VIDEO:
                fragment = VideoViewerFragment.newInstance(episodeId, path);
                break;
            default:
                throw new IllegalArgumentException("Unknown medium type");
        }

        getMainActivity().switchWindow(fragment);
    }
}
