package com.mytlogos.enterprise.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.viewmodel.UserViewModel;

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisterFragment extends LoginFragment {

    private EditText passwordRepeatView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.register, container, false);
        // Set up the login form.
        this.emailUserNameView = fragment.findViewById(R.id.email);
        populateAutoComplete();

        this.passwordView = fragment.findViewById(R.id.password);
        this.passwordRepeatView = fragment.findViewById(R.id.repeat_password);

        this.passwordRepeatView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = fragment.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());

        this.loginFormView = fragment.findViewById(R.id.login_form);
        this.progressView = fragment.findViewById(R.id.login_progress);

        FragmentActivity activity = Objects.requireNonNull(getActivity());
        this.userViewModel = ViewModelProviders.of(activity).get(UserViewModel.class);

        return fragment;
    }

    @Override
    void attemptLogin() {
        if (authTask != null) {
            return;
        }

        // Reset errors.
        this.passwordView.setError(null);
        this.passwordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String password = this.passwordView.getText().toString();
        String passwordRepeat = this.passwordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(passwordRepeat)) {
            this.passwordRepeatView.setError(getString(R.string.error_field_required));
            focusView = this.passwordRepeatView;
            cancel = true;
        }

        if (!TextUtils.equals(password, passwordRepeat)) {
            this.passwordRepeatView.setError(getString(R.string.error_not_matching));
            focusView = this.passwordRepeatView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            super.attemptLogin();
        }
    }

    @Override
    UserLoginTask getTask(@NonNull String email, @NonNull String password) {
        return new RegisterTask(email, password, this.userViewModel);
    }

    private class RegisterTask extends UserLoginTask {

        RegisterTask(@NonNull String email, @NonNull String password, UserViewModel userViewModel) {
            super(email, password, userViewModel);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                this.getViewModel().register(this.getEmail(), this.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            authTask = null;
            showProgress(false);

            if (success) {
                Objects.requireNonNull(getActivity()).finish();
            } else {
                emailUserNameView.setError(getString(R.string.error_register_failed));
                emailUserNameView.requestFocus();
            }
        }
    }
}
