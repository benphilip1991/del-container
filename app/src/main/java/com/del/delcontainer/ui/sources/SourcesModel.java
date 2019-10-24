package com.del.delcontainer.ui.sources;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SourcesModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SourcesModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is sources fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}