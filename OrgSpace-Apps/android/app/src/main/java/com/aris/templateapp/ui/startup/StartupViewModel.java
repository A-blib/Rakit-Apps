package com.aris.templateapp.ui.startup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.repository.ProviderRepository;
import com.aris.templateapp.data.repository.UserRepository;
import com.aris.templateapp.ui.provider.ProviderDashboardViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Menentukan layar pertama saat app dibuka (bagian 6.1). Dipakai StartupFragment, yang menampilkan animasi loading
 * pahlawan selama keputusan belum siap lalu berpindah ke layar tujuan.
 */
@HiltViewModel
public class StartupViewModel extends ViewModel {

    private final MutableLiveData<StartupDecision> decision = new MutableLiveData<>();

    @Inject
    public StartupViewModel(SessionStore sessionStore, UserRepository userRepository, ProviderRepository providerRepository,
                            AppExecutors executors) {
        if (!sessionStore.isIntroSeen()) {
            decision.setValue(StartupDecision.introFirst());
        } else if (!userRepository.hasSession()) {
            decision.setValue(StartupDecision.guest());
        } else {
            executors.networkIO().execute(() -> {
                StartupDecision result = decideForLoggedIn(userRepository);
                // Mumpung animasi loading layar awal masih berjalan: ambil data Beranda provider sekarang, supaya
                // dashboard tidak menampilkan loading untuk kedua kalinya.
                if (result.getDestination() == StartupDecision.Destination.PROVIDER_DASHBOARD) {
                    providerRepository.prefetchDashboard(ProviderDashboardViewModel.PERIOD_7_DAYS);
                }
                decision.postValue(result);
            });
        }
    }

    public LiveData<StartupDecision> getDecision() {
        return decision;
    }

    /** Berjalan di thread latar karena memanggil backend. */
    private static StartupDecision decideForLoggedIn(UserRepository userRepository) {
        Resource<User> result = userRepository.fetchMe();
        if (result.getStatus() == Resource.Status.SUCCESS) {
            return StartupDecision.forUser(result.getData());
        }
        // Gagal: jika 401 dan refresh gagal, TokenAuthenticator sudah menghapus sesi → hasSession() false → tamu.
        // Jika offline/server bermasalah, pakai salinan user di HP agar app tetap bisa dibuka (skenario 12).
        return StartupDecision.forUser(userRepository.getCachedUser());
    }
}
