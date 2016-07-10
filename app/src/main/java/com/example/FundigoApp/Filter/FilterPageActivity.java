package com.example.FundigoApp.Filter;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.example.FundigoApp.R;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class FilterPageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Serializable, AdapterView.OnItemSelectedListener {
    Integer[] Images = {
                               R.drawable.ic_sport, R.drawable.ic_airplane,
                               R.drawable.ic_beer, R.drawable.ic_buisness,
                               R.drawable.ic_camera, R.drawable.ic_education,
                               R.drawable.ic_gov, R.drawable.ic_home,
                               R.drawable.ic_music
    };
    String sports;
    String travel;
    String drinks;
    String business;
    String fashion;
    String education;
    String government;
    String home_lifeStyle;
    String music;
    String[] num;
    private String[] Names = {};
    private static DatePickerDialog datePickerDialog;
    private static Spinner dateSpinner;
    private static Spinner priceSpinner;
    private static String filter;
    GridView gridView;
    private static String dateFilterSelected;
    private static String priceFilterSelected;
    private static ArrayAdapter<CharSequence> dateAdapter;
    private static ArrayAdapter<CharSequence> priceAdapter;
    private static SharedPreferences _sharedPref;
    private boolean IsCalendarOpened = false;
    private static String subFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_filter_page);
        sports = getApplicationContext ().getString (R.string.sports);
        travel = getApplicationContext ().getString (R.string.travel);
        drinks = getApplicationContext ().getString (R.string.drinks);
        business = getApplicationContext ().getString (R.string.business);
        fashion = getApplicationContext ().getString (R.string.fashion);
        education = getApplicationContext ().getString (R.string.education);
        government = getApplicationContext ().getString (R.string.government);
        home_lifeStyle = getApplicationContext ().getString (R.string.home_lifeStyle);
        music = getApplicationContext ().getString (R.string.music);

        num = new String[]{"Sports",
                                  "Travel",
                                  "Drink",
                                  "Business",
                                  "Fashion",
                                  "Education",
                                  "Government",
                                  "Home and LifeStyle",
                                  "Music"};
        Names = new String[]{sports, travel, drinks, business, fashion, education, government, home_lifeStyle, music};
        setTitle (getApplicationContext ().getString (R.string.filter_page));

        /** Create the Custom Grid View*/
        FilterImageAdapter adapter = new FilterImageAdapter (FilterPageActivity.this, Names, Images);
        gridView = (GridView) findViewById (R.id.grdFilter);
        gridView.setAdapter (adapter);

        /**  On click Event when the custom grid is clickes*/
        gridView.setOnItemClickListener (this);


        dateSpinner = (Spinner) findViewById (R.id.dateFilter);
        dateAdapter = ArrayAdapter.createFromResource (this, R.array.eventDateFilter, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource (android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter (dateAdapter);
        dateSpinner.setOnItemSelectedListener (this);


        priceSpinner = (Spinner) findViewById (R.id.priceFilter);
        priceAdapter = ArrayAdapter.createFromResource (this, R.array.eventPriceFilter, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource (android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter (priceAdapter);
        priceSpinner.setOnItemSelectedListener (this);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Spinner selection options
        Date currentDate = new Date ();

        switch (parent.getId ()) {
            case R.id.dateFilter:
                switch (position) {
                    case 0:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = null;//no filter
                        break;
                    case 1:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = currentDate;//Today
                        break;
                    case 2:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 1);//day after
                        break;
                    case 3:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 2); // day after tomorrow
                        break;
                    case 4:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 1000); // (just as a code to identify weekend selection) = Weekend
                        break;
                    case 5:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        if (IsCalendarOpened == false) {
                            int year = Calendar.getInstance ().get (Calendar.YEAR);
                            int day = Calendar.getInstance ().get (Calendar.DAY_OF_MONTH);
                            int month = Calendar.getInstance ().get (Calendar.MONTH); // date picker conclude months from 0-11
                            datePickerDialog = new DatePickerDialog (this, listener, year, month, day);
                            datePickerDialog.show ();
                        } else {
                            IsCalendarOpened = false;
                        }

                        break;
                }
                break;
            case R.id.priceFilter:
                switch (position) {
                    case 0:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = -1; // no filter
                        break;
                    case 1:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = 0; // Free
                        break;
                    case 2:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = 50; // till 50
                        break;
                    case 3:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = 100; // till 100
                        break;
                    case 4:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = 200; //till 200
                        break;
                    case 5:
                        priceFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_PRICE_FILTER = 201; // higher then 200
                        break;

                }
                break;
        }
        saveInfo ();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) { // click the grid and selct category for filter
        if (filter == null) {
            filter = num[i];
            GlobalVariables.CURRENT_FILTER_NAME = filter;
            view.setBackgroundColor (Color.RED);
            AlertDialog.Builder _builder = new AlertDialog.Builder (this);
            _builder.setPositiveButton (R.string.advanced_filter, new DialogInterface.OnClickListener () {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        openSubCategory (); // assaf added for open Intent to subcategory activity and save info
                    } catch (Exception ex) {
                        Log.e (ex.getMessage (), "advanced filtering exception");
                    }
                }
            });
            _builder.setNeutralButton ("Cancel", new DialogInterface.OnClickListener () {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveInfo ();//save selection
                }
            })
                    .setCancelable (true);
            AlertDialog _alert = _builder.create ();
            _alert.show ();

        } else {
            if (filter.equals (num[i])) { // cancel filter selection - rest mainFilter and subFilter
                filter = null;
                GlobalVariables.CURRENT_FILTER_NAME = "";
                GlobalVariables.CURRENT_SUB_FILTER = "";
                subFilter = null;
                view.setBackgroundColor (Color.TRANSPARENT);
                saveInfo ();  // assaf added for open Intent to subcategory activity
            } else {
                Toast.makeText (this, R.string.can_choice_one_category, Toast.LENGTH_SHORT).show ();
            }
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

        return super.onKeyDown (keyCode, event);
    }

    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener () {
        //Data picker appear when select calendar in filter
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            Calendar calendar = Calendar.getInstance ();
            calendar.set (year, monthOfYear, dayOfMonth);
            GlobalVariables.CURRENT_DATE_FILTER = calendar.getTime ();
        }
    };


    private void saveInfo() { // save the filter info.
        _sharedPref = getSharedPreferences ("filterInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = _sharedPref.edit ();

        editor.putString ("mainFilter", filter);
        editor.putString ("subFilter", subFilter);
        editor.putString ("date", dateFilterSelected);
        editor.putString ("price", priceFilterSelected);
        editor.apply ();
    }


    private String[] getData()
    // display the filter info.
    {
        _sharedPref = getSharedPreferences ("filterInfo", MODE_PRIVATE);
        String _date = _sharedPref.getString ("date", "");
        String _price = _sharedPref.getString ("price", "");

        String[] values = {_date, _price};

        return values;
    }

    private void openSubCategory() {
        Intent intent = new Intent (this, FilterPageActivity2.class);
        intent.putExtra ("mainFilter", filter);
        intent.putExtra ("date", dateFilterSelected);
        intent.putExtra ("price", priceFilterSelected);

        saveInfo ();

        startActivity (intent);
    }

    @Override
    protected void onResume() {

        //when back from subfilter setup activity
        super.onResume ();

        String[] results = getData ();

        if (!results[0].equals ("") || !results[1].equals ("")) {
            if (dateAdapter.getPosition (results[0]) == 5) // prevent open calendar automtically follwoing open in main filter
                IsCalendarOpened = true;
            dateSpinner.setSelection (dateAdapter.getPosition (results[0]));
            priceSpinner.setSelection (priceAdapter.getPosition (results[1]));
        }
    }

    public static Date addDays(Date date, int days)// return the date which is after or before few days from current day
    {
        Calendar cal = Calendar.getInstance ();
        cal.setTime (date);
        cal.add (Calendar.DATE, days);
        return cal.getTime ();
    }

    public static Date getCurrentWeekend() {// return the day after last day of week
        Date date = new Date ();
        Calendar calendar = Calendar.getInstance ();

        calendar.setTime (date);
        int dayOfWeek = calendar.get (Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek ();
        calendar.add (Calendar.DAY_OF_MONTH, -dayOfWeek);// back to start day of week
        //Date weekStart = calendar.getTime();
        calendar.add (Calendar.DAY_OF_MONTH, 7);// the day after last day of week
        Date weekEnd = calendar.getTime ();
        return weekEnd;
    }

}