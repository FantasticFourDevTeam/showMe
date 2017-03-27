package com.example.FundigoApp.Customer.RealTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.Customer.SavedEvents.SavedEventActivity;
import com.example.FundigoApp.Customer.Social.MyNotificationsActivity;
import com.example.FundigoApp.CustomerFragmentsMainActivity;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.SearchActivity;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.EventDataMethods.GetEventsDataCallback;
import com.example.FundigoApp.StaticMethod.FilterMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods.GpsICallback;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RealTimeActivity extends Fragment implements View.OnClickListener,
                                                                           AdapterView.OnItemClickListener,
                                                                           GetEventsDataCallback,
                                                                           GpsICallback,CustomerFragmentsMainActivity.ClickCaller{

    private GridView gridView;
    private Button Event, RealTime, SavedEvent;
    private TextView turnOnGPS;
    private static List<EventInfo> events_sorted_by_dist_data = new ArrayList<EventInfo> ();
    private static List<EventInfo> events_data_filtered = new ArrayList<EventInfo> ();
    EventsGridAdapter eventsGridAdapter;
    ImageView search, notification;
    private static TextView pushViewText; //assaf: Text view for present the Push messages
    private static SharedPreferences _sharedPref;
    private static TextView filterTextView;
    private static Button mainCityFilterButton;
    private static Button mainFragmentCityFilterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_real_time, container, false);


        Log.e("oncreateview" ,"realtimeevents");

        //mainCityFilterButton = CustomerFragmentsMainActivity.GetMainButton();
        mainFragmentCityFilterButton = (Button)getActivity().findViewById(R.id.main_fragment_main_city_item);// 08.10 added
        mainCityFilterButton = (Button) getActivity().findViewById(R.id.main_fragment_main_current_location);
       // Event = (Button) findViewById (R.id.BarEvent_button);
       // RealTime = (Button) findViewById (R.id.BarRealTime_button);
       // SavedEvent = (Button) findViewById (R.id.BarSavedEvent_button);
        turnOnGPS = (TextView) rootView.findViewById (R.id.turnOnGps);
        //Event.setOnClickListener (this);
       // SavedEvent.setOnClickListener (this);
        //RealTime.setOnClickListener (this);
        //notification = (ImageView) findViewById (R.id.notification_item);
        //notification.setOnClickListener (this);
        ///search = (ImageView) findViewById (R.id.search);
        //search.setOnClickListener (this);
        //pushViewText = (TextView) rootView.findViewById(R.id.pushView); // cacnedled for now 08.03
        filterTextView = (TextView) rootView.findViewById(R.id.filterView);
        //RealTime.setTextColor (Color.WHITE);
        if (GlobalVariables.MY_LOCATION == null || !GPSMethods.isLocationEnabled (this.getContext())) {
            turnOnGps();
        }
        if (GlobalVariables.ALL_EVENTS_DATA.size () == 0) {
            Intent intent = new Intent (this.getContext(), EventPageActivity.class);
            EventDataMethods.downloadEventsData (this, null, this.getContext(), intent);
        } else {
            if (GlobalVariables.MY_LOCATION != null && GPSMethods.isLocationEnabled (this.getContext())) {
                events_sorted_by_dist_data = getSortedListByDist ();
                List<EventInfo> tempFilteredList =
                        FilterMethods.filterByFilterName (GlobalVariables.CURRENT_FILTER_NAME,
                                                                 GlobalVariables.CURRENT_SUB_FILTER, GlobalVariables.CURRENT_DATE_FILTER, GlobalVariables.CURRENT_PRICE_FILTER, events_sorted_by_dist_data);
                events_data_filtered.clear ();
                events_data_filtered.addAll (tempFilteredList);
            } //else {
               // GPSMethods.updateDeviceLocationGPS(this.getContext(), this);
            //}
        }
        if (GlobalVariables.MY_LOCATION == null) {
            GPSMethods.updateDeviceLocationGPS(this.getContext(), this);
        }
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        eventsGridAdapter = new EventsGridAdapter (this.getContext(), events_data_filtered);
        gridView.setAdapter(eventsGridAdapter);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemClickListener(this);

         return rootView;
    }

    private void turnOnGps() {
        turnOnGPS.setVisibility (View.VISIBLE);
    }

    @Override
    public void GetMenuCityButtonClick(){
        //do nothing
    }

    @Override
    public void gpsCallback() {

        if (GlobalVariables.ALL_EVENTS_DATA.size () > 0) {
            events_sorted_by_dist_data.clear();
            events_sorted_by_dist_data = getSortedListByDist ();
            List<EventInfo> tempFilteredList =
                    FilterMethods.filterByFilterName (GlobalVariables.CURRENT_FILTER_NAME,
                                                             GlobalVariables.CURRENT_SUB_FILTER,
                                                             GlobalVariables.CURRENT_DATE_FILTER,
                                                             GlobalVariables.CURRENT_PRICE_FILTER,
                                                             events_sorted_by_dist_data);
            events_data_filtered.clear();
            events_data_filtered.addAll(tempFilteredList);
            EventDataMethods.RemoveExpiredAndCanceledEvents(events_data_filtered);
            eventsGridAdapter.notifyDataSetChanged(); // 22.01 - support remove canceled and expired events form list
            eventsGridAdapter.notifyDataSetChanged();
        }

        turnOnGPS.setVisibility(View.GONE);
    }

    @Override
    public void eventDataCallback() {
        if (GlobalVariables.MY_LOCATION != null) {
            events_sorted_by_dist_data = getSortedListByDist ();
            List<EventInfo> tempFilteredList =
                    FilterMethods.filterByFilterName (GlobalVariables.CURRENT_FILTER_NAME,
                                                             GlobalVariables.CURRENT_SUB_FILTER,
                                                             GlobalVariables.CURRENT_DATE_FILTER,
                                                             GlobalVariables.CURRENT_PRICE_FILTER,
                                                             events_sorted_by_dist_data);
            events_data_filtered.clear ();
            events_data_filtered.addAll (tempFilteredList);
            eventsGridAdapter.notifyDataSetChanged ();
            turnOnGPS.setVisibility (View.GONE);
        } else {
            GPSMethods.updateDeviceLocationGPS(this.getContext(), this);
        }

        EventDataMethods.RemoveExpiredAndCanceledEvents(events_data_filtered);
        eventsGridAdapter.notifyDataSetChanged(); // 22.01 - support remove canceled and expired events form list
    }

    public List<EventInfo> getSortedListByDist() {
        List<EventInfo> arr = new ArrayList<> ();
        List<EventInfo> all_events_list = GlobalVariables.ALL_EVENTS_DATA;
        for (int i = 0; i < all_events_list.size (); i++) {
            EventInfo event = all_events_list.get (i);
            double latitude = event.getX ();
            double longitude = event.getY ();
            Location locationEvent = new Location ("eventPlace");
            locationEvent.setLatitude (latitude);
            locationEvent.setLongitude (longitude);
            double distance = (double) GlobalVariables.MY_LOCATION.distanceTo (locationEvent) / 1000;
            DecimalFormat df = new DecimalFormat ("#.##");
            String dx = df.format (distance);
            distance = Double.valueOf (dx);
            event.setDist (distance);
            arr.add (event);
        }
        Collections.sort(arr, new Comparator<EventInfo>() {
            @Override
            public int compare(EventInfo a, EventInfo b) {
                if (a.getDist() < b.getDist()) return -1;
                if (a.getDist() >= b.getDist()) return 1;
                return 0;
            }
        });

        return arr;
    }

   @Override
    public void onClick(View v) {
        int vId = v.getId ();
        Intent newIntent = null;
        if (vId == Event.getId ()) {
           getActivity().finish();
        } else if (vId == SavedEvent.getId ()) {
            newIntent = new Intent (this.getContext(), SavedEventActivity.class);
            startActivity(newIntent);
            getActivity().finish();
        } else if (v.getId () == search.getId ()) {
            newIntent = new Intent (this.getContext(), SearchActivity.class);
            startActivity (newIntent);
        } else if (v.getId () == notification.getId ()) {
            newIntent = new Intent (this.getContext(), MyNotificationsActivity.class);
            startActivity (newIntent);
        }
    }

   /* public void openFilterPage(View v) {
        Intent filterPageIntent = new Intent (this, FilterPageActivity.class);
        startActivity (filterPageIntent);
    }*/

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) {
        Bundle b = new Bundle ();
        Intent intent = new Intent (this.getContext(), EventPageActivity.class);
        EventDataMethods.onEventItemClick(i, events_data_filtered, intent);
        intent.putExtras(b);
        startActivity(intent);
    }

   /* public void openMenuPage(View v) {
        Intent menuPageIntent = new Intent (this, MenuActivity.class);
        startActivity (menuPageIntent);
    }

    public void openNotificationPage(View v) {
        Intent i = new Intent (this, MyNotificationsActivity.class);
        startActivity (i);
    }

    public void openSearch(View v) {
        Intent i = new Intent (this, SearchActivity.class);
        startActivity (i);
    }*/


    @Override
    public void onResume() {
        super.onResume();

        Log.e("onresune", "onResumeRealTime");

        List<EventInfo> tempFilteredList =
                FilterMethods.filterByFilterName (GlobalVariables.CURRENT_FILTER_NAME,
                                                         GlobalVariables.CURRENT_SUB_FILTER,
                                                         GlobalVariables.CURRENT_DATE_FILTER,
                                                         GlobalVariables.CURRENT_PRICE_FILTER,
                                                         events_sorted_by_dist_data);
        events_data_filtered.clear();
        events_data_filtered.addAll(tempFilteredList);
       // if (GlobalVariables.MY_LOCATION != null && GPSMethods.isLocationEnabled (this))
        displayFilterBanner (); // to display filter selected by user
        EventDataMethods.RemoveExpiredAndCanceledEvents(events_data_filtered);
        eventsGridAdapter.notifyDataSetChanged(); // 22.01 - support remove canceled and expired events form list
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        GeneralStaticMethods.onActivityResult(requestCode,
                data,
                this.getActivity());
    }

 /*   public void setTextToView(String str) {
        if (pushViewText.equals (null))
            pushViewText = (TextView) getView().findViewById(R.id.pushView);

        pushViewText.setText(str);//Assaf added: set Push notification text to the Textview by MainActivity
    } */

    private String[] getData()
    // display the filter info selected by the user.
    {
        _sharedPref = this.getActivity().getSharedPreferences ("filterInfo", Context.MODE_PRIVATE);
        String _date = _sharedPref.getString ("date", "");
        String _price = _sharedPref.getString ("price", "");
        String _mainfilter = _sharedPref.getString ("mainFilter", "");
        String _subfilter = _sharedPref.getString ("subFilter", "");
        String _dateFrom = _sharedPref.getString ("dateFrom", ""); //18.10 assaf
        String _dateTo =  _sharedPref.getString ("dateTo", "");//18.10 - assaf

        String[] values = {_mainfilter, _subfilter, _date, _price,_dateFrom,_dateTo};

        return values;
    }

    private void displayFilterBanner() {
        try {
            String[] results = getData (); // display the filter line
            String[] values = getResources ().getStringArray(R.array.eventPriceFilter);
            String[] dateValues = getResources().getStringArray(R.array.eventDateFilter);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM d yyyy", Locale.getDefault());
            String toFromDates="";

            if (!results[0].equals ("") || !results[1].equals ("") || !results[2].equals ("") || !results[3].equals ("")) {
                for (int i = 0; i < results.length; i++) {
                    if (results[i].equals (values[0])) //if the result is "No Filter" , we remove it from presemtig it in the filter view
                    {
                        results[i] = "";
                    }
                    if(results[i].equals(dateValues[5])&&GlobalVariables.CURRENT_DATE_FILTER!=null)
                    {
                         results[i] = dateFormatter.format(GlobalVariables.CURRENT_DATE_FILTER);// if Select from calendar then present real Date
                    }
                    else if(results[i].equals(dateValues[5])|| results[i].equals(dateValues[6]) &&GlobalVariables.CURRENT_DATE_FILTER==null)
                    {
                        results[i] =""; // if select from calendar filter selected but a date not set in Date picker
                    }

                    else if (results[i].equals(dateValues[6])&& GlobalVariables.CURRENT_DATE_FILTER!=null) //19.10 assaf :selected dates range
                    {
                        results[2] = ""; //remove the test "Select Date
                        if (results[4] != "") {
                            toFromDates = " " + results[4].substring(0, 10);
                            if (results[5] != "") {
                                toFromDates = toFromDates + " " + " - " + " " + results[5].substring(0, 10);
                            }
                        }
                    }
                }
                filterTextView.setVisibility (View.VISIBLE);

                // Main filter and Sub filter not pesented in Bar - for now it was cnacled - Assaf 04.12
                //filterTextView.setText (results[0] + " " + results[1] + " " + results[2] + " " + results[3] + " " + toFromDates);
                filterTextView.setText (""+ " " + "" + " " + results[2] + " " + results[3] + " " + toFromDates);

            }
        } catch (Exception ex) {
            Log.e ("TAG", ex.getMessage ());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) { // refresh the fragment When swipe to it
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            mainFragmentCityFilterButton.setVisibility(View.GONE);
            mainCityFilterButton.setVisibility(View.VISIBLE);
            if (getView()!=null) {
                if (GlobalVariables.MY_LOCATION != null)

                    mainCityFilterButton.setText(getString(R.string.fundigo) + "(GPS)");
              }
       }
    }
}
