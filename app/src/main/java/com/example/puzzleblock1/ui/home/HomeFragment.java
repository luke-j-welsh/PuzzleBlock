package com.example.puzzleblock1.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.BackgroundService;
import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.startForegroundService;


public class HomeFragment extends Fragment {

    private static final int APP_PERMISSION_REQUEST = 1 ;

    private HomeViewModel homeViewModel;
    public int appTime = 10;
    public String appTimeStr = "10";
    public TextView time;

    private static final String CHANNEL_ID = "Puzzle" ;
    public Timer backTimer = new Timer();




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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                appTime = appTime * 60000;
                buttonUp.setVisibility(View.GONE);
                buttonDown.setVisibility(View.GONE);
                buttonStart.setVisibility(View.GONE);
                startTimer(appTime, time);
            }
        });



        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTimer(int time, final TextView timerDisp)
    {
        final Intent backgroundCheckService = new Intent(getContext(), BackgroundService.class);
        backgroundCheckService.setAction("Start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }
        else
        {
            TimerTask backgroundChecker = new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String completed = getBreak();
                    System.out.println("This oneee " + completed);
                    if(completed.equals("0"))
                    {
                        getActivity().startService(backgroundCheckService);
                    }else
                    {
                        System.out.println("YAYYYYYYYYY");
                    }

                }
            };
            backTimer.schedule(backgroundChecker,0, 5000 );
        }


        new CountDownTimer(time, 1000) {
            int minute = 0;
            public void onTick(long millisUntilFinished) {

               if (minute == 0)
               {
                   timerDisp.setText("Time remaining: " + ((millisUntilFinished /60000) + 1) +" : 00");
                   minute = 59;
               }else if (minute < 10)
               {
                   timerDisp.setText("Time remaining: " + (millisUntilFinished /60000) +" : 0" + (minute));
                   minute = minute -1;
               }else{
                   timerDisp.setText("Time remaining: " + (millisUntilFinished /60000) +" : " + (minute));
                   minute = minute -1;
               }

            }

            public void onFinish() {
                timerDisp.setText("done!");
                Intent stopService = new Intent(getContext(), BackgroundService.class);
                stopService.setAction("Stop");
                getActivity().startService(stopService);
                backTimer.cancel();
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




    public String getBreak()
    {
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet.moveToFirst();
        String breaker = resultSet.getString(4);
        mydatabase.close();
        return breaker;
    }


}