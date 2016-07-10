package com.example.FundigoApp.Filter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class FilterPageActivity2 extends AppCompatActivity implements AdapterView.OnItemClickListener, Serializable,AdapterView.OnItemSelectedListener {

   // private String sportFilter;
   // private String travelFilter;
    GridView gridView;
    DatePickerDialog datePickerDialog;
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
    private static String[] sportsFilterName;
    private static String[] travelFilterName;
    private static String[] drinksFilterName;
    private static String[] buisnessFilterName;
    private static String[] fashionFilterName;
    private static String[] educationFilterName;
    private static String[] governmentFilterName;
    private static String[] home_lifestyleFilterName;
    private static String[] musicFilterName;
    private static Spinner dateSpinner;
    private static Spinner priceSpinner;
    private static ArrayAdapter<CharSequence> dateAdapter;
    private static ArrayAdapter<CharSequence> priceAdapter;
    private static String dateFilterSelected;
    private static String priceFilterSelected;
    private static SharedPreferences sharedPref;
    private boolean IsCalendarOpened=false;
    private String [] subFilterActualFiltersArray;
    private String subFilterToDisplayInEventsPage;
    private String filterNameToPresentInEventPage;

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

        sportsFilter = getResources ().getStringArray (R.array.sportFiltersAllLanguages);
        travelFilter = getResources().getStringArray(R.array.travelFiltersAllLanguages);
        drinksFilter = getResources().getStringArray(R.array.drinkFiltersAllLanguages);
        buisnessFilter = getResources().getStringArray(R.array.businessFiltersAllLanguages);
        fashionFilter = getResources().getStringArray(R.array.fashionFiltersAllLanguages);
        educationFilter = getResources().getStringArray(R.array.educationFiltersAllLanguages);
        governmentFilter = getResources().getStringArray(R.array.governmentFiltersAllLanguages);
        home_lifestyleFilter = getResources().getStringArray(R.array.homeLifeStyleFiltersAllLanguages);
        musicFilter = getResources().getStringArray(R.array.musicFiltersAllLanguages);

        sportsFilterName = getResources().getStringArray(R.array.sportFiltersName);
        travelFilterName = getResources().getStringArray(R.array.travelFiltersName);
        drinksFilterName = getResources().getStringArray(R.array.drinkFiltersName);
        buisnessFilterName = getResources().getStringArray(R.array.businessFiltersName);
        fashionFilterName = getResources().getStringArray(R.array.fashionFiltersName);
        educationFilterName = getResources().getStringArray(R.array.educationFiltersName);
        governmentFilterName = getResources().getStringArray(R.array.governmentFiltersName);
        home_lifestyleFilterName = getResources().getStringArray(R.array.homeLifeStyleFiltersName);
        musicFilterName = getResources().getStringArray(R.array.musicFiltersName);

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
                        GlobalVariables.CURRENT_DATE_FILTER=null;// delete previous selections to cover case that selection from calendar was canceled
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
                 subFilterToDisplayInEventsPage = av.getItemAtPosition(i).toString();
                 subFilter = subFilterActualFiltersArray[i];
                 GlobalVariables.CURRENT_SUB_FILTER = subFilter;
                 view.setBackgroundColor(Color.RED);
                 saveInfo();
             } else {
                 if (subFilter.equals(subFilterActualFiltersArray[i])){//&& GlobalVariables.CURRENT_SUB_FILTER=="") {
                     subFilterToDisplayInEventsPage = null;
                     subFilter = null;
                     GlobalVariables.CURRENT_SUB_FILTER = "";
                     view.setBackgroundColor(Color.TRANSPARENT);
                     saveInfo();
                 }
                 //press on new Sub filter after back from Main filter. current_sub_filter = "" althouhg sub is not null only in case of new View
                  else if (!subFilter.equals(subFilterActualFiltersArray[i])&& subFilter.equals(null))
                 {
                     subFilter =  subFilterActualFiltersArray[i];
                     subFilterToDisplayInEventsPage = av.getItemAtPosition(i).toString();
                     GlobalVariables.CURRENT_SUB_FILTER = subFilter;
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
                setGridViewAdapter (sportsFilterName,sportImages,sportsFilter);
                break;
            case "Travel":
                setGridViewAdapter (travelFilterName,travelImages,travelFilter);
                break;
            case "Drink":
                setGridViewAdapter (drinksFilterName,drinksImages,drinksFilter);
                break;
            case "Business":
                setGridViewAdapter (buisnessFilterName,buisnessImages,buisnessFilter);
                break;
            case "Fashion":
                setGridViewAdapter (fashionFilterName,fashionImages,fashionFilter);
                break;
            case "Education":
                setGridViewAdapter (educationFilterName,educationImages,educationFilter);
                break;
            case "Government":
                setGridViewAdapter (governmentFilterName,governmentImages,governmentFilter);
                break;
            case "Home and LifeStyle":
                setGridViewAdapter (home_lifestyleFilterName,home_lifestyleImages,home_lifestyleFilter);
                break;
            case "Music":
                setGridViewAdapter (musicFilterName,musicImages,musicFilter);
                break;
        }
        if (dateAdapter.getPosition(date)==5) // prevent open calendar automtically follwoing open in main filter
            IsCalendarOpened = true;
       dateSpinner.setSelection(dateAdapter.getPosition(date)); // set the default selection as got from the intent
       priceSpinner.setSelection(priceAdapter.getPosition(price));// set the default selection as got from the intent
        saveInfo();
    }

    private void setGridViewAdapter (String[] filterName ,Integer[] images,String filters[])
    {
        FilterImageAdapter adapter = new FilterImageAdapter (FilterPageActivity2.this, filterName, images,filters);
        gridView = (GridView) findViewById(R.id.grdFilterCategories);
        gridView.setAdapter(adapter);

        subFilterActualFiltersArray = Arrays.copyOf(filters,filters.length);// Take the Array of filters that currently filtered
        gridView.setOnItemClickListener(this);
    }
    public void saveInfo ()
    { // save the filter info.
        sharedPref = getSharedPreferences("filterInfo" ,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("mainFilter",filterNameToPresentInEventPage);
        editor.putString("date" , dateFilterSelected);
        editor.putString("price", priceFilterSelected);
        editor.putString("subFilter", subFilterToDisplayInEventsPage);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String filterNameValue = getData();
        filterNameToPresentInEventPage = filterNameValue;
        subFilterPresentbyFilter();
        gridView.setOnItemClickListener(this);
        GlobalVariables.CURRENT_SUB_FILTER="";// clean the sub filter
        subFilter=null;
        subFilterToDisplayInEventsPage=null;
        saveInfo();

    }

    public void backToMain(View view) // This Page prevented to be back to. Set in Android Manifest file
    {
        Intent intent = new Intent (this,MainActivity.class);
        startActivity(intent);
      }

    public void backToFilter(View view) // This Page prevented to be back to. Set in Android Manifest file
    {
        Intent intent = new Intent (this,FilterPageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {//prevent the back Button to main filter
       return;
    }

    private String getData()
    // display the filter info.
    {
        sharedPref = getSharedPreferences ("filterInfo", MODE_PRIVATE);
        String _filterName = sharedPref.getString("mainFilter", "");
        return _filterName;
    }

}

