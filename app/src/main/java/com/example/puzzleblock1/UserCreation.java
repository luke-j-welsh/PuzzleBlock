package com.example.puzzleblock1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_creation);


    }


    public void submitUser(View view){
        EditText editText = (EditText) findViewById(R.id.editBreak);
        String message = editText.getText().toString();
        breakTime = Integer.parseInt(message);
        Switch switch1 = (Switch) findViewById(R.id.switch1);
        Switch switch2 = (Switch) findViewById(R.id.switch2);
        Switch switch3 = (Switch) findViewById(R.id.switch3);

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
            mydatabase.execSQL("INSERT INTO User VALUES('1', " + category1 + ", " + category2 + ", " + category3 + ", " + 0 + ", " + breakTime + " );");
        } else if (resultSet.getCount() == 1)
        {
            Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET Category1 = " + category1 + ", Category2 = " + category2 + ", Category3 = " + category3 + ", BreakTime = " + breakTime + " WHERE userId=1", null);
            resultSet2.moveToFirst();
        }
        mydatabase.close();

        finish();


    }
}