package de.hirola.runningplan.ui.runningplans;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RunningPlansViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RunningPlansViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is running plans fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}