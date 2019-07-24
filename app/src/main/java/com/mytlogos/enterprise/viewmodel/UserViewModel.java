package com.mytlogos.enterprise.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;

import java.io.IOException;

public class UserViewModel extends RepoViewModel {

    private LiveData<User> user;

    public UserViewModel(@NonNull Application application) {
        super(application);
        user = repository.getUser();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public boolean isLoading() {
        return repository.isLoading();
    }

    public void updateUser(@NonNull UpdateUser updateUser) {
        repository.updateUser(updateUser);
    }

    public void login(String user, String password) throws IOException {
        repository.login(user, password);
    }

    public void register(String user, String password) throws IOException {
        repository.register(user, password);
    }

    public void logout() {
        repository.logout();
    }

    @Override
    protected void onCleared() {
        // todo clean up
    }
}
