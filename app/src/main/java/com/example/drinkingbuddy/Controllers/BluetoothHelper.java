package com.example.drinkingbuddy.Controllers;
import android.app.Application;     //this allows the bluetooth connection to be maintained throughout all activities.

import com.example.drinkingbuddy.Views.Load_Activity;

public class BluetoothHelper extends Application {

    public Load_Activity BT;

    @Override
    public void onCreate()
    {
        super.onCreate();
        BT = new Load_Activity();
    }
}
