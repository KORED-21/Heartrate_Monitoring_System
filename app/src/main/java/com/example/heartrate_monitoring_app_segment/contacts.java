/**
 Copyright [2022] [Eugene John, Joseph Matthew Espinas, Ramon Carmelo Y. Calimbahin, Randy Lance O. Zebroff ]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.example.heartrate_monitoring_app_segment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class contacts extends AppCompatActivity {

    private EditText conE, con1, con2, con3, con4;
    private Button saveCon, clrCon;
    private SharedPreferences userCons;
    private String emerStr, oneStr, twoStr, threeStr, fourStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("HMAS prototype v3.2");

        conE = findViewById(R.id.conEmer);
        con1 = findViewById(R.id.con1);
        con2 = findViewById(R.id.con2);
        con3 = findViewById(R.id.con3);
        con4 = findViewById(R.id.con4);
        saveCon = findViewById(R.id.saveCon);
        clrCon = findViewById(R.id.clrCon);


        //saving user info into a shared preferences
        userCons = getSharedPreferences("MyUserCons", Context.MODE_PRIVATE);
        saveCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });


        //if the user already inputted info before, this will show the previously entered values
        String emerC = userCons.getString("conE", "");
        String oneC = userCons.getString("con1", "");
        String twoC = userCons.getString("con2", "");
        String thrC = userCons.getString("con3", "");
        String fourC = userCons.getString("con4", "");

        if (!(emerC == "")) {
            conE.setText(emerC);
        } else {
            conE.setHint("Enter your local emergency hotline");
        }

        if (!(oneC == "")) {
            con1.setText(oneC);
        } else {
            con1.setHint("Enter a contact number here");
        }

        if (!(twoC == "")) {
            con2.setText(twoC);
        } else {
            con2.setHint("Enter a contact number here");
        }

        if (!(thrC == "")) {
            con3.setText(thrC);
        } else {
            con3.setHint("Enter a contact number here");
        }

        if (!(fourC == "")) {
            con4.setText(fourC);
        } else {
            con4.setHint("Enter a contact number here");
        }

        clrCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conE.setText("");
                con1.setText("");
                con2.setText("");
                con3.setText("");
                con4.setText("");
                saveInfo();
            }
        });

    }//end of onCreate

    private void saveInfo() {
        userCons = getSharedPreferences("MyUserCons", Context.MODE_PRIVATE);
        emerStr = conE.getText().toString();
        oneStr = con1.getText().toString();
        twoStr = con2.getText().toString();
        threeStr = con3.getText().toString();
        fourStr = con4.getText().toString();

        SharedPreferences.Editor editor = userCons.edit();

        editor.putString("conE", emerStr);
        editor.putString("con1", oneStr);
        editor.putString("con2", twoStr);
        editor.putString("con3", threeStr);
        editor.putString("con4", fourStr);
        editor.apply();
        Toast.makeText(contacts.this, "Contacts saved", Toast.LENGTH_SHORT).show();
    }
}