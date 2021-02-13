package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.Random;

public class DisplayPuzzle extends AppCompatActivity {
    public Puzzle userPuzzle;
    public EditText answerInput;
    public TextView userComm;
    public TextView lives;
    public TextView puzzleBodyV;
    public Button submit;
    public String livesAmount;
    public int category1;
    public int category2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_puzzle);
        answerInput = findViewById(R.id.userAnswer);
        userComm = findViewById(R.id.textView2);
        lives = findViewById(R.id.Lives);
        puzzleBodyV = findViewById(R.id.textView);
        submit = findViewById(R.id.button);
        if(puzzleActive()){
            getPuzzle();
        }

    }

    public void puzzleCategories()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet2.moveToFirst();
        String category1Str = resultSet2.getString(1);
        String category2Str = resultSet2.getString(2);
        category1 = Integer.parseInt(category1Str);
        category2 = Integer.parseInt(category2Str);
    }

    public boolean puzzleActive()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet2.moveToFirst();
        String active = resultSet2.getString(7);
        int activeInt = Integer.parseInt(active);
        if(activeInt == 0)
        {
            Cursor resultSet = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '1' WHERE userId=1",null);
            resultSet.moveToFirst();
            mydatabase.close();

            return true;
        }else
        {
            mydatabase.close();
            return false;
        }

    }

    public void getPuzzle(){
        puzzleCategories();
        this.moveTaskToBack(false);
        userComm.setText(null);
        int puzzleId;
        Random random = new Random();
        if(category1 == 1 && category2 == 1)
        {
            int puzzleTypeInt =  random.nextInt(3-1)+1;;
            System.out.println("puzzleType " + puzzleTypeInt);

            if(puzzleTypeInt == 1)
            {
                puzzleId = random.nextInt(10-1)+1;
            }else{
                puzzleId = random.nextInt(15-1)+1;
            }
            System.out.println("puzzleId " + puzzleId);
            SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
            Cursor resultSet = mydatabase.rawQuery("Select * from Puzzles WHERE puzzleType = " + puzzleTypeInt + " AND typeId ="+puzzleId,null);
            resultSet.moveToFirst();
            String puzzleName = resultSet.getString(0);
            String puzzleType = resultSet.getString(1);
            String puzzleBody = resultSet.getString(3);
            String puzzleAns = resultSet.getString(4);
            userPuzzle = new Puzzle(puzzleId,puzzleName,puzzleType,puzzleBody,puzzleAns);
            String body = userPuzzle.getPuzzleBody();
            puzzleBodyV.setText(body);
            Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
            resultSet2.moveToFirst();
            livesAmount = resultSet2.getString(6);
            lives.setText(livesAmount);
            mydatabase.close();
        }else if(category1 == 1 && category2 == 0)
        {
            puzzleId = random.nextInt(10-1)+1;
            System.out.println("puzzleId " + puzzleId);
            SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
            Cursor resultSet = mydatabase.rawQuery("Select * from Puzzles WHERE puzzleType = " + 1 + " AND typeId ="+puzzleId,null);
            resultSet.moveToFirst();
            String puzzleName = resultSet.getString(0);
            String puzzleType = resultSet.getString(1);
            String puzzleBody = resultSet.getString(3);
            String puzzleAns = resultSet.getString(4);
            userPuzzle = new Puzzle(puzzleId,puzzleName,puzzleType,puzzleBody,puzzleAns);
            String body = userPuzzle.getPuzzleBody();
            puzzleBodyV.setText(body);
            Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
            resultSet2.moveToFirst();
            livesAmount = resultSet2.getString(6);
            lives.setText(livesAmount);
            mydatabase.close();
        }else if (category2 == 1 && category1 == 0)
        {
            puzzleId = random.nextInt(15-1)+1;
            System.out.println("puzzleId " + puzzleId);
            SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
            Cursor resultSet = mydatabase.rawQuery("Select * from Puzzles WHERE puzzleType = " + 2 + " AND typeId ="+puzzleId,null);
            resultSet.moveToFirst();
            String puzzleName = resultSet.getString(0);
            String puzzleType = resultSet.getString(1);
            String puzzleBody = resultSet.getString(3);
            String puzzleAns = resultSet.getString(4);
            userPuzzle = new Puzzle(puzzleId,puzzleName,puzzleType,puzzleBody,puzzleAns);
            String body = userPuzzle.getPuzzleBody();
            puzzleBodyV.setText(body);
            Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
            resultSet2.moveToFirst();
            livesAmount = resultSet2.getString(6);
            lives.setText(livesAmount);
            mydatabase.close();
        }






    }




    public void submitAnswer(View view) {
        String answer = answerInput.getText().toString();
        answer = answer.toLowerCase();
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        if(answer.equals(userPuzzle.getPuzzleAns()))
        {
            Cursor resultSet = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '0' WHERE userId=1",null);
            resultSet.moveToFirst();
            userComm.setText("Correct!");
            answerInput.setText(null);
            setBreak();
            finish();
        }else
        {
            userComm.setText("Incorrect!");
            Integer livesNum = Integer.parseInt(livesAmount);

            livesNum = livesNum - 1;
            livesAmount = livesNum.toString();
            Cursor resultSet = mydatabase.rawQuery("UPDATE User SET Lives = "+ livesAmount +" WHERE userId=1",null);
            resultSet.moveToFirst();
            lives.setText(livesAmount);
            if(livesNum <= 0)
            {
                Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '0' WHERE userId=1",null);
                resultSet2.moveToFirst();
                userComm.setVisibility(View.GONE);
                answerInput.setVisibility(View.GONE);
                puzzleBodyV.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
                finish();
            }

        }
    }

    public void setBreak()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("UPDATE User SET Break = '1' WHERE userId=1",null);
        resultSet.moveToFirst();
        mydatabase.close();
    }


}