package com.example.puzzleblock1.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.R;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);

            }
        });

        return root;
    }


    public void sendMessage(View view) {
        Intent intent = new Intent(getActivity(), DisplayPuzzle.class);
        startActivity(intent);
    }

//    public void startPuzzle(int time, final TextView timerDisp, final Intent onFinish)
//    {
//
//        new CountDownTimer(time, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//                timerDisp.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
//                timerDisp.setText("done!");
//                onFinish();
//            }
//        }.start();
//    }
}