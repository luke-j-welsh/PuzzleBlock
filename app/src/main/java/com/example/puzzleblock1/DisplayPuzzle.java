package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

/**
 * DisplayPuzzle Class is used to display the puzzle to the user
 */
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
    public int category3;
    public ArrayList<Integer> categoryList = new ArrayList<>();

    /**
     * onCreate method runs when the DisplayPuzzle class begins, displays the information to the user
     * @param savedInstanceState
     */
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

        final Button buttonOkay = findViewById(R.id.button4);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
                Cursor resultSet = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '0' WHERE userId=1",null);
                resultSet.moveToFirst();
                startActivity(intent);
                resultSet.close();
                finish();
            }
        });

    }

    /**
     * puzzleCategories method checks the puzzle categories the user has selected in the database
     */
    public void puzzleCategories()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet2.moveToFirst();
        String category1Str = resultSet2.getString(1);
        String category2Str = resultSet2.getString(2);
        String category3Str = resultSet2.getString(3);
        category1 = Integer.parseInt(category1Str);
        category2 = Integer.parseInt(category2Str);
        category3 = Integer.parseInt(category3Str);
        if(category1 == 1)
        {
            categoryList.add(1);
        }
        if(category2==1)
        {
            categoryList.add(2);
        }
        if(category3==1)
        {
            categoryList.add(3);
        }
        resultSet2.close();
    }

    /**
     * puzzleActive method checks if a puzzle is showing or not and sets the database to the correct value
     * @return true if puzzle is active, false if not
     */
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
            resultSet2.close();
            resultSet.close();
            return true;
        }else
        {
            mydatabase.close();
            resultSet2.close();
            return false;
        }

    }

    /**
     * getPuzzle method checks the categories the user needs and chooses the puzzle
     */
    public void getPuzzle(){
        puzzleCategories();
        this.moveTaskToBack(false);
        userComm.setText(null);
        choosePuzzle();
    }

    /**
     * choosePuzzle method chooses the puzzle by using the Random function to choose the puzzle from the database and create an instance of the Puzzle class
     */
    public void choosePuzzle()
    {
        int puzzleId = 0;
        Random random = new Random();
        int puzzleTypeInt = random.nextInt(categoryList.size());
        int chosenCategory = categoryList.get(puzzleTypeInt);

        if (chosenCategory == 1) {
            puzzleId = random.nextInt(30 - 1) + 1;
        } else if(chosenCategory==2){
            puzzleId = random.nextInt(31 - 1) + 1;
        }else if(chosenCategory==3){
            puzzleId = random.nextInt(40 - 1) + 1;
        }
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from Puzzles WHERE puzzleType = " + chosenCategory + " AND typeId =" + puzzleId, null);
        resultSet.moveToFirst();
        String puzzleName = resultSet.getString(0);
        String puzzleType = resultSet.getString(1);
        String puzzleBody = resultSet.getString(3);
        String puzzleAns = resultSet.getString(4);
        userPuzzle = new Puzzle(puzzleId, puzzleName, puzzleType, puzzleBody, puzzleAns);
        String body = userPuzzle.getPuzzleBody();
        puzzleBodyV.setText(body);
        Cursor resultSet2 = mydatabase.rawQuery("Select * from User WHERE userId=1", null);
        resultSet2.moveToFirst();
        livesAmount = resultSet2.getString(6);
        lives.setText(livesAmount);
        mydatabase.close();
        resultSet2.close();
        resultSet.close();
    }


    /**
     * submitAnswer method runs when the submit button is pressed and checks if the user is right or not
     */
    @SuppressLint("SetTextI18n")
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
            resultSet.close();
            finish();
        }else
        {
            int livesNum = Integer.parseInt(livesAmount);
            livesNum = livesNum - 1;
            livesAmount = Integer.toString(livesNum);
            userComm.setText("Incorrect! " + livesAmount + " Lives Remaining!");
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
                resultSet2.close();
                finish();
            }
            resultSet.close();
        }
    }

    /**
     * setBreak method sets the user to be on break after completing a puzzle so the app knows not to block apps for now
     */
    public void setBreak()
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("UPDATE User SET Break = '1' WHERE userId=1",null);
        resultSet.moveToFirst();
        mydatabase.close();
        resultSet.close();
    }


}