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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class information extends AppCompatActivity {

    private EditText name, age, weight, height;
    private TextView sex, smsSample;
    private Button saveBtn, mEnt, fEnt, clearBtn;
    private SharedPreferences userInfo;
    private String nameStr, ageStr, weightStr, heightStr, sexStr, comp, incomp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("HMAS prototype v3.2");

        name = findViewById(R.id.nameEntry);
        age = findViewById(R.id.ageEntry);
        weight = findViewById(R.id.weightEntry);
        height = findViewById(R.id.heightEntry);
        saveBtn = findViewById(R.id.saveData);
        mEnt = findViewById(R.id.maleEntry);
        fEnt = findViewById(R.id.femaleEntry);
        sex = findViewById(R.id.sexEntry);
        clearBtn = findViewById(R.id.clearData);
        smsSample = findViewById(R.id.smsSample);


        //input for the male sex
        mEnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sex.setText("Male");
            }
        });

        //input for the female sex
        fEnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sex.setText("Female");
            }
        });

        //saving user info into a shared preferences
        userInfo = getSharedPreferences("MyUserInfo", Context.MODE_PRIVATE);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });

        //if the user already inputted info before, this will show the previously entered values
        String nameOut = userInfo.getString("name", "");
        String ageOut = userInfo.getString("age", "");
        String weightOut = userInfo.getString("weight", "");
        String heightOut = userInfo.getString("height", "");
        String sexOut = userInfo.getString("sex", "");

        if (!(nameOut == "")) {
            name.setText(nameOut);
        } else {
            name.setHint("Enter your name");
        }

        if (!(ageOut == "")) {
            age.setText(ageOut);
        } else {
            age.setHint("Age");
        }

        if (!(weightOut == "")) {
            weight.setText(weightOut);
        } else {
            weight.setHint("weight (in Kg)");
        }

        if (!(heightOut == "")) {
            height.setText(heightOut);
        } else {
            height.setHint("height (in cm)");
        }

        if (!(sexOut == "")) {
            sex.setText(sexOut);
        } else {
            sex.setText("");
        }


        //clears the data
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setText("");
                age.setText("");
                weight.setText("");
                height.setText("");
                sex.setText("");
                saveInfo();
            }
        });

    }//end of onCreate

    private void saveInfo() {
        userInfo = getSharedPreferences("MyUserInfo", Context.MODE_PRIVATE);
        nameStr = name.getText().toString();
        ageStr = age.getText().toString();
        weightStr = weight.getText().toString();
        heightStr = height.getText().toString();
        sexStr = sex.getText().toString();

        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("name", nameStr);
        editor.putString("age", ageStr);
        editor.putString("weight", weightStr);
        editor.putString("height", heightStr);
        editor.putString("sex", sexStr);
        editor.commit();
        Toast.makeText(information.this, "Information saved", Toast.LENGTH_SHORT).show();

        smsSample();
    }

    private void smsSample() {
        SharedPreferences userInfo = getSharedPreferences("MyUserInfo", Context.MODE_PRIVATE);
        String name = userInfo.getString("name", "");
        String age = userInfo.getString("age", "");
        String weight = userInfo.getString("weight", "");
        String height = userInfo.getString("height", "");
        String sex = userInfo.getString("sex", "");

        boolean fName = name.isEmpty();
        boolean fAge = age.isEmpty();
        boolean fWeight = weight.isEmpty();
        boolean fHeight = height.isEmpty();
        boolean fSex = sex.isEmpty();

        comp = name
                + " is currently having a heart attack! Their age is "
                + age
                + " years old with a weight of "
                + weight
                + "Kg and a height of "
                + height
                + "cm. The patient is a "
                + sex.toLowerCase()
                + ".";

        incomp = "A person is currently having a heart attack!";

        SharedPreferences fullMessage = getSharedPreferences("fullMessage", MODE_PRIVATE);
        SharedPreferences.Editor edit = fullMessage.edit();
        if (!fName && !fAge && !fWeight && !fHeight && !fSex){
            smsSample.setText(comp);
            edit = fullMessage.edit();
            edit.putString("fullMsg", comp);
            edit.apply();
        }else {
            smsSample.setText(incomp);
            edit = fullMessage.edit();
            edit.putString("fullMsg", incomp);
            edit.apply();
        }
    }
}
