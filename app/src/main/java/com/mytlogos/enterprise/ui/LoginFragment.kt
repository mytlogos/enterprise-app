package com.mytlogos.enterprise.ui

import android.Manifest.permission
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.snackbar.Snackbar
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.*

/**
 * A simple Login Fragment with Email/Username autocomplete.
 * Mostly copied from some Login Template.
 */
open class LoginFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    // UI references.
    protected lateinit var emailUserNameView: AutoCompleteTextView
    protected lateinit var passwordView: EditText
    protected lateinit var progressView: View
    protected lateinit var loginFormView: View
    protected lateinit var userViewModel: UserViewModel

    private var authenticating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val fragmentView = inflater.inflate(R.layout.login, container, false)
        // Set up the login form.
        emailUserNameView = fragmentView.findViewById(R.id.email)
        populateAutoComplete()

        passwordView = fragmentView.findViewById(R.id.password)
        passwordView.setOnEditorActionListener { _: TextView?, id: Int, _: KeyEvent? ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@setOnEditorActionListener true
            }
            false
        }
        val mEmailSignInButton = fragmentView.findViewById<Button>(R.id.email_sign_in_button)
        mEmailSignInButton.setOnClickListener { attemptLogin() }

        loginFormView = fragmentView.findViewById(R.id.login_form)
        progressView = fragmentView.findViewById(R.id.login_progress)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        return fragmentView
    }

    fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }
        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (this.requireActivity()
                .checkSelfPermission(permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        if (shouldShowRequestPermissionRationale(permission.READ_CONTACTS)) {
            Snackbar.make(emailUserNameView,
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {
                    requestPermissions(arrayOf(permission.READ_CONTACTS),
                        REQUEST_READ_CONTACTS)
                }
                .show()
        } else {
            requestPermissions(arrayOf(permission.READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    open fun attemptLogin() {
        if (authenticating) {
            return
        }

        // Reset errors.
        emailUserNameView.error = null
        passwordView.error = null

        // Store values at the time of the login attempt.
        val emailUserName = emailUserNameView.text.toString()
        val password = passwordView.text.toString()
        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.error = getString(R.string.error_invalid_password)
            focusView = passwordView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailUserName)) {
            emailUserNameView.error = getString(R.string.error_field_required)
            focusView = emailUserNameView
            cancel = true
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            authenticating = true
            lifecycleScope.launch { authenticate(emailUserName, password) }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 1
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        loginFormView.visibility = if (show) View.GONE else View.VISIBLE

        loginFormView
            .animate()
            .setDuration(shortAnimTime.toLong())
            .alpha(if (show) 0.0f else 1.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loginFormView.visibility = if (show) View.GONE else View.VISIBLE
                }
            })
        progressView.visibility = if (show) View.VISIBLE else View.GONE
        progressView
            .animate()
            .setDuration(shortAnimTime.toLong())
            .alpha(if (show) 1.0f else 0.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressView.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this.requireActivity(),  // Retrieve data rows for the device user's 'profile' contact.
            Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
            ProfileQuery.PROJECTION,  // Select only email addresses.
            ContactsContract.Contacts.Data.MIMETYPE +
                    " = ?",
            arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),  // Show primary email addresses first. Note that there won't be
            // a primary email address if the user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails: MutableList<String> = ArrayList()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }
        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {}
    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            emailAddressCollection
        )
        emailUserNameView.setAdapter(adapter)
    }

    open suspend fun authenticate(email: String, password: String) {
        val result = runCatching { userViewModel.login(email, password) }
        showProgress(false)

        if (result.isSuccess) {
            requireActivity().finish()
            println("finished login")
        } else {
            passwordView.error = getString(R.string.error_incorrect_password)
            passwordView.requestFocus()
        }
    }

    private interface ProfileQuery {
        companion object {
            val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
            const val ADDRESS = 0
        }
    }

    companion object {
        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @kotlin.jvm.JvmStatic
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}