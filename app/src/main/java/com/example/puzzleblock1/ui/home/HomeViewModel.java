package com.example.puzzleblock1.ui.home;

import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Start Timer");

    }


    public LiveData<String> getText() {
        return mText;
    }
}