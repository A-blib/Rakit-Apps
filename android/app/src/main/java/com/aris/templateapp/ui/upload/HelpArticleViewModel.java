package com.aris.templateapp.ui.upload;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.HelpArticleDto;
import com.aris.templateapp.data.repository.UploadRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Artikel Panduan untuk satu kode aturan. */
@HiltViewModel
public class HelpArticleViewModel extends ViewModel {

    private final UploadRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<HelpArticleDto>> article = new MutableLiveData<>();
    private String code;

    @Inject
    public HelpArticleViewModel(UploadRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    LiveData<Resource<HelpArticleDto>> getArticle() {
        return article;
    }

    void start(String code) {
        if (code.equals(this.code)) {
            return;
        }
        this.code = code;
        load();
    }

    void load() {
        article.setValue(Resource.loading());
        String current = code;
        executors.networkIO().execute(() -> article.postValue(repository.helpArticle(current)));
    }
}
