package com.example.drinkingbuddy.Views;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.drinkingbuddy.Controllers.DBHelper;
import com.example.drinkingbuddy.Models.Breathalyzer;
import com.example.drinkingbuddy.R;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    Button newBreath;
    TextView response;
    DBHelper myDB;
    List<Breathalyzer> breathalyzer_values;
    DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDB = new DBHelper(this);
        setContentView(R.layout.activity_main);
        setTitle("Drinking Buddy Home");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initializeComponents();
     //   ((BluetoothHelper)this.getApplicationContext()).BT.connectToBreathalyzer();

    }

        // Link Variables to Components in .XML file
        protected void initializeComponents() {
            newBreath = (Button) findViewById(R.id.newBreath);
            response = (TextView) findViewById(R.id.response);
            newBreath.setOnClickListener(onClickBreathButton);
        }

        // Display the Database
        public void displayResults () {
            breathalyzer_values = myDB.getAllResults();
            ArrayList<String> sampledResults = new ArrayList<>();
            for (int i = breathalyzer_values.size() - 1; i >= 0; i--) {
                double temp = Double.parseDouble(breathalyzer_values.get(i).getResult());
                String timeStamp = breathalyzer_values.get(i).getTimeStamp();
                temp = (((temp - 1500) / 5000));
                if(temp<0) {
                    temp=0;             //this is to avoid negative values and are now considered absolute zero for contraint purposes
                }
                sampledResults.add(String.valueOf(decimalFormat.format(temp) + "%, Time:" + timeStamp));

            }

                response.setText("Your Blood Alchool Level is: " + sampledResults);


        }
    @Override
    protected void onStart() {
        super.onStart();
        displayResults(); //will display nothing if never entered data or most recent value of breathelizer

    }
    private final View.OnClickListener onClickBreathButton= new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            openLoading();

        }

    };


    protected void openLoading(){        //open settings class on click
        Intent i = new Intent(this, Load_Activity.class);
        startActivity(i);

    }

}

