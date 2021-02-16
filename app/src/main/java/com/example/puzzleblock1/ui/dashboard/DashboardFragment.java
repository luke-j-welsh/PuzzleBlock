package com.example.puzzleblock1.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.BackgroundService;
import com.example.puzzleblock1.BlockingChoice;
import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.R;
import com.example.puzzleblock1.UserCreation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        final Button userButton = root.findViewById(R.id.userUpdate);
        final Button choiceButton = root.findViewById(R.id.userChoices);
        final Button killButton = root.findViewById(R.id.killButton);

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateUser = new Intent(getActivity(), UserCreation.class);
                startActivity(updateUser);
            }
        });

        killButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopService = new Intent(getContext(), BackgroundService.class);
                stopService.setAction("stop");
                getActivity().startService(stopService);
            }
        });

        choiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userChoice = new Intent(getActivity(), BlockingChoice.class);
                startActivity(userChoice);
            }
        });

        return root;


    }


}