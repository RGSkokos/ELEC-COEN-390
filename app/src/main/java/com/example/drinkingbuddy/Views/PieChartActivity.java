package com.example.drinkingbuddy.Views;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.drinkingbuddy.Controllers.DBHelper;
import com.example.drinkingbuddy.Controllers.FirebaseHelper;
import com.example.drinkingbuddy.Models.Breathalyzer;
import com.example.drinkingbuddy.Models.Drink;
import com.example.drinkingbuddy.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

//REFERENCE: https://medium.com/@leelaprasad4648/creating-linechart-using-mpandroidchart-33632324886d
// This code is heavily adapted from the reference above which makes use of MPAndroidChart library
// The library was pulled from the following github: https://github.com/PhilJay/MPAndroidChart

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    private DBHelper database;
    PieChart pieChart;
    List<Breathalyzer> breathalyzer_values;
    Map<String, Integer> DrinkType = new HashMap<>();
    protected FirebaseHelper firebaseHelper;
    protected ListView drinkInputsListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);
        initializeComponents();
    }

    @Override
    protected void onStart(){
        super.onStart();
        insertPieChartValues();
        displayPieChart();
        loadListView();
    }

    protected void initializeComponents(){
        database = new DBHelper(this);
        firebaseHelper = new FirebaseHelper(this);
        pieChart = findViewById(R.id.pieGraph);
        drinkInputsListview = findViewById(R.id.drinkInputsListview);
        breathalyzer_values = database.getAllResults();

        toolbar = findViewById(R.id.PieChartToolbar);
        // Set up the toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void loadListView(){
        ArrayList<Drink> drinks = database.ReturnDrinkTypes();
        ArrayList<String> drinksText = new ArrayList<>();

        for (int i = drinks.size() - 1; i >= 0 ; i--) {
            if(drinks.get(i).getUID().equals(firebaseHelper.getCurrentUID())) {
                String temp = "";

                temp += drinks.get(i).getTimestamp().substring(drinks.get(i).getTimestamp().indexOf(' ')) + "\t\t\t\t\t\t\t" + drinks.get(i).getDrinkName() + "\t\t\t\t\t\t\t\t\t" + drinks.get(i).getQuantity();
                drinksText.add(temp);
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.row, drinksText);
        drinkInputsListview.setAdapter(arrayAdapter);
    }

    //region Pie Chart
    private void insertPieChartValues()
    {//populates values for pie chart entries
        ArrayList<Drink> drinks = database.ReturnDrinkTypes();
        int[] drinkNumber = {0, 0 , 0, 0};

        for (Drink drink: drinks) {
            if(drink.getUID().equals(firebaseHelper.getCurrentUID())) {
                switch (drink.getDrinkName()) {
                    case "liquor":
                        drinkNumber[0] += drink.getQuantity();
                        break;
                    case "wine":
                        drinkNumber[1] += drink.getQuantity();
                        break;
                    case "beer":
                        drinkNumber[2] += drink.getQuantity();
                        break;
                    default:
                        drinkNumber[3] += drink.getQuantity();
                        break;
                }
            }
        }

        DrinkType.put("Liquor",drinkNumber[0]);
        DrinkType.put("Beer",drinkNumber[2]);
        DrinkType.put("Wine",drinkNumber[1]);
        DrinkType.put("Cider", drinkNumber[3]);
    }

    private void displayPieChart(){

        List<PieEntry> pieGraphValues = new ArrayList<>();
        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ColorTemplate.rgb("#F16F6F"));
        colors.add(ColorTemplate.rgb("#50D3A4"));
        colors.add(ColorTemplate.rgb("#3E78CF"));
        colors.add(ColorTemplate.rgb("#F6D47B"));

        //input data and fit data into pie chart entry
        for(String type: DrinkType.keySet()){
            if(DrinkType.get(type) != null)
            {
                pieGraphValues.add(new PieEntry(DrinkType.get(type), type));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(pieGraphValues, "");

        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.WHITE);

        Legend legend = pieChart.getLegend();
        legend.setTextColor(Color.WHITE);

        PieData pieData = new PieData(pieDataSet);

        pieChart.getDescription().setEnabled(false);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
//endregion
}