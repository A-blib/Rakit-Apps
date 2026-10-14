package com.aris.templateapp.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.core.template.ImageProcessor;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.repository.UserRepository;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Bagian "Foto profil" di Edit profil, dipakai bersama layar edit pembuat website dan provider. Foto langsung
 * diunggah saat dipilih (tidak menunggu tombol Simpan), sama seperti logo organisasi.
 */
@HiltViewModel
public class AvatarEditViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ImageProcessor imageProcessor;
    private final ThumbnailLoader thumbnailLoader;
    private final AppExecutors executors;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    /** Pesan singkat: foto diperbarui / dihapus / gambar tidak bisa dibaca. */
    private final MutableLiveData<Event<Integer>> message = new MutableLiveData<>();

    @Inject
    public AvatarEditViewModel(UserRepository userRepository, ImageProcessor imageProcessor,
                               ThumbnailLoader thumbnailLoader, AppExecutors executors) {
        this.userRepository = userRepository;
        this.imageProcessor = imageProcessor;
        this.thumbnailLoader = thumbnailLoader;
        this.executors = executors;
        // Data tersimpan di HP cukup untuk menampilkan foto/inisial saat ini.
        user.setValue(userRepository.getCachedUser());
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<Integer>> getMessage() {
        return message;
    }

    public void upload(Uri uri) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            byte[] png;
            try {
                // Persegi 512 px, sama dengan logo organisasi; server memotong & menulis ulang lagi.
                png = imageProcessor.logoPng(uri);
            } catch (IOException | SecurityException e) {
                png = null;
            }
            if (png == null) {
                busy.postValue(false);
                message.postValue(new Event<>(R.string.error_image_unreadable));
                return;
            }
            Resource<User> result = userRepository.uploadAvatar(png);
            if (result.getStatus() == Resource.Status.SUCCESS && result.getData() != null
                    && result.getData().getAvatarUrl() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(png, 0, png.length);
                if (bitmap != null) {
                    thumbnailLoader.put(result.getData().getAvatarUrl(), bitmap);
                }
            }
            finish(result, R.string.avatar_saved);
        });
    }

    public void remove() {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> finish(userRepository.removeAvatar(), R.string.avatar_removed));
    }

    private void finish(Resource<User> result, int doneMessage) {
        busy.postValue(false);
        if (result.getStatus() == Resource.Status.SUCCESS) {
            user.postValue(result.getData());
            message.postValue(new Event<>(doneMessage));
        } else {
            failure.postValue(new Event<>(result.getError()));
        }
    }
}
