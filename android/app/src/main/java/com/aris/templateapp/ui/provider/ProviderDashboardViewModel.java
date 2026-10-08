package com.aris.templateapp.ui.provider;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto;
import com.aris.templateapp.data.repository.ProviderRepository;
import com.aris.templateapp.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Data Beranda provider. Dimiliki shell Dashboard Provider (bukan tab Beranda) karena shell juga memakainya
 * untuk badge tab Beranda (jumlah "Perlu tindakan") dan untuk mendeteksi mode provider yang ditangguhkan.
 */
@HiltViewModel
public class ProviderDashboardViewModel extends ViewModel {

    public static final String PERIOD_7_DAYS = "7d";
    public static final String PERIOD_30_DAYS = "30d";

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<ProviderDashboardDto>> dashboard = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> accessLost = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> refreshFailed = new MutableLiveData<>();
    private String period = PERIOD_7_DAYS;
    // Data yang baru dimuat (mis. muat pertama sesaat sebelum tab tampil) tidak perlu langsung dimuat ulang.
    private static final long FRESH_MS = 3000;
    private long loadedAt;

    /** Nomor request terakhir: jawaban request lama (mis. periode sudah diganti lagi) diabaikan. */
    private int generation;

    @Inject
    public ProviderDashboardViewModel(ProviderRepository providerRepository, UserRepository userRepository,
                                      AppExecutors executors) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.executors = executors;
        load(false);
    }

    public LiveData<Resource<ProviderDashboardDto>> getDashboard() {
        return dashboard;
    }

    /**
     * Mode provider tidak bisa dipakai lagi; isinya kode error ({@code PROVIDER_SUSPENDED} atau
     * {@code PROVIDER_REQUIRED}). Layar membawa user ke Dashboard Pembuat Website.
     */
    public LiveData<Event<String>> getAccessLost() {
        return accessLost;
    }

    /** Memuat ulang diam-diam gagal: data lama tetap tampil, layar cukup memberi tahu lewat snackbar. */
    public LiveData<Event<ApiError>> getRefreshFailed() {
        return refreshFailed;
    }

    /** Dipanggil saat tab Beranda tampil lagi: muat ulang diam-diam jika data sudah lebih dari beberapa detik. */
    public void refreshIfStale() {
        if (SystemClock.elapsedRealtime() - loadedAt > FRESH_MS) {
            load(true);
        }
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        if (!period.equals(this.period)) {
            this.period = period;
            load(true);
        }
    }

    /**
     * @param quiet true = data lama tetap tampil selama memuat (ganti periode, kembali ke layar);
     *              false = tampilkan loading penuh (muat pertama, "Coba lagi")
     */
    public void load(boolean quiet) {
        int request = ++generation;
        // Dibuka langsung dari layar awal: pakai data yang sudah diambil selagi animasi loading di sana berjalan.
        ProviderDashboardDto prefetched = providerRepository.takePrefetchedDashboard(period);
        if (prefetched != null) {
            dashboard.setValue(Resource.success(prefetched));
            loadedAt = SystemClock.elapsedRealtime();
            return;
        }
        Resource<ProviderDashboardDto> current = dashboard.getValue();
        if (!quiet || current == null || current.getData() == null) {
            dashboard.setValue(Resource.loading());
        }
        String requestedPeriod = period;
        executors.networkIO().execute(() -> {
            Resource<ProviderDashboardDto> result = providerRepository.dashboard(requestedPeriod);
            if (result.getStatus() == Resource.Status.ERROR && isAccessError(result.getError())) {
                // Salinan user di HP diperbarui agar app dibuka berikutnya langsung memilih rumah yang benar.
                userRepository.fetchMe();
                accessLost.postValue(new Event<>(result.getError().getCode()));
                return;
            }
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                // Gagal memuat ulang secara diam-diam: data lama tetap ditampilkan, periode kembali ke milik data itu.
                if (result.getStatus() == Resource.Status.ERROR && quiet && current != null && current.getData() != null) {
                    if (current.getData().summary != null && current.getData().summary.period != null) {
                        period = current.getData().summary.period;
                    }
                    dashboard.setValue(current);
                    refreshFailed.setValue(new Event<>(result.getError()));
                    return;
                }
                dashboard.setValue(result);
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    loadedAt = SystemClock.elapsedRealtime();
                }
            });
        });
    }

    private static boolean isAccessError(ApiError error) {
        return error != null && ("PROVIDER_SUSPENDED".equals(error.getCode())
                || "PROVIDER_REQUIRED".equals(error.getCode()));
    }
}
