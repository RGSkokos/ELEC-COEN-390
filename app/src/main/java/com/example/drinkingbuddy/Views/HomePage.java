package com.example.drinkingbuddy.Views;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import com.example.drinkingbuddy.Controllers.DBHelper;
import com.example.drinkingbuddy.Controllers.SharedPreferencesHelper;
import com.example.drinkingbuddy.Models.Breathalyzer;
import com.example.drinkingbuddy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.List;

public class HomePage extends AppCompatActivity {

    //instance variables
    protected BluetoothAdapter bluetoothAdapter;
    protected Button newBreath;
    protected FloatingActionButton specifyDrinkButton;
    protected TextView response;
    protected TextView TimeStampTextview;
    protected TextView CurrentDrinkTextView;
    protected Toolbar toolbar;
    protected BottomNavigationView bottomNav;
    protected DBHelper myDB;
    protected List<Breathalyzer> breathalyzer_values;
    protected DecimalFormat decimalFormat = new DecimalFormat("0.0000");
    private FirebaseAuth firebaseAuth;
    protected int profileId;
    String type_of_drink;
    //private SharedPreferencesHelper sharedPreferencesHelper;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDB = new DBHelper(this);
        firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_home);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initializeComponents();
        setSupportActionBar(toolbar);

        specifyDrinkButton.setOnClickListener(view -> OpenFragment());

        // Set Home selected
        bottomNav.setSelectedItemId(R.id.homeBottomMenuItem);

        // Perform item selected listener
        bottomNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId())
            {
                case R.id.graphsBottomMenuItem:
                    startActivity(new Intent(getApplicationContext(), GraphsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.homeBottomMenuItem:
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayResults(); //will display nothing if never entered data or most recent value of breathalyzer

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null){
            goToLogin();
        }
        // Checks if a user is logged in by getting profile ID

    }

    // Don't allow back button to lead to login page (or anywhere)
    @Override
    public void onBackPressed() { }

    // Link Variables to Components in .XML file
    protected void initializeComponents() {
        newBreath = findViewById(R.id.newBreath);
        response = findViewById(R.id.response);
        CurrentDrinkTextView = findViewById(R.id.CurrentDrinktextView);
        newBreath.setOnClickListener(onClickBreathButton);
        toolbar = findViewById(R.id.toolbarHome);
        bottomNav = findViewById(R.id.bottomNavigation);
        TimeStampTextview = findViewById(R.id.TimeStampTextView);
        specifyDrinkButton = findViewById(R.id.SpecifyDrink);
    }

    //fragment open for type of drink
    private void OpenFragment() {
        TypeOfDrinkFragment dialog = new TypeOfDrinkFragment();
        dialog.show(getSupportFragmentManager(), "TypeOfDrink");
    }


    // Sets up the menu option bar to show profile and logout options
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    // Goes to edit mode if menu option selected
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.trendsMenuItem:
                if(myDB.getAllResults().size() > 0) {
                    goToTrends();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Must have at least one measurement to see trends", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.profileMenuItem:
                goToProfile();
                return true;
            case R.id.logoutMenuItem:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Display the Database
    @SuppressLint("SetTextI18n")
    public void displayResults () {
        breathalyzer_values = myDB.getAllResults();
        String drink = "";
        double temp = 0;
        String timeStamp = "";
        if(myDB.ReturnDrinkTypes().size() > 0) {
            drink = myDB.ReturnDrinkTypes().get(myDB.ReturnDrinkTypes().size() - 1);
        }
        if(breathalyzer_values.size() > 0)
        {
            temp = Double.parseDouble(breathalyzer_values.get(breathalyzer_values.size()-1).getResult());
            timeStamp = breathalyzer_values.get(breathalyzer_values.size() - 1).getTimeStamp();

            temp = (((temp - 150) / 1050)); //second value in numerator needs to be based on calibration
            temp = (temp<0) ? 0 : temp; //this is to avoid negative values and are now considered absolute zero for constraint purposes
        }

        response.setText("Your Blood Alcohol Level is: " + decimalFormat.format(temp) + "%");
        TimeStampTextview.setText("Measurement Taken: " + timeStamp);
        CurrentDrinkTextView.setText("Last Drink: " + drink);
        Log.d("Changing", "Changing Display " + drink);
    }

    private final View.OnClickListener onClickBreathButton= view -> {
        openLoading();
        /*if(type_of_drink != null) {
            myDB.SaveDrinkType(type_of_drink);
        }
        else {
            myDB.SaveDrinkType("Unknown");
        }*/

    };

    protected void openLoading(){        //open settings class on click

        Intent i = new Intent(this, LoadActivity.class);
        //i.putExtra("type_of_drink", type_of_drink);
        startActivity(i);
    }

    protected void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    protected void goToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    protected void logout() {
        FirebaseAuth.getInstance().signOut();
        //sharedPreferencesHelper.saveLoginId(0);
        goToLogin();
    }

    private void goToTrends() {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    public void setTypeOfDrink(String choice) {
        type_of_drink = choice;

    }
}

