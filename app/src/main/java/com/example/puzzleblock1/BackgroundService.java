package com.example.puzzleblock1;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.os.Process;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * BackgroundService Class runs the ForegroundService
 */
public class BackgroundService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private static final String CHANNEL_ID = "Puzzle" ;
    public View mOverlayView;
    public View overlayFail;
    public boolean puzzleOngoing = false;
    public int failTime;
    public int breakTimeInt;
    public String breakStr;
    public Timer backTimer;
    public Notification notification;
    public NotificationCompat.Builder notificationBuilder;
    public NotificationManager manager;
    public boolean lostTimer = false;
    public ArrayList<String> userChoices = new ArrayList<String>();


    /**
     * ServiceHandler class receives the message from the foreground service and runs the backgroundCheck method
     */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
            backgroundCheck();
        }
    }

    /**
     * onCreate method runs when BackgroundService Class is created and starts the forgreground service and the thread to handle this
     */
    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    /**
     * startMyOwnForeground declares the notification required to start a Foreground Service
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.puzzleblock1";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_puzzle_notify)
                .setContentTitle("Blocking Active!")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
    }

    /**
     * onStartCommand takes the intent provided from the request to start the service and either sends a message to the handler or stops the service
     * @param intent the intent that started the service
     * @param flags the flag attached to the intent
     * @param startId the ID of the start flag
     * @return int for startCommand
     */
    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("start")) {
            getUserChoices();

            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            serviceHandler.sendMessage(msg);
        }
        else if (intent.getAction().equals("stop")) {
            stopForeground(true);
            backTimer.cancel();
            stopSelf();
        }
        return START_STICKY;
    }

    /**
     * onBind method is used to bind the service, this is not used for this implementation
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * backgroundCheck method is used to check the user's activity and decide whether to show the blocking screen
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void backgroundCheck()
    {
        TimerTask backgroundChecker = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                getBreak();
                getLost();
                long end = System.currentTimeMillis();
                long start = end - 5000;
                UsageStatsManager usageStatsManager = (UsageStatsManager) getApplication().getSystemService(Context.USAGE_STATS_SERVICE);
                UsageEvents events = usageStatsManager.queryEvents(start, end);
                while (events.hasNextEvent())
                {
                    UsageEvents.Event nextEvent = new UsageEvents.Event();
                    events.getNextEvent(nextEvent);
                    if(userChoices.contains(nextEvent.getPackageName()))
                    {
                        createOverlay();
                        break;
                    }
                }
            }
        };
        backTimer = new Timer();
        backTimer.schedule(backgroundChecker,0, 5000 );
    }

    /**
     * getUserChoices method runs an SQL query to check what apps the user has blocked and adds them to a linkedlist
     */
    public void getUserChoices()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from UserChoice WHERE userId=1",null);
        resultSet.moveToFirst();
        for(int i = 1; i < resultSet.getColumnCount(); i++)
        {
            String userChoice = resultSet.getString(i);
            int userChoiceInt = Integer.parseInt(userChoice);
            if(userChoiceInt == 1)
            {
                if(i==1)
                {
                    userChoices.add("com.twitter.android");
                }else if (i == 2)
                {
                    userChoices.add("com.snapchat.android");
                }else if (i == 3)
                {
                    userChoices.add("com.facebook.katana");
                }else if (i == 4)
                {
                    userChoices.add("com.facebook.orca");
                }else if (i == 5)
                {
                    userChoices.add("com.instagram.android");
                }else if (i == 6)
                {
                    userChoices.add("com.reddit.frontpage");
                }else if (i == 7)
                {
                    userChoices.add("com.zhiliaoapp.musically");
                }else if (i == 8)
                {
                    userChoices.add("com.google.android.youtube");
                }
            }
        }
        resultSet.close();
    }

    /**
     * createOverlay method is used to create the different overlays required for the blocking function to work succesfully
     */
    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createOverlay()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet2.moveToFirst();
        String livesAmount = resultSet2.getString(6);
        String failTimeStr = resultSet2.getString(5);
        failTime = Integer.parseInt(failTimeStr);
        String active = resultSet2.getString(7);
        int activeInt = Integer.parseInt(active);
        int livesInt = Integer.parseInt(livesAmount);
        resultSet2.close();
        mydatabase.close();
        if(livesInt > 0 && activeInt == 0)
        {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    if(mOverlayView == null )
                    {
                        mOverlayView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.overlay_display, null);
                        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT);
                        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        mWindowManager.addView(mOverlayView, params);

                        final Button buttonOkay = mOverlayView.findViewById(R.id.button3);
                        buttonOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                mOverlayView.setVisibility(View.GONE);

                            }
                        });

                        final Button buttonStart = mOverlayView.findViewById(R.id.button2);
                        buttonStart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent puzzleStart = new Intent(getApplicationContext(), DisplayPuzzle.class);
                                puzzleStart.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(puzzleStart);
                                mOverlayView.setVisibility(View.GONE);
                                puzzleOngoing = true;
                            }
                        });
                    }
                    else if(mOverlayView.getVisibility() == View.GONE )
                    {
                        mOverlayView.setVisibility(View.VISIBLE);

                    }
                }
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runner);
        } else if ((livesInt == 0 && activeInt == 0))
        {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    if (overlayFail != null )
                    {
                        overlayFail.setVisibility(View.VISIBLE);
                    }
                    if(overlayFail == null )
                    {
                        overlayFail = LayoutInflater.from(getApplicationContext()).inflate(R.layout.overlayfail, null);
                        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT);
                        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        mWindowManager.addView(overlayFail, params);

                        final Button buttonOkay = overlayFail.findViewById(R.id.okay);
                        buttonOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overlayFail.setVisibility(View.GONE);
                            }
                        }
                        );
                    }
                }
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runner);
        }
    }


    /**
     * getLost method checks if the user has no lives left and displays the appropriate screen
     */
    public void getLost()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);

        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet2.moveToFirst();
        String livesAmount = resultSet2.getString(6);
        int livesAmountInt = Integer.parseInt(livesAmount);
        String failTimeStr = resultSet2.getString(5);
        failTime = Integer.parseInt(failTimeStr);
        resultSet2.close();
        mydatabase.close();
        final int breakTime = failTime * 60000;
        if(livesAmountInt == 0 && !lostTimer)
        {
            lostTimer = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(breakTime, 1000) {
                        int minute = 0;
                        public void onTick(long millisUntilFinished) {

                            if (minute == 0) {
                                notificationBuilder.setContentTitle("Try Again in: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                notificationBuilder.setContentTitle("Try Again in: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                notificationBuilder.setContentTitle("Try Again in: " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                            manager.notify(2, notificationBuilder.build());
                        }
                        public void onFinish() {
                            SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db", MODE_PRIVATE, null);
                            Cursor resultSet = mydatabase.rawQuery("UPDATE User SET Lives = '3' WHERE userId=1", null);
                            resultSet.moveToFirst();
                            mydatabase.close();
                            lostTimer = false;
                            overlayFail.setVisibility(View.GONE);
                            backgroundNotification(("3 Lives Regained"));
                            notificationBuilder.setContentTitle("Blocking Active!");
                            manager.notify(2, notificationBuilder.build());
                            resultSet.close();
                        }
                    }.start();
                }
            });
        }
    }

    /**
     * getBreak method checks if the user is on a break, creating a notification to show to the user
     */
    public void getBreak()
    {
        checkUser();
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet.moveToFirst();
        String breaker = resultSet.getString(4);
        mydatabase.close();
        resultSet.close();
        int breakTime = breakTimeInt * 60000;
        if(breaker.equals("1"))
        {
            backTimer.cancel();
            final int finalBreakTime = breakTime;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(finalBreakTime, 1000) {
                        int minute = 0;
                        public void onTick(long millisUntilFinished) {
                            if (minute == 0) {
                                notificationBuilder.setContentTitle("Break Time Remaining: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                notificationBuilder.setContentTitle("Break Time Remaining: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                notificationBuilder.setContentTitle("Break Time Remaining: " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                            manager.notify(2, notificationBuilder.build());
                        }

                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onFinish() {
                            SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db", MODE_PRIVATE, null);
                            Cursor resultSet = mydatabase.rawQuery("UPDATE User SET Break = '0' WHERE userId=1", null);
                            resultSet.moveToFirst();
                            mydatabase.close();
                            backgroundNotification(("Break Time Over!"));
                            notificationBuilder.setContentTitle("Blocking Active!");
                            manager.notify(2, notificationBuilder.build());
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            backgroundCheck();
                            resultSet.close();
                        }
                    }.start();
                }

            });
        }
    }

    /**
     * checkUser method checks information in the user table of the database
     */
    public void checkUser() {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            Intent newUser = new Intent(this, UserCreation.class);
            startActivity(newUser);
        } else if (resultSet.getCount() == 1) {
            resultSet.moveToFirst();
            breakStr = resultSet.getString(5);
            breakTimeInt = Integer.parseInt(breakStr);
        }
        mydatabase.close();
        resultSet.close();
    }

    /**
     * backgroundNotification method is used to build the notification
     * @param message the message the user is shown
     */
    public void backgroundNotification(String message){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_puzzle_notify)
                        .setContentTitle("Puzzle Block")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(pendingIntent, true)
                        .setAutoCancel(true);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 1;
        notificationManager.notify(notificationId, incomingCallNotification);
    }
}
