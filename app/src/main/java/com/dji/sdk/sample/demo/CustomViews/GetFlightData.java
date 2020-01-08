package com.dji.sdk.sample.demo.CustomViews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;


public class GetFlightData extends AppCompatActivity {

    private static final String TAG = "Main Activity ->";
    private TableLayout gpsTable;
    private String address = "http://192.168.1.2/droneapp/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_get_flight_data);
        gpsTable = (TableLayout) findViewById(R.id.dataTable);
        getDataFromDB();
    }


    //Get all data from the entered address
    private void getDataFromDB(){
        //Dynamic input
        String jsonURL = getAddress();

        //Calling the backend/ entered address
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(jsonURL)
                .build();

        Call call = client.newCall(request);

        //Interpreting the answer (for possible errors)
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ToastUtils.setResultToToast("Access denied");
                final String error = e.toString();
                Log.d("TAG", error);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String dbData = response.body().string();
                createDataTable(dbData);
            }
        });
    }


    //Display the flight data
    private void createDataTable(String data){
        //Replace all special characters
        data = data.replaceAll("\\}\\,\\{",";");
        data = data.replaceAll("\\[","");
        data = data.replaceAll("\\]","");
        data = data.replaceAll("\\}","");
        data = data.replaceAll("\\{","");


        //Splitting all sets
        String [] gpsData = data.split(";");


        //Add a row per data set
        for (int i = 0; i < gpsData.length; i++){
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView tableText = new TextView(this);
            tableText.setText(gpsData[i]);
            row.addView(tableText);
            gpsTable.addView(row, i);
        }
    }


    //Dialog for entering the address
    private String getAddress(){

        //Build new dialog
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Backend Address");

        //Input Text
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        input.setText(address);

        //Confirmation button for entering the address
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                address = input.getText().toString();
            }
        });

        //Cancel button --> Standard value
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

        return address;
    }
}