package com.mytlogos.enterprise.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.model.User;

import java.io.IOException;

public class UserViewModel extends AndroidViewModel {

    private final Repository repository;
    private LiveData<User> user;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        user = repository.getUser();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public boolean isLoading() {
        return repository.isLoading();
    }

    public void updateUser(@NonNull User roomUser) {
        repository.updateUser(roomUser);
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
