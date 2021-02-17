package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class BlockingChoice extends AppCompatActivity {

    public int twit = 0;
    public int snap = 0;
    public int face = 0;
    public int faceMess = 0;
    public int insta = 0;
    public int reddit = 0;
    public int tik = 0;
    public int youtube = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocking_choice);
    }

    public void submitUser(View view){

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchTwit = findViewById(R.id.switchTwitter);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchSnap = findViewById(R.id.switchSnap);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchFace = findViewById(R.id.switchFace);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchFaceMess = findViewById(R.id.switchFaceMess);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchInsta = findViewById(R.id.switchInsta);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchReddit = findViewById(R.id.switchReddit);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchTik = findViewById(R.id.switchTikTok);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchYoutube = findViewById(R.id.switchYoutube);

        if(switchTwit.isChecked()){
            twit = 1;
        }
        if(switchSnap.isChecked()){
            snap = 1;
        }
        if(switchFace.isChecked()){
            face = 1;
        }
        if(switchFaceMess.isChecked()){
            faceMess = 1;
        }
        if(switchInsta.isChecked()){
            insta = 1;
        }
        if(switchReddit.isChecked()){
            reddit = 1;
        }
        if(switchTik.isChecked()){
            tik = 1;
        }
        if(switchYoutube.isChecked()){
            youtube = 1;
        }

        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from UserChoice WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            mydatabase.execSQL("INSERT INTO UserChoice VALUES('1', " + twit + ", " + snap + ", " + face + ", " + faceMess + ", " + insta + ", " + reddit + ", " + tik + ", " + youtube + " );");
        } else if (resultSet.getCount() == 1)
        {
            Cursor resultSet2 = mydatabase.rawQuery("UPDATE UserChoice SET twitter = " + twit + ", snapchat = " + snap + ", facebook = " + face + ", facebookMess = " + faceMess + ", insta = "+ insta + ", reddit = " + reddit + ", tiktok = " + tik + ", youtube = " + youtube +" WHERE userId=1", null);
            resultSet2.moveToFirst();
            resultSet2.close();

        }
        resultSet.close();
        mydatabase.close();

        finish();


    }
}