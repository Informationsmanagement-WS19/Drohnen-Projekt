package com.dji.sdk.sample.demo.flightcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.dji.sdk.sample.R;

public class StartMission extends AppCompatActivity {
    TextView tv_preparedData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mission);

        //Retrieve intent added data
        Intent intent = getIntent();
        String intentData = intent.getStringExtra("intent_data");

        initUI();

        tv_preparedData.setText(intentData);
    }


    public void initUI(){
        tv_preparedData = findViewById(R.id.preparedData);
    }
}
