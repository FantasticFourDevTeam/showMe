package com.example.FundigoApp.StaticMethod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.FundigoApp.Events.Event;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.HttpHandler;
import com.example.FundigoApp.Events.Result;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.MyLocation.CityMenu;
import com.example.FundigoApp.Producer.Artists.Artist;
import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventDataMethods {

    public static void onEventItemClick(int positionViewItem,
                                        List<EventInfo> eventsList,
                                        Intent intent) {
        intent.putExtra("eventDate", eventsList.get(positionViewItem).getDate());
        intent.putExtra("eventName", eventsList.get(positionViewItem).getName());
        intent.putExtra("eventTags", eventsList.get(positionViewItem).getTags());
        intent.putExtra("eventPrice", eventsList.get(positionViewItem).getPrice());
        intent.putExtra("eventInfo", eventsList.get(positionViewItem).getInfo());
        intent.putExtra("eventPlace", eventsList.get(positionViewItem).getPlace());
        intent.putExtra("toilet", eventsList.get(positionViewItem).getToilet());
        intent.putExtra("parking", eventsList.get(positionViewItem).getParking());
        intent.putExtra("capacity", eventsList.get(positionViewItem).getCapacity());
        intent.putExtra("atm", eventsList.get(positionViewItem).getAtm());
        intent.putExtra("index", eventsList.get(positionViewItem).getIndexInFullList());
        intent.putExtra("i", String.valueOf(positionViewItem));
        intent.putExtra("artist", eventsList.get(positionViewItem).getArtist());
        intent.putExtra("fbUrl", eventsList.get(positionViewItem).getFbUrl());
    }

    public interface GetEventsDataCallback {
        void eventDataCallback();
    }

    ///////Assaf:not in background Method - no change was done
    public static void downloadEventsData(final GetEventsDataCallback ic,
                                          String producerId,
                                          final Context context,
                                          final Intent intent) {
        final ArrayList<EventInfo> tempEventsList = new ArrayList<>();
        Boolean IsNotEnglish;

        IsNotEnglish = GeneralStaticMethods.getLanguage(); //assaf - 27.10 - check what is the Device Language and set the Dates and Address accordignly
        String address = "";
        String date = "";
        String city = "";
        ParseQuery<Event> query = new ParseQuery("Event");

        if (producerId != null && producerId != "") {
            query.whereEqualTo("producerId", producerId);
        }
        query.orderByDescending("createdAt");
        query.setLimit(1000);
        List<Event> eventParse = null;
        try {
            eventParse = query.find();
            for (int i = 0; i < eventParse.size(); i++) {
                Event event = eventParse.get(i);
                //27.10 assaf - support non english Date and Address
                if (IsNotEnglish) {

                    Log.e("Address hebrew","value" + event.getAddressPerLanguage().get("iw"));
                    if (event.getAddressPerLanguage()!=null && !event.getAddressPerLanguage().get("iw").equals("")) {
                        address = event.getAddressPerLanguage().get("iw");//assaf - get the address in hebrew
                    }
                    else {
                        address = event.getAddress();
                    }
                    if (address =="" || address == null) {
                        address = event.getAddress();
                    }
                    if (event.getCityPerLanguage()!=null && !event.getCityPerLanguage().get("iw").equals("")) {
                        city = event.getCityPerLanguage().get("iw");//assaf - 28.10 save city name per Lanagauge as an object in Parse
                    }
                    else{
                        city = event.getCity();
                    }
                    if (city=="" || city == null)
                    {
                     city = event.getCity();
                    }
                    date = GeneralStaticMethods.getDateToStringConversion(event.getRealDate());
                } else {
                    address = event.getAddress();
                    date = getEventDateAsString(event.getRealDate());
                    city = event.getCity();
                }
                //////
                tempEventsList.add(new EventInfo(event.getPic().getUrl(),
                        event.getRealDate(),
                        //getEventDateAsString (event.getRealDate ()),
                        date,
                        event.getName(),
                        event.getTags(),
                        event.getPrice(),
                        event.getDescription(),
                        event.getPlace(),
                        //event.getAddress (),
                        address,
                        //event.getCity(),
                        city,
                        event.getEventToiletService(),
                        event.getEventParkingService(),
                        event.getEventCapacityService(),
                        event.getEventATMService(),
                        event.getFilterName(),
                        event.getSubFilterName(),
                        false,
                        event.getProducerId(),
                        i,
                        event.getX(),
                        event.getY(),
                        event.getArtist(),
                        event.getNumOfTickets(),
                        event.getObjectId(),
                        event.getFbUrl(),
                        event.getIsStadium(),event.getCancelEvent(), event.getCreatedAt(),event.getEventFromFacebook()//16.10 assaf added get created at
                ));
            }
            GeneralStaticMethods.updateSavedEvents(tempEventsList, context);
            GlobalVariables.ALL_EVENTS_DATA.clear();
            GlobalVariables.ALL_EVENTS_DATA.addAll(tempEventsList);

            if (!GlobalVariables.IS_PRODUCER) {
                EventDataMethods.RemoveExpiredAndCanceledEvents(tempEventsList);//Assaf remove cnacled or expired events city location from the List of cities menu
                GlobalVariables.cityMenuInstance = new CityMenu(tempEventsList, context);
                GlobalVariables.namesCity = GlobalVariables.cityMenuInstance.getCityNames();
                if (!GlobalVariables.deepLinkEventObjID.equals("")) {
                    for (int i = 0; i < GlobalVariables.ALL_EVENTS_DATA.size(); i++) {
                        if (GlobalVariables.deepLinkEventObjID.equals(GlobalVariables.ALL_EVENTS_DATA.get(i).getParseObjectId())) {
                            Bundle b = new Bundle();
                            onEventItemClick(i, GlobalVariables.ALL_EVENTS_DATA, intent);
                            intent.putExtras(b);
                            context.startActivity(intent);
                            ic.eventDataCallback();
                            return;
                        }
                    }
                }
            }
            ic.eventDataCallback();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public static void RemoveExpiredAndCanceledEvents(List<EventInfo> eventList) // 22.01 assaf - for remove expired and cancled events form list
    {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        List <EventInfo> tempEventInfoList = new ArrayList<>();
        for (EventInfo eventInfo:eventList) {
            if (!eventInfo.getIsCanceled()&& !eventInfo.getDate().before(currentDate))
                tempEventInfoList.add(eventInfo);
        }

        eventList.clear();
        eventList.addAll(tempEventInfoList);
    }


    ///////Assaf:15/10 fetch from Parse only evnets that not expired
  /*  public static void downloadEventsDataWithoutExpiredEvents(final GetEventsDataCallback ic,
                                          String producerId,
                                          final Context context,
                                          final Intent intent) {
        final ArrayList<EventInfo> tempEventsList = new ArrayList<> ();
        ParseQuery<Event> query = new ParseQuery ("Event");

        Calendar calendar = Calendar.getInstance();// 15.10 - assaf to skip events that expired
        Date currentDate = calendar.getTime();// 15.10 - assaf to skip events that expired

        if (producerId != null && producerId != "") {
            query.whereEqualTo ("producerId", producerId);
        }
        query.whereGreaterThanOrEqualTo("realDate",currentDate);// 15.10 - assaf to skip events that expired
        query.orderByDescending ("createdAt");
        query.setLimit(1000);
        List<Event> eventParse = null;
        try {
            eventParse = query.find();
            for (int i = 0; i < eventParse.size (); i++) {
                Event event = eventParse.get (i);
                tempEventsList.add (new EventInfo (event.getPic ().getUrl (),
                                                          event.getRealDate (),
                                                          getEventDateAsString (event.getRealDate ()),
                                                          event.getName (),
                                                          event.getTags (),
                                                          event.getPrice (),
                                                          event.getDescription (),
                                                          event.getPlace (),
                                                          event.getAddress (),
                                                          event.getCity (),
                                                          event.getEventToiletService (),
                                                          event.getEventParkingService (),
                                                          event.getEventCapacityService (),
                                                          event.getEventATMService (),
                                                          event.getFilterName (),
                                                          event.getSubFilterName (),
                                                          false,
                                                          event.getProducerId (),
                                                          i,
                                                          event.getX (),
                                                          event.getY (),
                                                          event.getArtist (),
                                                          event.getNumOfTickets (),
                                                          event.getObjectId (),
                                                          event.getFbUrl (),
                                                          event.getIsStadium ()
                ));
            }
            GeneralStaticMethods.updateSavedEvents (tempEventsList, context);
            GlobalVariables.ALL_NON_EXPIRED_EVENTS_DATA.clear ();
            GlobalVariables.ALL_NON_EXPIRED_EVENTS_DATA.addAll (tempEventsList);

            if (!GlobalVariables.IS_PRODUCER) {
                GlobalVariables.cityMenuInstance = new CityMenu(tempEventsList, context);
                GlobalVariables.namesCity = GlobalVariables.cityMenuInstance.getCityNames();
                if (!GlobalVariables.deepLinkEventObjID.equals("")) {
                    for (int i = 0; i < GlobalVariables.ALL_NON_EXPIRED_EVENTS_DATA.size(); i++) {
                        if (GlobalVariables.deepLinkEventObjID.equals(GlobalVariables.ALL_NON_EXPIRED_EVENTS_DATA.get(i).getParseObjectId())) {
                            Bundle b = new Bundle();
                            onEventItemClick(i, GlobalVariables.ALL_NON_EXPIRED_EVENTS_DATA, intent);
                            intent.putExtras(b);
                            context.startActivity(intent);
                            ic.eventDataCallback();
                            return;
                        }
                    }
                }
            }
            ic.eventDataCallback();
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }*/

    public static EventInfo getEventFromObjID(String parseObjID, List<EventInfo> eventsList) {
        for (EventInfo eventInfo : eventsList) {
            if (eventInfo.getParseObjectId().equals(parseObjID)) {
                return eventInfo;
            }
        }
        return null;
    }

    public static void uploadArtistData() { // Assaf: updated , Global Varialble.Artist was added
        GlobalVariables.artist_list.clear();
        List<String> temp_artist_list = new ArrayList<String>();
        for (int i = 0; i < GlobalVariables.ALL_EVENTS_DATA.size(); i++) {
            EventInfo eventInfo = GlobalVariables.ALL_EVENTS_DATA.get(i);
            if (eventInfo.getArtist() != null &&
                    !eventInfo.getArtist().equals("") &&
                    !temp_artist_list.contains(eventInfo.getArtist())) {
                temp_artist_list.add(eventInfo.getArtist());
                GlobalVariables.artist_list.add(new Artist(eventInfo.getArtist()));// assaf added
            }
        }
        GlobalVariables.artist_list.add(new Artist(GlobalVariables.No_Artist_Events));// assaf added
    }

    public static void uploadArtistDataWithoutNoArtist() { // Assaf: 13/10 - added - pull Artists only without no Artists
        GlobalVariables.artist_list.clear();
        List<String> temp_artist_list = new ArrayList<String>();
        for (int i = 0; i < GlobalVariables.ALL_EVENTS_DATA.size(); i++) {
            EventInfo eventInfo = GlobalVariables.ALL_EVENTS_DATA.get(i);
            if (eventInfo.getArtist() != null &&
                    !eventInfo.getArtist().equals("") &&
                    !temp_artist_list.contains(eventInfo.getArtist())) {
                temp_artist_list.add(eventInfo.getArtist());
                GlobalVariables.artist_list.add(new Artist(eventInfo.getArtist()));
            }
        }
    }

    public static String getEventDateAsString(Date eventDate) { //29.09 assaf
        long time = eventDate.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String dayOfWeek = null;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                dayOfWeek = "SUN";
                break;
            case 2:
                dayOfWeek = "MON";
                break;
            case 3:
                dayOfWeek = "TUE";
                break;
            case 4:
                dayOfWeek = "WED";
                break;
            case 5:
                dayOfWeek = "THU";
                break;
            case 6:
                dayOfWeek = "FRI";
                break;
            case 7:
                dayOfWeek = "SAT";
                break;
        }
        String month = null;
        switch (calendar.get(Calendar.MONTH)) {
            case 0:
                month = "JAN";
                break;
            case 1:
                month = "FEB";
                break;
            case 2:
                month = "MAR";
                break;
            case 3:
                month = "APR";
                break;
            case 4:
                month = "MAY";
                break;
            case 5:
                month = "JUN";
                break;
            case 6:
                month = "JUL";
                break;
            case 7:
                month = "AUG";
                break;
            case 8:
                month = "SEP";
                break;
            case 9:
                month = "OCT";
                break;
            case 10:
                month = "NOV";
                break;
            case 11:
                month = "DEC";
                break;
        }
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int year = calendar.get(Calendar.YEAR); //29.09 - Assaf added to present year
        String ampm = null;
        if (calendar.get(Calendar.AM_PM) == Calendar.AM)
            ampm = "AM";
        else if (calendar.get(Calendar.AM_PM) == Calendar.PM)
            ampm = "PM";

        String min;
        if (minute < 10) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }
        return dayOfWeek + ", " + month + " " + day + ", " + year + ", " + hour + ":" + min + " " + ampm;//29.09 -assaf add year
    }

    public static String getDisplayedEventPrice(String eventPrice) {
        if (eventPrice.contains("-")) {
            String[] prices = eventPrice.split("-");
            return prices[0] + "₪-" + prices[1] + "₪";
        } else if (!eventPrice.equals("FREE")) {
            return eventPrice + "₪";
        } else {
            return eventPrice;
        }
    }

    public static void updateEventInfoDromParseEvent(EventInfo eventInfo,
                                                     Event event) {
        eventInfo.setPrice(event.getPrice());
        eventInfo.setAddress(event.getAddress());
        eventInfo.setIsStadium(event.getIsStadium());
        eventInfo.setParseObjectId(event.getObjectId());
        Date currentDate = new Date();
        eventInfo.setIsFutureEvent(event.getRealDate().after(currentDate));
        eventInfo.setArtist(event.getArtist());
        eventInfo.setAtm(event.getEventATMService());
        eventInfo.setCapacity(event.getEventCapacityService());
        eventInfo.setDate(event.getRealDate());
        eventInfo.setDateAsString(getEventDateAsString(event.getRealDate()));
        eventInfo.setFilterName(event.getFilterName());
        eventInfo.setDescription(event.getDescription());
        eventInfo.setName(event.getName());
        eventInfo.setNumOfTickets(event.getNumOfTickets());
        eventInfo.setPlace(event.getPlace());
        eventInfo.setProducerId(event.getProducerId());
        eventInfo.setParking(event.getEventParkingService());
        eventInfo.setTags(event.getTags());
        eventInfo.setToilet(event.getEventToiletService());
        eventInfo.setX(event.getX());
        eventInfo.setY(event.getY());
    }

    public static void addressNameNonEnglish(String address, HashMap<String, String> addressPerLanguage,HashMap<String, String> cityPerLanguage) throws UnsupportedEncodingException // save address name differnet then english
    {
        String langauage = "";
        String languageCode = "";
        String queryString;
        String street = "";
        String city = "";
        String number = "";
        String valid_address = "";
        String addressArray = "";

        languageCode = "iw"; // 28.10 assaf cuurenly support Hebrew only
        langauage = "&language=" + languageCode;

        try {
            queryString = "" +
                    "&address=" + URLEncoder.encode(address, "utf-8") +
                    "&key=" + GlobalVariables.GEO_API_KEY + langauage;

            addressArray = HttpHandler.get(GlobalVariables.GEO_API_ADDRESS, queryString);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (addressArray != null && addressArray != "") {
                Gson gson = new Gson();
                Result result = gson.fromJson(addressArray, Result.class);

                if (result.getStatus().equals("OK")) {
                    String long_name = result.getResults().get(0).getAddress_components().get(1).getLong_name();
                    street = long_name.replaceAll("Street", "");
                    number = result.getResults().get(0).getAddress_components().get(0).getShort_name();
                    city = result.getResults().get(0).getAddress_components().get(2).getLong_name();

                    valid_address = street + " " + number + ", " + city;
                    addressPerLanguage.put(languageCode, valid_address);
                    cityPerLanguage.put (languageCode,city);                }
                else if (result.getStatus().equals("ZERO_RESULTS")) {
                    addressPerLanguage.put(languageCode, "");
                    cityPerLanguage.put (languageCode,"");
                }
            }
            else
            {
                addressPerLanguage.put(languageCode, "");
                cityPerLanguage.put(languageCode,"");
            }
        } catch (Exception e) {
            addressPerLanguage.put(languageCode, "");
            cityPerLanguage.put(languageCode,"");
            e.printStackTrace();
        }
    }
}
//Background Method - not in use for now
  /*  static class  findAndSaveAddress extends AsyncTask<String, Void, String> {//assaf 28.10 - look for same address but in other lanaguages and rerurn
        String langauage = "";
        String languageCode = "";
        String queryString;
        String street = "";
        String city = "";
        String number = "";
        String valid_address = "";
        String addressArray[] = new String[1];

        @Override
        protected String doInBackground(String... params) {

     //   if (GeneralStaticMethods.getLanguage()){ // check if device Language is Hebrew or English
            languageCode = "iw";
            langauage = "&language=" +languageCode;
//   //  }
//  // else {
//   //   langauage = "&language=en";
//    // }
            try {
                queryString = "" +
                        "&address=" + URLEncoder.encode(params[0], "utf-8") +
                        "&key=" + GlobalVariables.GEO_API_KEY + langauage;

                addressArray[0] = HttpHandler.get(GlobalVariables.GEO_API_ADDRESS, queryString);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return addressArray[0];
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try
            {
                if (addressArray[0] !=null && addressArray[0] !="")
                {
                    Gson gson = new Gson();
                    Result result = gson.fromJson(addressArray[0], Result.class);

                    if (result.getStatus().equals("OK"))
                    {
                        String long_name = result.getResults().get(0).getAddress_components().get(1).getLong_name();
                        street = long_name.replaceAll("Street", "");
                        number = result.getResults().get(0).getAddress_components().get(0).getShort_name();
                        city = result.getResults().get(0).getAddress_components().get(2).getLong_name();
                    }

                    valid_address = street + " " + number + ", " + city;
                    Log.i("address", valid_address);
                    addressPerLanguage .put(languageCode, valid_address);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.i("address", addressPerLanguage.get("iw"));
        }
    }*/

