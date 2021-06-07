package com.mytlogos.enterprise.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mytlogos.enterprise.MainActivity
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.tools.FileTools.getContentTool
import com.mytlogos.enterprise.tools.Utils

open class BaseFragment : Fragment() {
    protected fun setTitle(title: String?) {
        mainActivity.setTitle(title)
    }

    protected fun setTitle(@StringRes title: Int) {
        mainActivity.setTitle(title)
    }

    protected val mainActivity: MainActivity
        get() = this.requireActivity() as MainActivity

    fun openInBrowser(url: String?) {
        if (url == null || url.isEmpty()) {
            showToast("No Link available")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val manager = this.requireActivity().packageManager
        if (intent.resolveActivity(manager) != null) {
            this.startActivity(intent)
        } else {
            showToast("No Browser available")
        }
    }

    fun openInBrowser(urls: List<String>) {
        if (urls.isEmpty()) {
            return
        }
        if (urls.size == 1) {
            this.openInBrowser(urls[0])
        } else {
            val domains = urls.map { obj: String -> Utils.getDomain(obj) }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setItems(domains) { _: DialogInterface?, which: Int ->
                    if (which >= 0 && which < urls.size) {
                        val url = urls[which]
                        this.openInBrowser(url)
                    }
                }
                .show()
        }
    }

    fun checkEmptyList(list: List<*>?, root: View, listView: View?): Boolean {
        val textView: TextView = root.findViewById(R.id.empty_view)

        // TODO: 22.07.2019 this one not seem to work
        //  it doesn't throw an error, but also does not display any text if empty
        if (list == null || list.isEmpty()) {
            textView.visibility = View.VISIBLE
            return true
        }
        textView.visibility = View.GONE
        return false
    }

    @JvmOverloads
    fun showToast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), msg, duration).show()
    }

    /**
     * Copied and modified a little from
     * [
 * Close/hide the Android Soft Keyboard
](https://stackoverflow.com/a/17789187) *
     */
    fun hideKeyboard() {
        val activity = requireActivity()
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = this.requireView().rootView
        }
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    fun openLocal(episodeId: Int, mediumId: Int, mediumType: Int) {
        val application = mainActivity.application
        val tool = getContentTool(mediumType, application)

        if (!tool.isSupported) {
            showToast("This medium type is not yet supported")
            return
        }
        val path = tool.getItemPath(mediumId)

        if (path?.isEmpty() == true) {
            showToast("No Medium Found")
            return
        }
        val fragment: Fragment = when (mediumType) {
            MediumType.TEXT -> TextViewerFragment.newInstance(episodeId, path)
            MediumType.AUDIO -> AudioViewerFragment.newInstance(episodeId, path)
            MediumType.IMAGE -> ImageViewerFragment.newInstance(episodeId, path)
            MediumType.VIDEO -> VideoViewerFragment.newInstance(episodeId, path)
            else -> throw IllegalArgumentException("Unknown medium type")
        }
        mainActivity.switchWindow(fragment)
    }
}