package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.Event;

/**
 * Permintaan membuka tab tertentu di Dashboard Provider dari layar lain (mis. "Lihat Template Anda" setelah template
 * tayang). Disimpan di ViewModel milik Activity karena layar asal tidak memegang Fragment dashboard.
 */
public class ProviderTabRequest extends ViewModel {

    /** Status filter Template Anda yang dibuka bersama tab (null = tidak diubah). */
    public static final class Request {
        public final int tab;
        public final String templatesStatus;

        public Request(int tab, String templatesStatus) {
            this.tab = tab;
            this.templatesStatus = templatesStatus;
        }
    }

    private final MutableLiveData<Event<Request>> request = new MutableLiveData<>();

    public LiveData<Event<Request>> getRequest() {
        return request;
    }

    public void open(int tab, String templatesStatus) {
        request.setValue(new Event<>(new Request(tab, templatesStatus)));
    }
}
