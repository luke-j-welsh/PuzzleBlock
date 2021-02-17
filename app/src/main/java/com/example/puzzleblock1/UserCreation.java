package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

public class UserCreation extends AppCompatActivity {

    public int category1 = 0;
    public int category2 = 0;
    public int category3 = 0;
    public int breakTime;
    public int lives = 3;
    public int puzzleActive = 0;
    public int timingActive = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_creation);
    }


    public void submitUser(View view){
        EditText editText = findViewById(R.id.editBreak);
        String message = editText.getText().toString();
        breakTime = Integer.parseInt(message);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switch1 = findViewById(R.id.switch1);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switch2 = findViewById(R.id.switch2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switch3 = findViewById(R.id.switch3);

        if(switch1.isChecked()){
            category1 = 1;
        }
        if(switch2.isChecked()){
            category2 = 1;
        }
        if(switch3.isChecked()){
            category3 = 1;
        }

        SQLiteDatabase mydatabase = openOrCreateDatabase("PuzzleDatabase.db", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            mydatabase.execSQL("INSERT INTO User VALUES('1', " + category1 + ", " + category2 + ", " + category3 + ", " + 0 + ", " + breakTime + ", " + lives + ", " + puzzleActive + ", " + timingActive + " );");
        } else if (resultSet.getCount() == 1)
        {
            Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET Category1 = " + category1 + ", Category2 = " + category2 + ", Category3 = " + category3 + ", BreakTime = " + breakTime + " WHERE userId=1", null);
            resultSet2.moveToFirst();
            resultSet2.close();
        }
        mydatabase.close();
        resultSet.close();
        finish();


    }
}