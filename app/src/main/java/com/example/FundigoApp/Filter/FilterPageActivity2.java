package com.example.FundigoApp.Filter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.MainActivity;
import com.example.FundigoApp.R;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class FilterPageActivity2 extends AppCompatActivity implements AdapterView.OnItemClickListener, Serializable,AdapterView.OnItemSelectedListener {

   // private String sportFilter;
   // private String travelFilter;
    GridView gridView;
    DatePickerDialog datePickerDialog;
    private static String mainfilter;
    private static String subFilter;
    private static String[] sportsFilter;
    private static String[] travelFilter;
    private static String[] drinksFilter;
    private static String[] buisnessFilter;
    private static String[] fashionFilter;
    private static String[] educationFilter;
    private static String[] governmentFilter;
    private static String[] home_lifestyleFilter;
    private static String[] musicFilter;
    private static Spinner dateSpinner;
    private static Spinner priceSpinner;
    private static ArrayAdapter<CharSequence> dateAdapter;
    private static ArrayAdapter<CharSequence> priceAdapter;
    private static String dateFilterSelected;
    private static String priceFilterSelected;
    private static SharedPreferences sharedPref;
    private boolean IsCalendarOpened=false;
//    private static int priceIntegerValueCode;
//    private static Date dateValue;

    private static Integer[] sportImages  = {R.drawable.ic_sport,  R.drawable.ic_sport, R.drawable.ic_sport};
    private static Integer[] travelImages  = {R.drawable.ic_airplane, R.drawable.ic_airplane, R.drawable.ic_airplane   };
    private static Integer[] drinksImages  = {R.drawable.ic_beer, R.drawable.ic_beer, R.drawable.ic_beer  };
    private static Integer[] buisnessImages  = {R.drawable.ic_buisness, R.drawable.ic_buisness, R.drawable.ic_buisness,};
    private static Integer[] fashionImages  = {R.drawable.ic_camera, R.drawable.ic_camera, R.drawable.ic_camera};
    private static Integer[] educationImages  = {R.drawable.ic_education, R.drawable.ic_education, R.drawable.ic_education,};
    private static Integer[] governmentImages  = {R.drawable.ic_gov, R.drawable.ic_gov, R.drawable.ic_gov};
    private static Integer[] home_lifestyleImages  = {R.drawable.ic_home, R.drawable.ic_home, R.drawable.ic_home};
    private static Integer[] musicImages  = {R.drawable.ic_music, R.drawable.ic_music, R.drawable.ic_music};


    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_page2);

        sportsFilter = getResources ().getStringArray (R.array.sportFilters);
        travelFilter = getResources().getStringArray(R.array.travelFilters);
        drinksFilter = getResources().getStringArray(R.array.drinkFilters);
        buisnessFilter = getResources().getStringArray(R.array.businessFilters);
        fashionFilter = getResources().getStringArray(R.array.fashionFilters);
        educationFilter = getResources().getStringArray(R.array.educationFilters);
        governmentFilter = getResources().getStringArray(R.array.governmentFilters);
        home_lifestyleFilter = getResources().getStringArray(R.array.homeLifeStyleFilters);
        musicFilter = getResources().getStringArray(R.array.musicFilters);

        dateSpinner = (Spinner) findViewById(R.id.dateFilter);
        dateAdapter = ArrayAdapter.createFromResource(this, R.array.eventDateFilter, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(this);

        priceSpinner = (Spinner) findViewById(R.id.priceFilter);
        priceAdapter = ArrayAdapter.createFromResource(this, R.array.eventPriceFilter, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);
        priceSpinner.setOnItemSelectedListener(this);

        subFilterPresentbyFilter();
        gridView.setOnItemClickListener(this);

        GlobalVariables.CURRENT_SUB_FILTER="";// clean the sub filter
        mainfilter = GlobalVariables.CURRENT_FILTER_NAME;
        subFilter=null;
        saveInfo();
      }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Spinner selection options
        Date currentDate = new Date();

        switch (parent.getId()) {
            case R.id.dateFilter:
                switch (position) {
                    case 0:
                       dateFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_DATE_FILTER = null;//no filter - date in hte past
                        break;
                    case 1:
                       dateFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_DATE_FILTER = currentDate;//Today
                        break;
                    case 2:
                       dateFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_DATE_FILTER = FilterPageActivity.addDays (currentDate, 1);//day after
                        break;
                    case 3:
                       dateFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_DATE_FILTER = FilterPageActivity.addDays (currentDate, 2); // day after tommorow
                        break;
                    case 4:
                        dateFilterSelected= parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_DATE_FILTER = FilterPageActivity.addDays (currentDate, 1000); // Weekend
                        break;
                    case 5:
                        dateFilterSelected = parent.getItemAtPosition(position).toString();// date from calendar
                        if (IsCalendarOpened==false) {
                        int year = Calendar.getInstance().get(Calendar.YEAR);
                        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        int month = Calendar.getInstance().get(Calendar.MONTH); // date picker conclude months from 0-11
                        datePickerDialog = new DatePickerDialog(this, listener, year, month, day);
                        datePickerDialog.show();
                        }
                        else {
                            IsCalendarOpened = false;
                        }
                        break;
                }
              break;
            case R.id.priceFilter:
                switch (position) {
                    case 0:
                        priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER = -1; // no filter
                        break;
                    case 1:
                       priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER=0; // Free
                        break;
                    case 2:
                        priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER=50; // till 50
                        break;
                    case 3:
                        priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER=100; // till 100
                        break;
                    case 4:
                        priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER=200; //till 200
                        break;
                    case 5:
                        priceFilterSelected = parent.getItemAtPosition(position).toString();
                        GlobalVariables.CURRENT_PRICE_FILTER=201; // higher then 200
                        break;
                }
                break;
        }
        saveInfo();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

     @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) {
         try {
             if (subFilter == null) {
                 subFilter = av.getItemAtPosition(i).toString();
                 GlobalVariables.CURRENT_SUB_FILTER = subFilter;
                 mainfilter = GlobalVariables.CURRENT_FILTER_NAME;
                 view.setBackgroundColor(Color.RED);
                 saveInfo();
             } else {
                 if (subFilter.equals(av.getItemAtPosition(i).toString())){//&& GlobalVariables.CURRENT_SUB_FILTER=="") {
                     subFilter = null;
                     GlobalVariables.CURRENT_SUB_FILTER = "";
                     view.setBackgroundColor(Color.TRANSPARENT);
                     saveInfo();
                 }
                 //press on new Sub filter after back from Main filter. current_sub_filter = "" althouhg sub is not null only in case of new View
                 else if (!subFilter.equals(av.getItemAtPosition(i).toString())&& subFilter.equals(null))
                 {
                     subFilter = av.getItemAtPosition(i).toString();
                     GlobalVariables.CURRENT_SUB_FILTER = subFilter;
                     mainfilter = GlobalVariables.CURRENT_FILTER_NAME;
                     view.setBackgroundColor(Color.RED);
                     saveInfo();
                 }
                 else {
                     Toast.makeText(this, R.string.can_choice_one_category, Toast.LENGTH_SHORT).show();
                 }
             }
         }
         catch (Exception ex)
         {
             Log.e("TAG",ex.getMessage());
         }
   }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt (android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount () == 0) {
            onBackPressed ();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        //Data picker appear when select calendar in filter
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(year,monthOfYear,dayOfMonth);
            GlobalVariables.CURRENT_DATE_FILTER = calendar.getTime();
        }
    };

    private void subFilterPresentbyFilter () // present the Sub filters according to main filter selected previously
    {
        Intent intent = getIntent();
        String filter = intent.getStringExtra("mainFilter");
        String date = intent.getStringExtra("date");
        String price = intent.getStringExtra("price");
        switch (filter)
        {
            case "Sports":
                setGridViewAdapter (sportsFilter,sportImages);
                break;
            case "Travel":
                setGridViewAdapter (travelFilter,travelImages);
                break;
            case "Drink":
                setGridViewAdapter (drinksFilter,drinksImages);
                break;
            case "Business":
                setGridViewAdapter (buisnessFilter,buisnessImages);
                break;
            case "Fashion":
                setGridViewAdapter (fashionFilter,fashionImages);
                break;
            case "Education":
                setGridViewAdapter (educationFilter,educationImages);
                break;
            case "Government":
                setGridViewAdapter (governmentFilter,governmentImages);
                break;
            case "Home and LifeStyle":
                setGridViewAdapter (home_lifestyleFilter,home_lifestyleImages);
                break;
            case "Music":
                setGridViewAdapter (musicFilter,musicImages);
                break;
        }
        if (dateAdapter.getPosition(date)==5) // prevent open calendar automtically follwoing open in main filter
            IsCalendarOpened = true;
       dateSpinner.setSelection(dateAdapter.getPosition(date)); // set the default selection as got from the intent
       priceSpinner.setSelection(priceAdapter.getPosition(price));// set the default selection as got from the intent
        saveInfo();
    }

    private void setGridViewAdapter (String[] names ,Integer[] images)
    {
        FilterImageAdapter adapter = new FilterImageAdapter (FilterPageActivity2.this, names, images);
        gridView = (GridView) findViewById(R.id.grdFilterCategories);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(this);
    }
    public void saveInfo ()
    { // save the filter info.
        sharedPref = getSharedPreferences("filterInfo" ,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("mainFilter",mainfilter);
        editor.putString("date" , dateFilterSelected);
        editor.putString("price" , priceFilterSelected);
        editor.putString("subFilter", subFilter);
        editor.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    public void backToMain(View view)
    {
        Intent intent = new Intent (this,MainActivity.class);
        startActivity(intent);
    }
    }

