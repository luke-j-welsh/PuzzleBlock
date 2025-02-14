package com.example.puzzleblock1;

import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * MainActivity Class is created on the initialization of the app
 */
public class MainActivity extends AppCompatActivity {
    //Two variables used for notification purposes
    private static final String CHANNEL_ID = "Puzzle" ;
    private static final int APP_PERMISSION_REQUEST = 1 ;

    /**
     * onCreate class runs on the initialization of the app
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        createDatabase();
        createNotificationChannel();
        if (!isAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }

    }

    /**
     * isAccessGranted method is used to check if the permissions are granted
     * @return boolean, true if permissions are granted and false if not
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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


    /**
     * createDatabase function is used to check if the database file already exists within the file and if not, creates it
     */
    public void createDatabase()
    {
        File dbfile = new File(this.getDatabasePath("PuzzleDatabase.db").getPath());
        if (!dbfile.exists())
        {
            String appDataPath = this.getApplicationInfo().dataDir;
            File dbFolder = new File(appDataPath + "/databases");
            dbFolder.mkdir();
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


    }

    /**
     * createNotificationChannel method is used to create the notification channel, this allows notifications to be sent to the device
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}