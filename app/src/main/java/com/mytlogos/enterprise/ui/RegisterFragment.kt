package com.mytlogos.enterprise.ui

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
 * A simple Register-Fragment.
 */
class RegisterFragment : LoginFragment() {
    private lateinit var passwordRepeatView: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.register, container, false)
        // Set up the login form.
        emailUserNameView = fragment.findViewById(R.id.email)
        passwordView = fragment.findViewById(R.id.password)

        passwordRepeatView = fragment.findViewById(R.id.repeat_password)
        passwordRepeatView.setOnEditorActionListener { _: TextView?, id: Int, _: KeyEvent? ->
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

        populateAutoComplete()
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        return fragment
    }

    override fun attemptLogin() {
        // Reset errors.
        passwordView.error = null
        passwordRepeatView.error = null

        // Store values at the time of the login attempt.
        val password = passwordView.text.toString()
        val passwordRepeat = passwordRepeatView.text.toString()
        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(passwordRepeat)) {
            passwordRepeatView.error = getString(R.string.error_field_required)
            focusView = passwordRepeatView
            cancel = true
        }
        if (!TextUtils.equals(password, passwordRepeat)) {
            passwordRepeatView.error = getString(R.string.error_not_matching)
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


    override suspend fun authenticate(email: String, password: String) {
        val result = runCatching { userViewModel.register(email, password) }
        showProgress(false)

        if (result.isSuccess) {
            requireActivity().finish()
            println("finished login")
        } else {
            emailUserNameView.error = getString(R.string.error_register_failed)
            emailUserNameView.requestFocus()
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