package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class DisplayPuzzle extends AppCompatActivity {
    public Puzzle userPuzzle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_puzzle);
        getPuzzle();
        String body = userPuzzle.getPuzzleBody();
        TextView textView = findViewById(R.id.textView);
        textView.setText(body);
    }

    public void getPuzzle(){
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
    }
}