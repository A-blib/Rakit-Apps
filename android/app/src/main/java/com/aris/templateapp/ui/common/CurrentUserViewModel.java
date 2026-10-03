package com.aris.templateapp.ui.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * User yang sedang login menurut salinan di HP (null = tamu). Dipakai dashboard untuk memilih
 * tombol "Masuk" atau avatar, dan Dashboard Provider untuk banner status.
 */
@HiltViewModel
public class CurrentUserViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AppExecutors executors;
    private final MutableLiveData<User> user = new MutableLiveData<>();

    @Inject
    public CurrentUserViewModel(UserRepository userRepository, AppExecutors executors) {
        this.userRepository = userRepository;
        this.executors = executors;
    }

    public LiveData<User> getUser() {
        return user;
    }

    /** Membaca ulang dari penyimpanan (di thread latar), mis. setiap layar kembali tampil setelah login/keluar. */
    public void reload() {
        executors.diskIO().execute(() -> user.postValue(userRepository.getCachedUser()));
    }
}
