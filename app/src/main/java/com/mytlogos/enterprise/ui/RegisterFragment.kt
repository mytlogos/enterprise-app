package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.viewmodel.UserViewModel
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class RegisterFragment : LoginFragment() {
    private var passwordRepeatView: EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.register, container, false)
        // Set up the login form.
        emailUserNameView = fragment.findViewById(R.id.email) as AutoCompleteTextView?
        populateAutoComplete()
        passwordView = fragment.findViewById(R.id.password) as EditText?
        passwordRepeatView = fragment.findViewById(R.id.repeat_password) as EditText?
        passwordRepeatView!!.setOnEditorActionListener { _: TextView?, id: Int, _: KeyEvent? ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@setOnEditorActionListener true
            }
            false
        }
        val mEmailSignInButton: View = fragment.findViewById(R.id.email_sign_in_button)
        mEmailSignInButton.setOnClickListener { attemptLogin() }
        loginFormView = fragment.findViewById(R.id.login_form)
        progressView = fragment.findViewById(R.id.login_progress)
        val activity = requireActivity()
        userViewModel = ViewModelProvider(activity).get(UserViewModel::class.java)
        return fragment
    }

    override fun attemptLogin() {
        if (authTask != null) {
            return
        }

        // Reset errors.
        passwordView!!.error = null
        passwordRepeatView!!.error = null

        // Store values at the time of the login attempt.
        val password = passwordView!!.text.toString()
        val passwordRepeat = passwordRepeatView!!.text.toString()
        var cancel = false
        var focusView: View? = null
        if (TextUtils.isEmpty(passwordRepeat)) {
            passwordRepeatView!!.error = getString(R.string.error_field_required)
            focusView = passwordRepeatView
            cancel = true
        }
        if (!TextUtils.equals(password, passwordRepeat)) {
            passwordRepeatView!!.error = getString(R.string.error_not_matching)
            focusView = passwordRepeatView
            cancel = true
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            super.attemptLogin()
        }
    }

    override fun getTask(email: String, password: String): UserLoginTask {
        return RegisterTask(email, password, userViewModel!!)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RegisterTask(
        email: String,
        password: String,
        userViewModel: UserViewModel
    ) : UserLoginTask(email, password, userViewModel) {
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                viewModel.register(email, password)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        override fun onPostExecute(success: Boolean) {
            authTask = null
            showProgress(false)
            if (success) {
                Objects.requireNonNull(activity)!!.finish()
            } else {
                emailUserNameView!!.error = getString(R.string.error_register_failed)
                emailUserNameView!!.requestFocus()
            }
        }
    }

    companion object {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @kotlin.jvm.JvmStatic
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }
}