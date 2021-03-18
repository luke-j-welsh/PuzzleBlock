package com.example.puzzleblock1.ui.home;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.BackgroundService;
import com.example.puzzleblock1.BlockingChoice;
import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.MainActivity;
import com.example.puzzleblock1.R;
import com.example.puzzleblock1.UserCreation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * HomeFragment class displays the home screen and runs the main functions of the app
 */
public class HomeFragment extends Fragment {

    private static final int APP_PERMISSION_REQUEST = 1 ;
    public boolean timingActive;
    private HomeViewModel homeViewModel;
    public boolean breakCheck = false;
    public boolean livesCheck = false;
    public int appTime = 60;
    public String appTimeStr = "60";
    public TextView time;
    public TextView breakTimer;
    public TextView title;
    public BottomNavigationView bottomBar;

    public FloatingActionButton buttonUp;
    public FloatingActionButton buttonDown;
    public FloatingActionButton buttonStart;

    private static final String CHANNEL_ID = "Puzzle" ;
    public Timer backTimer = new Timer();
    public Intent backgroundCheckService;
    public int breakTimeInt;
    public String breakStr;


    /**
     * onCreateView method runs when the HomeFragment class begins and initialises the user interface
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        checkUserInit();
        buttonUp = v.findViewById(R.id.upButton);
        buttonDown = v.findViewById(R.id.downButton);
        buttonStart = v.findViewById(R.id.startButton);


        backgroundCheckService = new Intent(getContext(), BackgroundService.class);
        backgroundCheckService.setAction("start");
        breakTimer = v.findViewById(R.id.breakTimer);
        time = v.findViewById(R.id.userTime);
        title = v.findViewById(R.id.textView3);
        bottomBar = getActivity().findViewById(R.id.nav_view);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);


        String startTxt = (appTimeStr + " Minutes");
        time.setText(startTxt);
        backTimer.cancel();
        buttonUp.setVisibility(View.VISIBLE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonStart.setVisibility(View.VISIBLE);

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
                bottomBar.setVisibility(View.GONE);
                title.setText("Digital Detoxing");
                startTimer(appTime, time);

            }
        });

        return v;
    }

    /**
     * checkUserInit method checks if a user exists and whether there is data in the database, if not, some methods are run to initialise the user
     */
    public void checkUserInit() {
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            Intent newUser = new Intent(getContext(), UserCreation.class);
            startActivity(newUser);

            Intent newUser2 = new Intent (getContext(), BlockingChoice.class);
            startActivity(newUser2);
        } else if (resultSet.getCount() == 1)
        {
            resultSet.moveToFirst();
            breakStr = resultSet.getString(5);
            String lives = resultSet.getString(6);
            int livesInt = Integer.parseInt(lives);
            breakTimeInt = Integer.parseInt(breakStr);

            String breakChecker = resultSet.getString(4);
            int breakCheckerInt = Integer.parseInt(breakChecker);

            if(breakCheckerInt == 1)
            {
                Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET Break = '0' WHERE userId=1",null);
                resultSet2.moveToFirst();
                resultSet2.close();
            }
            if(livesInt != 3)
            {
                Cursor resultSet3 = mydatabase.rawQuery("UPDATE User SET Lives = '3' WHERE userId=1",null);
                resultSet3.moveToFirst();
                resultSet3.close();
            }
            Cursor resultSet4 = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '0' WHERE userId=1",null);
            resultSet4.moveToFirst();
            resultSet4.close();
        }
        resultSet.close();
        mydatabase.close();
    }

    /**
     * checkUser method checks a user exists and ensures the break time is correct
     */
    public void checkUser() {
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            Intent newUser = new Intent(getContext(), UserCreation.class);
            startActivity(newUser);
        } else if (resultSet.getCount() == 1)
        {
            resultSet.moveToFirst();
            breakStr = resultSet.getString(5);
            breakTimeInt = Integer.parseInt(breakStr);
        }
        mydatabase.close();
        resultSet.close();
    }


    /**
     * startTimer method begins the timer for a session and starts the foreground service
     * @param time the length of the session
     * @param timerDisp the textview that displays the time left
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTimer(int time, final TextView timerDisp)
    {
        startService();
        displayInfo();

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
                stopService.setAction("stop");
                bottomBar.setVisibility(View.VISIBLE);
                String startTxt = (appTimeStr + " Minutes");
                timerDisp.setText(startTxt);
                backTimer.cancel();
                buttonUp.setVisibility(View.VISIBLE);
                buttonDown.setVisibility(View.VISIBLE);
                buttonStart.setVisibility(View.VISIBLE);
                title.setText("Start Detoxing...");
                appTime = Integer.parseInt(appTimeStr);
                getActivity().startService(stopService);
                backgroundNotification("Detox Complete!");
                backTimer.cancel();
            }
        }.start();
    }

    /**
     * displayInfo method runs the getBreak method with a timer to check if the user is on a break or not
     */
    public void displayInfo()
    {
        TimerTask backgroundChecker = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                getBreak();
            }
        };
        backTimer = new Timer();
        backTimer.schedule(backgroundChecker,0, 5000 );
    }


    /**
     * startService method creates a ForegroundService
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startService(){
        getActivity().startForegroundService(backgroundCheckService);
    }

    /**
     * timeChange method changes the time displayed when the user alters their detox session
     * @param initial the time that was held before changing
     * @param addition the time being added or taken away from the initial time
     * @param posNeg whether it is an increase or decrease in time
     * @return appTimeStr holding the new time in a string
     */
    public String timeChange(String initial, String addition, int posNeg)
    {
        int newTime;
        int timeIn = Integer.parseInt(initial);
        int timeadd = Integer.parseInt(addition);
        if(posNeg == 1)
        {
            newTime = timeIn + timeadd;
        }else
        {
            newTime = timeIn - timeadd;
        }
        appTimeStr = Integer.toString(newTime);
        appTime = newTime;
        return appTimeStr;
    }

    /**
     * backgroundNotification method creates a notification to display to the user
     * @param message the message to display to the user in the notification
     */
    public void backgroundNotification(String message){
        Intent notificationIntent = new Intent(getContext(), MainActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_puzzle_notify)
                        .setContentTitle("Puzzle Block")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(pendingIntent, true)
                        .setAutoCancel(true);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        int notificationId = 1;
        notificationManager.notify(notificationId, incomingCallNotification);

    }


    /**
     * getBreak method checks whether the user is on a break or are blocked and displays this information in the app
     */
    public void getBreak()
    {
        checkUser();
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet.moveToFirst();
        String breaker = resultSet.getString(4);
        String lives = resultSet.getString(6);
        int livesInt = Integer.parseInt(lives);
        mydatabase.close();
        resultSet.close();
        int breakTime = breakTimeInt * 60000;
        if(breaker.equals("1") && !breakCheck)
        {
            final int finalBreakTime = breakTime;
            breakCheck = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(finalBreakTime, 1000) {
                        int minute = 0;
                        public void onTick(long millisUntilFinished) {

                            if (minute == 0) {
                                breakTimer.setText("Break Time Remaining: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                breakTimer.setText("Break Time Remaining: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                breakTimer.setText("Break Time Remaining: " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                        }
                        public void onFinish() {
                            breakTimer.setText(null);
                            breakCheck = false;
                        }
                    }.start();
                }

            });
        }else if ((livesInt == 0) && (!livesCheck))
        {
            final int finalBreakTime = breakTime;
            livesCheck = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(finalBreakTime, 1000) {
                        int minute = 0;
                        public void onTick(long millisUntilFinished) {

                            if (minute == 0) {
                                breakTimer.setText("Try Again in: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                breakTimer.setText("Try Again in: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                breakTimer.setText("Try Again in " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                        }
                        public void onFinish() {
                            breakTimer.setText(null);
                            breakCheck = false;
                        }
                    }.start();
                }

            });
        }
    }

}