package com.example.FundigoApp.Filter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterPageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Serializable, AdapterView.OnItemSelectedListener {
    Integer[] Images = {
                               R.drawable.ic_sport, R.drawable.ic_airplane,
                               R.drawable.ic_beer, R.drawable.ic_buisness,
                               R.drawable.ic_camera, R.drawable.ic_education,
                               R.drawable.ic_gov, R.drawable.ic_home,
                               R.drawable.ic_music,  R.drawable.ic_sport, R.drawable.ic_airplane,
                               R.drawable.ic_beer, R.drawable.ic_buisness,
                               R.drawable.ic_camera, R.drawable.ic_education,
                               R.drawable.ic_gov, R.drawable.ic_home,
                               R.drawable.ic_music, R.drawable.ic_sport, R.drawable.ic_airplane,
                               R.drawable.ic_beer, R.drawable.ic_buisness,
                               R.drawable.ic_camera, R.drawable.ic_education,
                               R.drawable.ic_gov,R.drawable.ic_home,
                               R.drawable.ic_music,  R.drawable.ic_sport, R.drawable.ic_airplane,
                               R.drawable.ic_beer
    };

    //24.09 - assaf: fb filter added
    String sports;
    String tourism;
    String party;
    String business;
    String comedy;
    String workshop;
    String kids;
    String lifeStyle;
    String music;
    String art;//fb filter
    String book;//fb filter
    String movie;//fb filter
    String fundraiser;//fb filter
    String volunteering;//fb filter
    String family;//fb filter
    String festival;//fb filter
    String neighborhood;//fb filter
    String religious;//fb filter
    String shopping;//fb filter
    String nightLife;//fb filter
    String theater;//fb filter
    String dining;//fb filter
    String food;//fb filter
    String fitness;//fb filter
    String dance;//fb filter
    String conference;//fb filter
    String meetup;//fb filter
    String class_event; //fb filter
    String lecture; //fb filter
    String other; //fb filter
    String[] num;
    private String[] Names = {};
    private static DatePickerDialog datePickerDialog;
    private static Spinner dateSpinner;
    private static Spinner priceSpinner;
    private static String filter;
    GridView gridView;
    private static String dateFilterSelected;
    private static String dateFilterFromSelected=""; //18.10 assaf: support from to date Range
    private static String dateFilterToSelected="";//18.10 assaf: support from to date Range
    private static String priceFilterSelected;
    private static ArrayAdapter<CharSequence> dateAdapter;
    private static ArrayAdapter<CharSequence> priceAdapter;
    private static SharedPreferences _sharedPref;
   // private boolean IsCalendarOpened = false;
    private static String subFilter;
    private String filterNameToPresentInEventPage;
    private String subFilterNameToPresentInEventPage;
    Button fromButton;
    Button toButton ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_page);
        sports = getApplicationContext ().getString (R.string.sports);
        tourism = getApplicationContext ().getString (R.string.travel);
        party = getApplicationContext ().getString (R.string.drinks);
        business = getApplicationContext ().getString (R.string.business);
        comedy = getApplicationContext ().getString (R.string.fashion);
        workshop = getApplicationContext ().getString (R.string.education);
        kids = getApplicationContext ().getString (R.string.government);
        lifeStyle = getApplicationContext ().getString (R.string.home_lifeStyle);
        music = getApplicationContext ().getString (R.string.music);
        art =getApplicationContext ().getString (R.string.art);
        book =getApplicationContext ().getString (R.string.book);
        movie =getApplicationContext ().getString (R.string.movie);
        fundraiser =getApplicationContext ().getString (R.string.fundraiser);
        volunteering =getApplicationContext ().getString (R.string.volunteering);
        family =getApplicationContext ().getString (R.string.family);
        festival =getApplicationContext ().getString (R.string.festival);
        neighborhood = getApplicationContext ().getString (R.string.neighborhood);
        religious =getApplicationContext ().getString (R.string.religious);
        shopping =getApplicationContext ().getString (R.string.shopping);
        nightLife =getApplicationContext ().getString (R.string.nightLife);
        theater =getApplicationContext ().getString (R.string.theater);
        dining =getApplicationContext ().getString (R.string.dining);
        food =getApplicationContext ().getString (R.string.food);
        fitness =getApplicationContext ().getString (R.string.fitness);
        dance =getApplicationContext ().getString (R.string.dance);
        conference =getApplicationContext ().getString (R.string.conference);
        meetup =getApplicationContext ().getString (R.string.meetup);
        class_event =getApplicationContext ().getString (R.string.class_event);
        lecture =getApplicationContext ().getString (R.string.lecture);
        other =getApplicationContext ().getString (R.string.other);

        //24.09 - to support filter in Hebrew and keep last filter selcted by user
        num = new String[]{"Sports", // for filter puprposev. it is include whogo filters and also FB filters
                                  "Tourism",
                                  "Party",
                                  "Business",
                                  "Comedy",
                                  "Workshop",
                                  "Kids",
                                  "LifeStyle",
                                  "Music","ART_EVENT","BOOK_EVENT","MOVIE_EVENT","FUNDRAISER","VOLUNTEERING","FAMILY_EVENT","FESTIVAL_EVENT",
                                  "NEIGHBORHOOD","RELIGIOUS_EVENT","SHOPPING","NIGHTLIFE","THEATER_EVENT","DINING_EVENT","FOOD_TASTING","FITNESS",
                                  "DANCE_EVENT","CONFERENCE_EVENT","MEETUP","CLASS_EVENT","LECTURE","OTHER"};

        Names = new String[]{sports, tourism, party, business, comedy, workshop, kids, lifeStyle, music,
                art,book,movie,fundraiser,volunteering,family,festival,neighborhood,religious,shopping,nightLife,theater,dining,food,fitness,
                dance,conference,meetup,class_event,lecture,other};//for filters topics titles (per Lanuguage)

        setTitle (getApplicationContext ().getString (R.string.filter_page));

        /** Create the Custom Grid View*/
