package com.example.puzzleblock1.ui.home;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    public int appTime = 10;
    public String appTimeStr = "10";
    public TextView time;
    private static final String CHANNEL_ID = "Puzzle" ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        final FloatingActionButton buttonUp = v.findViewById(R.id.upButton);
        final FloatingActionButton buttonDown = v.findViewById(R.id.downButton);
        final FloatingActionButton buttonStart = v.findViewById(R.id.startButton);


        time = v.findViewById(R.id.userTime);
        String startTxt = (appTimeStr + " Minutes");
        time.setText(startTxt);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);


        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeNew = (timeChange(appTimeStr, "5",1) + " Minutes");
                time.setText(timeNew);
            }
        });

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeNew = (timeChange(appTimeStr, "5",0) + " Minutes");
                time.setText(timeNew);
            }
        });


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appTime = appTime * 1000;
                buttonUp.setVisibility(View.GONE);
                buttonDown.setVisibility(View.GONE);
                buttonStart.setVisibility(View.GONE);
                startPuzzle(appTime, time);
            }
        });



        return v;
    }

    public void startPuzzle(int time, final TextView timerDisp)
    {

        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                timerDisp.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerDisp.setText("done!");
                background();
            }
        }.start();
    }

    public String timeChange(String initial, String addition, int posNeg)
    {
        Integer newTime;
        int timeIn = Integer.parseInt(initial);
        int timeadd = Integer.parseInt(addition);
        if(posNeg == 1)
        {
            newTime = timeIn + timeadd;
        }else
        {
            newTime = timeIn - timeadd;

        }
        appTimeStr = newTime.toString();
        appTime = newTime;
        return appTimeStr;
    }

    public void background(){
        Intent fullScreenIntent = new Intent(getActivity(), DisplayPuzzle.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getActivity(), 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_home_black_24dp)
                        .setContentTitle("Puzzle Time")
                        .setContentText("Time to Complete a Puzzle")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setAutoCancel(true);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        int notificationId = 1;
        notificationManager.notify(notificationId, incomingCallNotification);

    }

}