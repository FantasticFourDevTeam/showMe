package com.example.FundigoApp.Customer.SavedEvents;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.FundigoApp.Customer.RealTime.RealTimeActivity;
import com.example.FundigoApp.Customer.Social.MyNotificationsActivity;
import com.example.FundigoApp.CustomerFragmentsMainActivity;
import com.example.FundigoApp.Events.CreateEventActivity;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.Events.EventsListAdapter;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.MyLocation.CityMenu;
import com.example.FundigoApp.R;
import com.example.FundigoApp.SearchActivity;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.EventDataMethods.GetEventsDataCallback;
import com.example.FundigoApp.StaticMethod.FilterMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods.GpsICallback;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavedEventActivity extends Fragment implements View.OnClickListener,
                                                                             AdapterView.OnItemClickListener,
                                                                             GetEventsDataCallback,
                                                                             GpsICallback,CustomerFragmentsMainActivity.ClickCaller {
    static ArrayList<EventInfo> filteredSavedEventsList = new ArrayList<> ();

    ListView list_view;
    static EventsListAdapter eventsListAdapter;
    Button eventTab;
    Button savedEvent;
    Button realTimeTab;
    private static PopupMenu popup;
    static Context context;
    ImageView search, notification;
    private static TextView pushViewText; //assaf: Text view for present the Push messages
    private static SharedPreferences _sharedPref;
    private static TextView filterTextView;
    ArrayList<String> _mainFilterForFilter = new ArrayList<>();//assaf - 02.12 assaf
    private Button mainCityFilterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_saved_event, container, false);
        context = this.getContext();

        list_view = (ListView) rootView.findViewById(R.id.listView);
        //eventTab = (Button) findViewById (R.id.BarEvent_button);
        //savedEvent = (Button) findViewById (R.id.BarSavedEvent_button);
        //realTimeTab = (Button) findViewById (R.id.BarRealTime_button);
        //notification = (ImageView) findViewById (R.id.notification_item);
        //notification.setOnClickListener (this);
        //search = (ImageView) findViewById (R.id.search);
        //search.setOnClickListener (this);
       // pushViewText = (TextView) rootView.findViewById (R.id.pushView); // canceled for now - 08/03
        filterTextView = (TextView) rootView.findViewById (R.id.filterView);
        //mainCityFilterButton = CustomerFragmentsMainActivity.GetMainButton();
        mainCityFilterButton = (Button)getActivity().findViewById(R.id.main_fragment_main_city_item);
        popup = new PopupMenu (this.getContext(), mainCityFilterButton);

        eventsListAdapter = new EventsListAdapter (this.getContext(), filteredSavedEventsList, true);
       // realTimeTab.setOnClickListener (this);
        //eventTab.setOnClickListener(this);
        //savedEvent.setOnClickListener(this);

        list_view.setAdapter(eventsListAdapter);
        list_view.setSelector(new ColorDrawable(Color.TRANSPARENT));
        list_view.setOnItemClickListener(this);

        if (GlobalVariables.ALL_EVENTS_DATA.size () == 0) {
            Intent intent = new Intent (this.getContext(), EventPageActivity.class);
            EventDataMethods.downloadEventsData (this, null, this.getContext(), intent);
        } else {
            inflateCityMenu();
            getSavedEventsFromJavaList ();
            if (GlobalVariables.MY_LOCATION == null) {
                GPSMethods.updateDeviceLocationGPS (this.getContext(), this);
            }

            EventDataMethods.RemoveExpiredAndCanceledEvents(filteredSavedEventsList);//22.01 - Assaf remove cnacled or expired events form the List of events
            eventsListAdapter.notifyDataSetChanged();
        }
        GlobalVariables.SAVED_ACTIVITY_RUNNING = true;

        return rootView;
    }

    @Override
    public void onResume() {

         super.onResume();

         displayFilterBanner();

        if (GlobalVariables.ALL_EVENTS_DATA.size () != 0) {
            if (GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                ArrayList<EventInfo> tempEventsListFiltered =
                        FilterMethods.filterByCityAndFilterName (
                                                                        GlobalVariables.namesCity[GlobalVariables.indexCityChosen],
                                                                        GlobalVariables.CURRENT_FILTER_NAME,
                                                                        GlobalVariables.CURRENT_SUB_FILTER,
                                                                        GlobalVariables.CURRENT_DATE_FILTER,
                                                                        GlobalVariables.CURRENT_PRICE_FILTER,
                                                                        GlobalVariables.ALL_EVENTS_DATA);
                filteredSavedEventsList.clear ();
                filteredSavedEventsList.addAll (getSavedEventsFromList (tempEventsListFiltered));
                eventsListAdapter.notifyDataSetChanged();
                if (GlobalVariables.CITY_GPS != null &&
                            !GlobalVariables.CITY_GPS.isEmpty () &&
                            GlobalVariables.namesCity[GlobalVariables.indexCityChosen].equals (GlobalVariables.CITY_GPS) &&
                            GPSMethods.getCityIndexFromName (GlobalVariables.CITY_GPS) >= 0) {
                    mainCityFilterButton.setText (GlobalVariables.namesCity[GlobalVariables.indexCityChosen] + "(GPS)");
                } else {
                    mainCityFilterButton.setText (GlobalVariables.namesCity[GlobalVariables.indexCityChosen]);
                }
            } else if (GlobalVariables.CITY_GPS != null &&
                               !GlobalVariables.CITY_GPS.isEmpty () &&
                               GPSMethods.getCityIndexFromName (GlobalVariables.CITY_GPS) >= 0) {
                ArrayList<EventInfo> tempEventsListFiltered =
                        FilterMethods.filterByCityAndFilterName (
                                                                        GlobalVariables.CITY_GPS,
                                                                        GlobalVariables.CURRENT_FILTER_NAME,
                                                                        GlobalVariables.CURRENT_SUB_FILTER,
                                                                        GlobalVariables.CURRENT_DATE_FILTER,
                                                                        GlobalVariables.CURRENT_PRICE_FILTER,
                                                                        GlobalVariables.ALL_EVENTS_DATA);
                filteredSavedEventsList.clear ();
                filteredSavedEventsList.addAll (getSavedEventsFromList (tempEventsListFiltered));
                eventsListAdapter.notifyDataSetChanged ();
                mainCityFilterButton.setText (GlobalVariables.CITY_GPS + "(GPS)");
            } else {
                List<EventInfo> tempEventsList = new ArrayList<> ();
                for (int i = 0; i < GlobalVariables.ALL_EVENTS_DATA.size (); i++) {
                    if (GlobalVariables.ALL_EVENTS_DATA.get (i).getIsSaved ()) {
                        tempEventsList.add (GlobalVariables.ALL_EVENTS_DATA.get (i));
                    }
                }
                filteredSavedEventsList.clear ();
                if (GlobalVariables.CURRENT_FILTER_NAME != null) {
                    tempEventsList = FilterMethods.filterByFilterName (GlobalVariables.CURRENT_FILTER_NAME,
                                                                              GlobalVariables.CURRENT_SUB_FILTER,
                                                                              GlobalVariables.CURRENT_DATE_FILTER,
                                                                              GlobalVariables.CURRENT_PRICE_FILTER,
                                                                              tempEventsList);
                }
                filteredSavedEventsList.addAll (getSavedEventsFromList (tempEventsList));
                eventsListAdapter.notifyDataSetChanged ();
            }
        }
        EventDataMethods.RemoveExpiredAndCanceledEvents(filteredSavedEventsList);//22.01 - Assaf remove cnacled or expired events form the List of events
        eventsListAdapter.notifyDataSetChanged();

    }

    @Override
    public void gpsCallback() {

        ArrayList<EventInfo> tempEventsListForCityNameFilter = new ArrayList<>();

        if (GlobalVariables.CITY_GPS != null && !GlobalVariables.CITY_GPS.isEmpty()&&GlobalVariables.MY_LOCATION!=null&& this.getContext()!=null) {

            tempEventsListForCityNameFilter.clear();
            tempEventsListForCityNameFilter.addAll(GlobalVariables.ALL_EVENTS_DATA);
            EventDataMethods.RemoveExpiredAndCanceledEvents(tempEventsListForCityNameFilter);// - Assaf remove canceled or expired events cities form the city menu list
            GlobalVariables.cityMenuInstance = new CityMenu(tempEventsListForCityNameFilter, this.getContext());
            //GlobalVariables.cityMenuInstance = new CityMenu(GlobalVariables.ALL_EVENTS_DATA, this.getContext());
            GlobalVariables.namesCity = GlobalVariables.cityMenuInstance.getCityNames();
            inflateCityMenu();
            int indexCityGps = GPSMethods.getCityIndexFromName(GlobalVariables.CITY_GPS);
            if (indexCityGps >= 0) {
                //popup.getMenu ().getItem (GlobalVariables.indexCityGPS).setTitle (GlobalVariables.namesCity[GlobalVariables.indexCityGPS]);
                GlobalVariables.indexCityGPS = indexCityGps;
                popup.getMenu().getItem(GlobalVariables.indexCityGPS).setTitle(GlobalVariables.CITY_GPS + "(GPS)");
                if (!GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                    ArrayList<EventInfo> tempEventsListFiltered =
                            FilterMethods.filterByCityAndFilterName(
                                    GlobalVariables.CITY_GPS,
                                    GlobalVariables.CURRENT_FILTER_NAME,
                                    GlobalVariables.CURRENT_SUB_FILTER,
                                    GlobalVariables.CURRENT_DATE_FILTER,
                                    GlobalVariables.CURRENT_PRICE_FILTER,
                                    GlobalVariables.ALL_EVENTS_DATA);
                    filteredSavedEventsList.clear();
                    filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsListFiltered));
                    eventsListAdapter.notifyDataSetChanged();
                    mainCityFilterButton.setText(GlobalVariables.CITY_GPS + "(GPS)");
                }
            } else { // ASSAF :08/09 to support a case that GPS location updated but not found in the list of Cities
                if (!GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                    ArrayList<EventInfo> tempEventsList =
                            FilterMethods.filterByCityAndFilterName(
                                    GlobalVariables.namesCity[GlobalVariables.indexCityChosen],
                                    GlobalVariables.CURRENT_FILTER_NAME,
                                    GlobalVariables.CURRENT_SUB_FILTER,
                                    GlobalVariables.CURRENT_DATE_FILTER,
                                    GlobalVariables.CURRENT_PRICE_FILTER,
                                    GlobalVariables.ALL_EVENTS_DATA);
                    filteredSavedEventsList.clear();
                    filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsList));
                    eventsListAdapter.notifyDataSetChanged();
                }
            }
        }

        if (GlobalVariables.USER_CHOSEN_CITY_MANUALLY) { // 08/09 Assaf- case of manual selection
            ArrayList<EventInfo> tempEventsList =
                    FilterMethods.filterByCityAndFilterName(
                            GlobalVariables.namesCity[GlobalVariables.indexCityChosen],
                            GlobalVariables.CURRENT_FILTER_NAME,
                            GlobalVariables.CURRENT_SUB_FILTER,
                            GlobalVariables.CURRENT_DATE_FILTER,
                            GlobalVariables.CURRENT_PRICE_FILTER,
                            GlobalVariables.ALL_EVENTS_DATA);
            filteredSavedEventsList.clear();
            filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsList));
            eventsListAdapter.notifyDataSetChanged();
            if (GlobalVariables.CITY_GPS != null &&
                    GlobalVariables.namesCity[GlobalVariables.indexCityChosen].equals(GlobalVariables.CITY_GPS) &&
                    GPSMethods.getCityIndexFromName(GlobalVariables.CITY_GPS) >= 0) {
                mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen] + "(GPS)");
            } else {
                mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen]);
            }
        }

        EventDataMethods.RemoveExpiredAndCanceledEvents(filteredSavedEventsList);//22.01 - Assaf remove cnacled or expired events form the List of events
        eventsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void eventDataCallback() {
        inflateCityMenu();
        getSavedEventsFromJavaList();
        if (GlobalVariables.MY_LOCATION == null) {
            GPSMethods.updateDeviceLocationGPS(this.getContext(), this);
        }
        EventDataMethods.RemoveExpiredAndCanceledEvents(filteredSavedEventsList);//22.01 - Assaf remove cnacled or expired events form the List of events
        eventsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void GetMenuCityButtonClick(){
        inflateCityMenu();
        popup.show();
    }

    public void getSavedEventsFromJavaList() {
        try {
            List<EventInfo> tempEventsList = new ArrayList<>();
            for (int i = 0; i < GlobalVariables.ALL_EVENTS_DATA.size(); i++) {
                if (GlobalVariables.ALL_EVENTS_DATA.get(i).getIsSaved()) {
                    tempEventsList.add(GlobalVariables.ALL_EVENTS_DATA.get(i));
                }
            }
            if (GlobalVariables.CURRENT_FILTER_NAME != null) {
                tempEventsList = FilterMethods.filterByFilterName(GlobalVariables.CURRENT_FILTER_NAME,
                        GlobalVariables.CURRENT_SUB_FILTER,
                        GlobalVariables.CURRENT_DATE_FILTER,
                        GlobalVariables.CURRENT_PRICE_FILTER,
                        tempEventsList);
            }
            filteredSavedEventsList.clear();
            filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsList));
            eventsListAdapter.notifyDataSetChanged();
            if (GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                ArrayList<EventInfo> tempEventsListFiltered =
                        FilterMethods.filterByCityAndFilterName(
                                GlobalVariables.namesCity[GlobalVariables.indexCityChosen],
                                GlobalVariables.CURRENT_FILTER_NAME,
                                GlobalVariables.CURRENT_SUB_FILTER,
                                GlobalVariables.CURRENT_DATE_FILTER,
                                GlobalVariables.CURRENT_PRICE_FILTER,
                                GlobalVariables.ALL_EVENTS_DATA);
                filteredSavedEventsList.clear();
                filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsListFiltered));
                eventsListAdapter.notifyDataSetChanged();
                if (GlobalVariables.CITY_GPS != null &&
                        GlobalVariables.namesCity[GlobalVariables.indexCityChosen].equals(GlobalVariables.CITY_GPS) &&
                        GPSMethods.getCityIndexFromName(GlobalVariables.CITY_GPS) >= 0) {
                    mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen] + "(GPS)");
                } else {
                    mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen]);
                }
            } else if (GlobalVariables.CITY_GPS != null &&
                    !GlobalVariables.CITY_GPS.isEmpty() &&
                    GPSMethods.getCityIndexFromName(GlobalVariables.CITY_GPS) >= 0) {
                ArrayList<EventInfo> tempEventsListFiltered =
                        FilterMethods.filterByCityAndFilterName(
                                GlobalVariables.CITY_GPS,
                                GlobalVariables.CURRENT_FILTER_NAME,
                                GlobalVariables.CURRENT_SUB_FILTER,
                                GlobalVariables.CURRENT_DATE_FILTER,
                                GlobalVariables.CURRENT_PRICE_FILTER,
                                GlobalVariables.ALL_EVENTS_DATA);
                filteredSavedEventsList.clear();
                filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsListFiltered));
                eventsListAdapter.notifyDataSetChanged();
                mainCityFilterButton.setText(GlobalVariables.CITY_GPS + "(GPS)");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void inflateCityMenu() {
        popup = new PopupMenu(this.getContext(), mainCityFilterButton);//Assaf
        popup.getMenuInflater ().inflate(R.menu.popup_city, popup.getMenu());

        if (GlobalVariables.namesCity.length == 0) {
            loadCityNamesToPopUp();
        } else {
            loadCityNamesToPopUp ();
        }
     //   mainCityFilterButton.setOnClickListener (new View.OnClickListener () {
       //     @Override
       //     public void onClick(View v) {
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        GlobalVariables.indexCityChosen = GlobalVariables.popUpIDToCityIndex.get(item.getItemId());
                        GlobalVariables.CURRENT_CITY_NAME = GlobalVariables.namesCity[GlobalVariables.indexCityChosen];

                        if (GlobalVariables.CITY_GPS != null &&
                                GlobalVariables.namesCity[GlobalVariables.indexCityChosen].equals(GlobalVariables.CITY_GPS) &&
                                GPSMethods.getCityIndexFromName(GlobalVariables.CITY_GPS) >= 0) {
                            mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen] + "(GPS)");

                        } else {
                            mainCityFilterButton.setText(GlobalVariables.namesCity[GlobalVariables.indexCityChosen]);

                        }
                        ArrayList<EventInfo> tempEventsListFiltered =
                                FilterMethods.filterByCityAndFilterName(
                                        GlobalVariables.namesCity[GlobalVariables.indexCityChosen],
                                        GlobalVariables.CURRENT_FILTER_NAME,
                                        GlobalVariables.CURRENT_SUB_FILTER,
                                        GlobalVariables.CURRENT_DATE_FILTER,
                                        GlobalVariables.CURRENT_PRICE_FILTER,
                                        GlobalVariables.ALL_EVENTS_DATA);
                        filteredSavedEventsList.clear();
                        filteredSavedEventsList.addAll(getSavedEventsFromList(tempEventsListFiltered));

                        EventDataMethods.RemoveExpiredAndCanceledEvents(filteredSavedEventsList);//22.01 - Assaf remove cnacled or expired events form the List of events
                        eventsListAdapter.notifyDataSetChanged();
                        GlobalVariables.USER_CHOSEN_CITY_MANUALLY = true;
                        return true;
                    }
                });
               // popup.show ();//showing popup menu
            }
       // });
   // }

    private void loadCityNamesToPopUp() {
        try {
            boolean foundCity = true;
            if (!GlobalVariables.CURRENT_CITY_NAME.isEmpty ()) {
                foundCity = false;
            }
            for (int i = 0; i < GlobalVariables.namesCity.length; i++) {
                if (i == GlobalVariables.indexCityGPS &&
                            GlobalVariables.CITY_GPS != null &&
                            !GlobalVariables.CITY_GPS.isEmpty () &&
                            GPSMethods.getCityIndexFromName (GlobalVariables.CITY_GPS) >= 0) {
                    popup.getMenu().add(Menu.NONE, i, Menu.NONE, GlobalVariables.namesCity[i] + "(GPS)"); // create menu items dynamically
                } else {
                    popup.getMenu().add(Menu.NONE, i, Menu.NONE, GlobalVariables.namesCity[i]); // create menu items dynamically
                }
                GlobalVariables.popUpIDToCityIndex.put (popup.getMenu ().getItem (i).getItemId (), i);
                if (!GlobalVariables.CURRENT_CITY_NAME.isEmpty () &&
                            GlobalVariables.CURRENT_CITY_NAME.equals (GlobalVariables.namesCity[i])) {
                    GlobalVariables.indexCityChosen = i;
                    foundCity = true;
                }
            }
            if (!foundCity) {
                GlobalVariables.CURRENT_CITY_NAME = "";
                GlobalVariables.indexCityChosen = 0;
            }
            if (GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                mainCityFilterButton.setText (popup.getMenu ().getItem (GlobalVariables.indexCityChosen).getTitle ());
            } else if (GlobalVariables.CITY_GPS != null &&
                               !GlobalVariables.CITY_GPS.isEmpty () &&
                               GPSMethods.getCityIndexFromName (GlobalVariables.CITY_GPS) >= 0) {
                mainCityFilterButton.setText (GlobalVariables.CITY_GPS + "(GPS)");
            } else {
                mainCityFilterButton.setText (popup.getMenu ().getItem (0).getTitle ());
            }
            if (GlobalVariables.namesCity.length < 10)
            //in case number of cities is smaller then 10. remove Menu items
            {
                onPrepareOptionsMenu (popup.getMenu ());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void onPrepareOptionsMenu(Menu menu) { //Assaf- Hide the Items in Menu XML which are empty since the length of menu is less then 11
        try {
            super.onPrepareOptionsMenu (menu);
            int maxLength = 11;
            int numOfItemsToRemove = maxLength - GlobalVariables.namesCity.length;
            while (numOfItemsToRemove > 0) {
                menu.getItem (maxLength - 1).setVisible (false);
                numOfItemsToRemove--;
                maxLength--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       // return true;
    }

    @Override
    public void onClick(View v) {
        Intent newIntent = null;
        if (v.getId () == eventTab.getId ()) {
            getActivity().finish();
        } else if (v.getId () == realTimeTab.getId ()) {
            newIntent = new Intent (this.getContext(), RealTimeActivity.class);
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

    //passed to MainFragmner - 01.03 Assaf
//    public void openFilterPage(View v) {
//        Intent filterPageIntent = new Intent (this.getContext(), FilterPageActivity.class);
//        startActivity (filterPageIntent);
//    }
//
//    public void openMenuPage(View v) {
//        Intent menuPageIntent = new Intent (this.getContext(), MenuActivity.class);
//        startActivity (menuPageIntent);
//    }

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) {
        Bundle b = new Bundle ();
        Intent intent = new Intent (this.getContext(), EventPageActivity.class);
        EventDataMethods.onEventItemClick (i, filteredSavedEventsList, intent);
        intent.putExtras (b);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        GeneralStaticMethods.onActivityResult(requestCode,
                data,
                this.getActivity());
    }

    public void createEvent(View view) {
        Intent intent = new Intent (this.getContext(), CreateEventActivity.class);
        startActivity(intent);
    }

    List<EventInfo> getSavedEventsFromList(List<EventInfo> eventInfoList) {
        ArrayList<EventInfo> tempEventsList = new ArrayList<> ();
        for (int i = 0; i < eventInfoList.size (); i++) {
            if (eventInfoList.get (i).getIsSaved ()) {
                tempEventsList.add (eventInfoList.get (i));
            }
        }
        return tempEventsList;
    }

   /* public void setTextToView(String str) {
        if (pushViewText.equals (null))
            pushViewText = (TextView) getView().findViewById(R.id.pushView);

        pushViewText.setText (str);//Assaf added: set Push notification text to the Textview by MainActivity
    } */

    private String[] getData()
    // display the filter info selected by the user.
    {
        _sharedPref = this.getActivity().getSharedPreferences ("filterInfo", Context.MODE_PRIVATE);
        String _date = _sharedPref.getString ("date", "");
        String _price = _sharedPref.getString ("price", "");
        String _mainfilter = _sharedPref.getString("mainFilter", "");
        String _subfilter = _sharedPref.getString("subFilter", "");
        String _dateFrom = _sharedPref.getString("dateFrom", ""); //18.10 assaf
        String _dateTo =  _sharedPref.getString ("dateTo", "");//18.10 - assaf

        String[] values = {_mainfilter, _subfilter, _date, _price,_dateFrom,_dateTo};

        return values;
    }

    private void displayFilterBanner() {

        String[] results = getData(); // display the filter line
        String[] values = getResources().getStringArray(R.array.eventPriceFilter);// get values from resource
        String[] dateValues = getResources().getStringArray(R.array.eventDateFilter);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM d yyyy", Locale.getDefault());
        String toFromDates="";

        try {
            if (!results[0].equals("") || !results[1].equals("") || !results[2].equals("") || !results[3].equals("")) {
                for (int i = 0; i < results.length; i++) {
                    if (results[i].equals(values[0])) //if the result is "No Filter" , we remove it from present it in the filter view
                    {
                        results[i] = "";
                    }
                    if (results[i].equals(dateValues[5]) && GlobalVariables.CURRENT_DATE_FILTER != null) {
                        results[i] = dateFormatter.format(GlobalVariables.CURRENT_DATE_FILTER);// if Select from calendar then presnent real Date
                    } else if (results[i].equals(dateValues[5]) || results[i].equals(dateValues[6]) && GlobalVariables.CURRENT_DATE_FILTER == null) {
                        results[i] = ""; // if select from calendar filter selected but a date not set in Date picker
                    } else if (results[i].equals(dateValues[6]) && GlobalVariables.CURRENT_DATE_FILTER != null) //19.10 assaf :selected dates range
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

                 filterTextView.setVisibility(View.VISIBLE);
                //filterTextView.setText(results[0] + " " + results[1] + " " + results[2] + " " + results[3] + " " + toFromDates);
                //Main and sub filter prestig was canceled - assaf 04/12
                 filterTextView.setText("" + " " + " " + "" + results[2] + " " + results[3] + " " + toFromDates);


            }
        } catch (Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) { // refresh the fragment When swipe to it
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            mainCityFilterButton.setVisibility(View.VISIBLE);
            Button mainCityCurrentLocationButton = (Button) getActivity().findViewById(R.id.main_fragment_main_current_location);
            mainCityCurrentLocationButton.setVisibility(View.GONE);
            if (getView()!=null)
            {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment _fragment = new SavedEventActivity();
                fragmentTransaction.replace(R.id.savedEventFragment,_fragment);
                fragmentTransaction.commit();
            }
        }
    }

}