//        FilterImageAdapter adapter = new FilterImageAdapter (FilterPageActivity.this, Names, Images,num);
//        gridView = (GridView) findViewById (R.id.grdFilter);
//        gridView.setAdapter (adapter);

        /**  On click Event when the custom grid is clickes*/
       // gridView.setOnItemClickListener (this);


        dateSpinner = (Spinner) findViewById (R.id.dateFilter);
        dateAdapter = ArrayAdapter.createFromResource (this, R.array.eventDateFilter, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(this);


        priceSpinner = (Spinner) findViewById (R.id.priceFilter);
        priceAdapter = ArrayAdapter.createFromResource (this, R.array.eventPriceFilter, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);
        priceSpinner.setOnItemSelectedListener(this);

        fromButton = (Button) findViewById(R.id.fromDateButton);
        toButton = (Button) findViewById(R.id.toDateButton);
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
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        dateFilterToSelected="";
                        dateFilterFromSelected="";
                        break;
                    case 1:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = currentDate;//Today
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        dateFilterToSelected="";
                        dateFilterFromSelected="";
                        break;
                    case 2:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 1);//day after
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        dateFilterToSelected="";
                        dateFilterFromSelected="";
                        break;
                    case 3:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 2); // day after tomorrow
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        dateFilterToSelected="";
                        dateFilterFromSelected="";
                        break;
                    case 4:
                        dateFilterSelected = parent.getItemAtPosition (position).toString ();
                        GlobalVariables.CURRENT_DATE_FILTER = addDays (currentDate, 1000); // (just as a code to identify weekend selection) = Weekend
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        dateFilterToSelected="";
                        dateFilterFromSelected="";
                        break;
                    case 5:
                          dateFilterSelected = parent.getItemAtPosition (position).toString ();
                       // if (IsCalendarOpened == false) {
                            GlobalVariables.CURRENT_DATE_FILTER=null;// delete previous selections to cover case that selection from calendar was canceled
                            //after it was opened
                            int year = Calendar.getInstance ().get (Calendar.YEAR);
                            int day = Calendar.getInstance ().get (Calendar.DAY_OF_MONTH);
                            int month = Calendar.getInstance ().get (Calendar.MONTH); // date picker conclude months from 0-11
                            datePickerDialog = new DatePickerDialog (this, listener, year, month, day);
                            datePickerDialog.show();
                            fromButton.setVisibility(View.GONE);
                            toButton.setVisibility(View.GONE);
                            dateFilterToSelected="";
                            dateFilterFromSelected="";
                       //  }// else {
                          //  IsCalendarOpened = false;
                   //     }
                        break;
                    case 6: // 18.10 assaf add dates range
                            dateFilterSelected = parent.getItemAtPosition(position).toString();
                            if (dateFilterFromSelected !="" && dateFilterToSelected!="")
                            {
                               GlobalVariables.CURRENT_DATE_FILTER = addDays(new Date(),3000); // save the same dates range when back to it
                            }
                            else {
                                GlobalVariables.CURRENT_DATE_FILTER = null;//cancel the dates range when sellecting other options
                            }
                            openRangeDatePickers();
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

    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener () {//Assaf:Date selected even without press the Done
          //Data picker appear when select calendar in filter
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                GlobalVariables.CURRENT_DATE_FILTER = calendar.getTime();
                Toast.makeText(getApplicationContext(),calendar.getTime().toString(),Toast.LENGTH_SHORT).show();
                saveInfo(); //24.09 added
        }
    };

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) { // click the grid and selct category for filter
        if (filter == null || filter == "") {//24.09 assaf fixed
            filter = num[i];
            filterNameToPresentInEventPage = Names[i];// main filter name to present
            GlobalVariables.CURRENT_FILTER_NAME = filter;
            view.setBackgroundColor (Color.RED);
            saveInfo ();

            //31.08 This Dialog open the SubFilter page - for now it was remarked...
            /*AlertDialog.Builder _builder = new AlertDialog.Builder (this);
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
            _alert.show ();*/

        } else {
            if (filter.equals(num[i])) { // cancel filter selection - reset mainFilter and subFilter
                filter = null;
                filterNameToPresentInEventPage = null;// main filter name to present
                GlobalVariables.CURRENT_FILTER_NAME = "";
                GlobalVariables.CURRENT_SUB_FILTER = "";
                subFilter = null;
                subFilterNameToPresentInEventPage = null;
                view.setBackgroundColor (Color.TRANSPARENT);
                saveInfo ();  // assaf added for open Intent to subcategory activity
            }
            else {
                Toast.makeText (this, R.string.can_choice_one_category, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Build.VERSION.SDK_INT > 5
                    && keyCode == KeyEvent.KEYCODE_BACK
                    && event.getRepeatCount () == 0) {
            onBackPressed ();
            return true;
        }

        return super.onKeyDown (keyCode, event);
    }




    private void saveInfo() { // save the filter info.
        _sharedPref = getSharedPreferences ("filterInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = _sharedPref.edit ();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");//assaf - 24.09
        editor.putString ("mainFilter", filterNameToPresentInEventPage);
        editor.putString("subFilter", subFilterNameToPresentInEventPage);
        editor.putString("date", dateFilterSelected);
        editor.putString("dateFrom",dateFilterFromSelected);
        editor.putString("dateTo",dateFilterToSelected);
        editor.putString("price", priceFilterSelected);
        editor.putString("mainFilterForFilter", filter); //24.09 assaf
        editor.putString("subFilterForFilter", GlobalVariables.CURRENT_SUB_FILTER); //24.09 assaf
        editor.putInt("priceFilterForFilter", GlobalVariables.CURRENT_PRICE_FILTER); //24.09 assaf
        if (GlobalVariables.CURRENT_DATE_FILTER!=null) {//24.09 assaf
            editor.putString("dateFilterForFilter", dateFormatter.format(GlobalVariables.CURRENT_DATE_FILTER));
        }
         else {
            editor.putString("dateFilterForFilter", null);
        }        ///
         editor.apply();
    }


    public String[] getData()
    // display the filter info.
    {
        _sharedPref = getSharedPreferences("filterInfo", Context.MODE_PRIVATE);
        String _filterName = _sharedPref.getString("mainFilter", "");
        String _date = _sharedPref.getString ("date", "");
        String _dateFrom = _sharedPref.getString ("dateFrom", "");
        String _dateTo =  _sharedPref.getString ("dateTo", "");
        String _price = _sharedPref.getString ("price", "");
        String _subFilter = _sharedPref.getString("subFilter","");
        String _mainFilterForFilter = _sharedPref.getString("mainFilterForFilter","");
        String _subFilterForFilter = _sharedPref.getString("subFilterForFilter","");
        Integer _priceForFilter = _sharedPref.getInt("priceFilterForFilter", -5);
        String _dateForFilter = _sharedPref.getString("dateFilterForFilter", "");

        String[] values = {_date, _price,_subFilter,_filterName,_mainFilterForFilter,_subFilterForFilter,Integer.toString(_priceForFilter),_dateForFilter,_dateFrom,_dateTo};

        return values;
    }

    private void openSubCategory() { // for open the Sub filter page. for now it was remarked
        Intent intent = new Intent (this, FilterPageActivity2.class);
        intent.putExtra ("mainFilter", filter);
        intent.putExtra ("date", dateFilterSelected);
        intent.putExtra("price", priceFilterSelected);

        startActivity(intent);
    }

    @Override
    protected void onResume() {

        //when back from subfilter setup activity or back from Events list
        super.onResume ();
        subFilter = GlobalVariables.CURRENT_SUB_FILTER;// otherwsie subfilter will be null/empty althigh no chnages done
        String[] results = getData();
        filter = results[4];//24.09 - assaf - to save the last filter slected by user
        GlobalVariables.CURRENT_FILTER_NAME = filter;////24.09 - assaf - to save the last filter selected by user
        filterNameToPresentInEventPage = results[3];//get the mainfilter set
        subFilterNameToPresentInEventPage = results[2];//get the subfilter set in subfilter activity
        if (!results[0].equals ("") || !results[1].equals ("")) {
          //  if (dateAdapter.getPosition (results[0]) == 5) // prevent open calendar automtically follwoing open in main filter
            //    IsCalendarOpened = true;
            dateSpinner.setSelection (dateAdapter.getPosition (results[0]));
            priceSpinner.setSelection (priceAdapter.getPosition (results[1]));
        }

        /** Create the Custom Grid View*/
        //24.09 - assaf changed to execute it in onresume
        FilterImageAdapter adapter = new FilterImageAdapter (FilterPageActivity.this, Names, Images,num);
        gridView = (GridView) findViewById (R.id.grdFilter);
        gridView.setAdapter (adapter);
        gridView.setOnItemClickListener (this);
    }

    public static Date addDays(Date date, int days)// return the date which is after or before few days from current day
    {
        Calendar cal = Calendar.getInstance ();
        cal.setTime(date);
        cal.add (Calendar.DATE, days);
        return cal.getTime ();
    }

    public static Date getCurrentWeekend() {// return the day after last day of week
        Date date = new Date ();
        Calendar calendar = Calendar.getInstance ();

        calendar.setTime (date);
        int dayOfWeek = calendar.get (Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek ();
        calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek);// back to start day of week
        //Date weekStart = calendar.getTime();
        calendar.add (Calendar.DAY_OF_MONTH, 7);// the day after last day of week
        Date weekEnd = calendar.getTime ();
        return weekEnd;
    }

    private void openRangeDatePickers(){ // 18.10 assaf add dates range

     fromButton.setVisibility(View.VISIBLE);
     toButton.setVisibility(View.VISIBLE);
     View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           if (v.getId() == fromButton.getId()) {

                int year = Calendar.getInstance ().get (Calendar.YEAR);
                int day = Calendar.getInstance ().get (Calendar.DAY_OF_MONTH);
                int month = Calendar.getInstance ().get (Calendar.MONTH); // date picker conclude months from 0-11
               datePickerDialog = new DatePickerDialog (v.getContext(), fromDatelistener, year, month, day);
               datePickerDialog.show ();

            } else if (v.getId() == toButton.getId()) {

                int year = Calendar.getInstance ().get (Calendar.YEAR);
                int day = Calendar.getInstance ().get (Calendar.DAY_OF_MONTH);
                int month = Calendar.getInstance ().get (Calendar.MONTH); // date picker conclude months from 0-11
                datePickerDialog = new DatePickerDialog (v.getContext(), toDatelistener, year, month, day);
                datePickerDialog.show ();
            }
        }
    };
        fromButton.setOnClickListener(onClickListener);
        toButton.setOnClickListener(onClickListener);

}

    DatePickerDialog.OnDateSetListener fromDatelistener = new DatePickerDialog.OnDateSetListener () {//18.10 asaf add dates range
        //Data picker appear when select calendar in filter
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Date currentDate = new Date ();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            GlobalVariables.CURRENT_DATE_FILTER = addDays(currentDate, 3000);//18.10 assaf -  Just to sign that this is Range Dates Filter
            dateFilterFromSelected = calendar.getTime().toString();
            Toast.makeText(getApplicationContext(),dateFilterFromSelected ,Toast.LENGTH_SHORT).show();
            saveInfo();
        }
    };

    DatePickerDialog.OnDateSetListener toDatelistener = new DatePickerDialog.OnDateSetListener () {//18.10 asaf add dates range
        //Data picker appear when select calendar in filter
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Date currentDate = new Date ();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            dateFilterToSelected= calendar.getTime().toString();
            GlobalVariables.CURRENT_DATE_FILTER = addDays(currentDate, 3000);
            Toast.makeText(getApplicationContext(),dateFilterToSelected,Toast.LENGTH_SHORT).show();//18.10 assaf -Just to sign that this is Range Dates Filter
            saveInfo();
        }
    };

    @Override
    public void onBackPressed() {
               // 20.10 Assaf: verify that From and To dates were set
        if (dateFilterFromSelected!="" && dateFilterToSelected=="" || dateFilterToSelected!="" && dateFilterFromSelected=="")
        {
          Toast.makeText(getApplicationContext(),getString(R.string.setfromto),Toast.LENGTH_SHORT).show();
        }
        else {
            this.finish();
        }

    }
}