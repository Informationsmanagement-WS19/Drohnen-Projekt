package com.dji.sdk.sample.demo.flightcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dji.sdk.sample.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadMission extends AppCompatActivity {

    private static final String TAG = "Main Activity ->";
    private TextView tv_error;
    private Button btn_getData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_mission);
        initUI();

        //Check permissions
        int checkVal = this.checkCallingOrSelfPermission(Manifest.permission.INTERNET);
        if(checkVal == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Internet granted");
            tv_error.setText(R.string.InternetGranted);

            btn_getData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDataFromDB();
                }
            });
        }else{
            Log.d(TAG, "No internet");
            btn_getData.setEnabled(false);
            tv_error.setText(R.string.InternetNotGranted);
        }
    }

    public void initUI(){
        tv_error = findViewById(R.id.ifError);
        btn_getData = findViewById(R.id.getData);
    }


    public void getDataFromDB(){
        final TextView tv_errorMsg = findViewById(R.id.ifError);
        //Wenn über HotSpot verbunden: ipconfig-> Drahtlos-LAN-Adapter WLAN: -> letztes Mal: 192.168.1.2
        //String jsonURL = "http://10.0.2.2/droneapp/";
        String jsonURL = "http://192.168.1.2/droneapp/";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(jsonURL)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "Access denied");

                final String error = e.toString();

                Log.d("TAG", error);

                LoadMission.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_errorMsg.setText(error);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String dbData = response.body().string();
                createGPSData(dbData);
            }
        });
    }


    public void createGPSData(String data){
        System.out.println("Our Data: " + data);


        Intent intent = new Intent(this, StartMission.class);
        //Transfer the retrieved DB-Data to new intent
        intent.putExtra("intent_data", data);
        startActivity(intent);

    }




}
