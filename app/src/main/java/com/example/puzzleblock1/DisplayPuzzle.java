package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class DisplayPuzzle extends AppCompatActivity {
    public Puzzle userPuzzle;
    public EditText answerInput;
    public TextView userComm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_puzzle);
        answerInput = findViewById(R.id.userAnswer);
        userComm = findViewById(R.id.textView2);
        getPuzzle();
    }

    public void getPuzzle(){
        this.moveTaskToBack(false);
        userComm.setText(null);
        Random random = new Random();
        int puzzleId =  random.nextInt(4 - 1) + 1;
        System.out.println("HIII " + puzzleId);
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from Puzzles WHERE PuzzleID="+puzzleId,null);
        resultSet.moveToFirst();
        String puzzleName = resultSet.getString(1);
        String puzzleType = resultSet.getString(2);
        String puzzleBody = resultSet.getString(3);
        String puzzleAns = resultSet.getString(4);
        userPuzzle = new Puzzle(puzzleId,puzzleName,puzzleType,puzzleBody,puzzleAns);
        String body = userPuzzle.getPuzzleBody();
        TextView textView = findViewById(R.id.textView);
        textView.setText(body);
    }

    public void reopen() {
        System.out.println("HII Thereee");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
        //  the activity from a service
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
    }


    public void submitAnswer(View view) {
        String answer = answerInput.getText().toString();
        if(answer.equals(userPuzzle.getPuzzleAns()))
        {
            userComm.setText("Correct!");
            this.moveTaskToBack(true);
            answerInput.setText(null);
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {
                    userComm.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    userComm.setText("done!");
                    finish();
                    reopen();
//                    getPuzzle();
                }
            }.start();
        }else
        {
            userComm.setText("Incorrect!");
        }
    }
}