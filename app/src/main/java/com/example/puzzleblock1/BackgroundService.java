package com.example.puzzleblock1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.os.Process;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BackgroundService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private static final String CHANNEL_ID = "Puzzle" ;
    public View mOverlayView;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
            backgroundCheck();
            stopSelf(msg.arg1);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
        }
    }

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        super.onCreate();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            startMyOwnForeground();
//        else
//            startForeground(1, new Notification());
    HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();



        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.puzzleblock1";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction().equals("Start")) {
//            Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            serviceHandler.sendMessage(msg);
        }
        else if (intent.getAction().equals("Stop")) {
            //your end servce code
            stopForeground(true);
            stopSelfResult(startId);
        }
        return START_STICKY;


    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void backgroundCheck()
    {
        long end = System.currentTimeMillis();
        long start = end - 5000;
        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents events = usageStatsManager.queryEvents(start, end);
//        System.out.println("This one: " + usageStatsManager.isAppInactive("com.google.android.youtube"));
        while (events.hasNextEvent())
        {
            UsageEvents.Event nextEvent = new UsageEvents.Event();
            events.getNextEvent(nextEvent);
            System.out.println("| This one: " + nextEvent.getPackageName() + "| Time: " + nextEvent.getTimeStamp());

            if(nextEvent.getPackageName().equals("com.twitter.android"))
            {
                createOverlay();
//                Intent starter = new Intent(this, DisplayPuzzle.class);
//                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(starter);
            }
        }
    }

    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createOverlay()
    {
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                if(mOverlayView == null)
                {
                    mOverlayView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.overlay_display, null);
                    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                            WindowManager.LayoutParams.WRAP_CONTENT,
//                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                    WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    mWindowManager.addView(mOverlayView, params);

                    final Button buttonStart = mOverlayView.findViewById(R.id.button2);
                    buttonStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent puzzleStart = new Intent(getApplicationContext(), DisplayPuzzle.class);
                            puzzleStart.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(puzzleStart);
                            Intent intent = new Intent("puzzleComplete");
                            intent.putExtra("key","True");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            sendBroadcast(intent);
                            mOverlayView.setVisibility(View.GONE);
                        }
                    });
                }
                else if(mOverlayView.getVisibility() == View.GONE)
                {
                    mOverlayView.setVisibility(View.VISIBLE);

                }


            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runner);

    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
