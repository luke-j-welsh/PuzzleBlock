package com.example.puzzleblock1;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final String CHANNEL_ID = "Puzzle" ;



    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        if (!isAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        createNotificationChannel();
//
//        Calendar beginCal = Calendar.getInstance();
//        beginCal.set(Calendar.DATE, 3);
//        beginCal.set(Calendar.MONTH, 1);
//        beginCal.set(Calendar.YEAR, 2021);
//
//        Calendar endCal = Calendar.getInstance();
//        endCal.set(Calendar.DATE, 4);
//        endCal.set(Calendar.MONTH, 1);
//        endCal.set(Calendar.YEAR, 2021);

        Intent backgroundCheckService = new Intent(this, BackgroundService.class);

        startForegroundService(backgroundCheckService);

//        Timer backTimer = new Timer();
//        TimerTask backgroundChecker = new TimerTask() {
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void run() {
//                backgroundCheck();
//            }
//        };
//        backTimer.schedule(backgroundChecker,0, 10000 );




//
//        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MONTH, -1);
//        long start = calendar.getTimeInMillis();
//        long end = System.currentTimeMillis();
//        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, start, end);
//
//        for(int i = 0; i < queryUsageStats.size();i++)
//        {
////            System.out.println("Thiiss onee " + queryUsageStats.get(i).getLastTimeUsed());
//            System.out.println("Package Name: " + queryUsageStats.get(i).getPackageName() + " Last used: " + queryUsageStats.get(i).getLastTimeVisible());
//
//        }

//        background();
    }







    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }





    public void createDatabase()
    {
        String appDataPath = this.getApplicationInfo().dataDir;

        File dbFolder = new File(appDataPath + "/databases");//Make sure the /databases folder exists
        dbFolder.mkdir();//This can be called multiple times.

        File dbFilePath = new File(appDataPath + "/databases/PuzzleDatabase.db");

        try {
            InputStream inputStream = this.getAssets().open("PuzzleDatabase.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer))>0)
            {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e){
            //handle
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }




}