package com.example.FundigoApp.StaticMethod;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventsListAdapter;
import com.example.FundigoApp.Filter.FilterPageActivity;
import com.example.FundigoApp.GlobalVariables;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FilterMethods extends Application {

    public static Context _context;
    public static String [] resultsGet;//for get Dates range Shared P.

    public static void setContext(Context context) { //used for callin gthe getdata and pass Context parameter to it.
        _context = context;
    }

    public static void filterEventsByArtist(String artistName, List<EventInfo> eventsListFiltered) {
        eventsListFiltered.clear ();
        for (EventInfo eventInfo : GlobalVariables.ALL_EVENTS_DATA) {
            if (eventInfo.getArtist () == null || eventInfo.getArtist ().isEmpty ()) {
                if (artistName.equals (GlobalVariables.No_Artist_Events)) {
                    eventsListFiltered.add (eventInfo);
                }
            } else if (eventInfo.getArtist ().equals (artistName)) {
                eventsListFiltered.add (eventInfo);
            }
        }
    }

    public static void filterListsAndUpdateListAdapter(List<EventInfo> eventsListToFilter,
                                                       EventsListAdapter eventsListAdapter,
                                                       String[] namesCity,
                                                       int indexCityChosen) {
        try {

            if (GlobalVariables.USER_CHOSEN_CITY_MANUALLY) {
                ArrayList<EventInfo> tempEventsList =
                        filterByCityAndFilterName(
                                namesCity[indexCityChosen],
                                GlobalVariables.CURRENT_FILTER_NAME,
                                GlobalVariables.CURRENT_SUB_FILTER,
                                GlobalVariables.CURRENT_DATE_FILTER,
                                GlobalVariables.CURRENT_PRICE_FILTER,
                                GlobalVariables.ALL_EVENTS_DATA);
                eventsListToFilter.clear();
                eventsListToFilter.addAll(tempEventsList);
                eventsListAdapter.notifyDataSetChanged();

            } else if (GlobalVariables.CITY_GPS != null) {
                ArrayList<EventInfo> tempEventsList =
                        filterByCityAndFilterName(
                                GlobalVariables.CITY_GPS,
                                GlobalVariables.CURRENT_FILTER_NAME,
                                GlobalVariables.CURRENT_SUB_FILTER,
                                GlobalVariables.CURRENT_DATE_FILTER,
                                GlobalVariables.CURRENT_PRICE_FILTER,
                                GlobalVariables.ALL_EVENTS_DATA);

                eventsListToFilter.clear();
                eventsListToFilter.addAll(tempEventsList);
                eventsListAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static List<EventInfo> filterByFilterName(//String currentFilterName,
                                                     ArrayList<String> currentFilterName,
                                                     String subFilterName,
                                                     Date dateFilter, int priceFilter,
                                                     List<EventInfo> eventsListToFilter) {
        ArrayList<EventInfo> tempEventsList = new ArrayList<>();
        Date _currentDate = new Date();
        resultsGet = getData(_context);//for date range
        Date[] datesFilter = getRangeDateForFilter();//for dates range

        try {
            if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter == null && priceFilter == -1) {
                tempEventsList.addAll(eventsListToFilter);
                return tempEventsList;
            } else {
                for (int i = 0; i < eventsListToFilter.size(); i++) {
                    String subFilterEvent = eventsListToFilter.get(i).getSubFilterName();
                    Date dateEvent = eventsListToFilter.get(i).getDate();
                    String filterEvent = eventsListToFilter.get(i).getFilterName();
                    String tempEventPrice = eventsListToFilter.get(i).getPrice();
                    int priceEvent = -1;
                    boolean IsDateEqual = false;
                    Date weekEndFilter = FilterPageActivity.addDays(_currentDate, 1000); // for check if weekend filter was activivated
                    boolean IsWeekendFilter = false;
                    boolean IsEventInWeekEnd = false;
                    boolean IsDatesRangeSelected = false; // 19.10 assaf to support filter by dates Range
                    boolean ISEventInRange = false;
                    if (dateFilter != null && DateCompare(weekEndFilter, dateFilter)) // DateCompare(weekEndFilter,dateFilter) to check if Weekdnd filter seletced
                    {
                        IsWeekendFilter = true; // to check if weekd end filter selected
                        Date endofWeekDate = FilterPageActivity.getCurrentWeekend(); // end day of the week
                        Date twoDaysBeforeEndOfWeek = FilterPageActivity.addDays(FilterPageActivity.getCurrentWeekend(), -3); // three days before
                        if (dateEvent.after(twoDaysBeforeEndOfWeek) && dateEvent.before(endofWeekDate)) {
                            IsEventInWeekEnd = true;
                        }
                    }

                    if (dateFilter != null && (!IsWeekendFilter || !IsDatesRangeSelected)) // current and event date compare in case of date filter is activate
                    {
                        IsDateEqual = DateCompare(dateEvent, dateFilter); // Isdateequal = true in all filters except when weekdend selected or Dates in Range
                    }

                    // in case that price is FREE
                    if (!tempEventPrice.equals("FREE")) {
                        priceEvent = priceHandler(eventsListToFilter.get(i).getPrice());
                    }

                    if (dateFilter!=null && (DateCompare(dateFilter,FilterPageActivity.addDays(_currentDate,3000))))//19.10 - assaf check if Dates range filter (From or To or Both) were set
                    {
                        IsDatesRangeSelected = true;
                        if (!datesFilter[0].equals("") && !datesFilter[1].equals("")) {// from and To
                            if (dateEvent.after(datesFilter[0]) && dateEvent.before(datesFilter[1])) {
                                ISEventInRange = true;
                            }
                        }
//                          else if (!datesFilter[0].equals("") && datesFilter[1].equals("")) { // only From
//                            if (dateEvent.after(datesFilter[0])) {
//                                ISEventInRange = true;
//                            }
//                        }
//                          else if (datesFilter[0].equals("") && !datesFilter[1].equals("")) { // only To
//                            if (dateEvent.before(datesFilter[1])) {
//                                ISEventInRange = true;
//                            }
//                        }
                    }


                    //==============Start point of conditions to filters====================== ///

                    if (mainFilterListFindFilter(currentFilterName, filterEvent) & dateFilter != null // All filters
                            & priceFilter != -1 & subFilterName.equals(subFilterEvent)) {
                        if ((IsDateEqual && priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0)) // NEED to HANDLE EOW
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if ((ISEventInRange && IsDatesRangeSelected && priceFilter != 201 && priceFilter >= priceEvent) || (IsDatesRangeSelected && ISEventInRange && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && ISEventInRange & IsDatesRangeSelected) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }


                    } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & dateFilter != null // main ,Price + date filters. no sub
                            & priceFilter != -1 & subFilterName.isEmpty()) {
                        if ((IsDateEqual && priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if ((ISEventInRange && IsDatesRangeSelected && priceFilter != 201 && priceFilter >= priceEvent) || (IsDatesRangeSelected && ISEventInRange && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsDatesRangeSelected & ISEventInRange) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }


                    } else if (mainFilterListFindFilter(currentFilterName, filterEvent) && dateFilter == null
                            && priceFilter == -1 && subFilterName.isEmpty())//only main Filter
                    {
                        tempEventsList.add(eventsListToFilter.get(i));
                    } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.equals(subFilterEvent)
                            && dateFilter == null && priceFilter == -1) // main + sub and no date filter and no price filters
                    {
                        tempEventsList.add(eventsListToFilter.get(i));
                    } else if (mainFilterListFindFilter(currentFilterName,filterEvent) & subFilterName.equals(subFilterEvent) &&
                            dateFilter != null && priceFilter == -1)// main + sub + date and no Price filter
                    {
                        if (IsDateEqual) // NEED to HANDLE EOW
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (IsWeekendFilter && IsEventInWeekEnd) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (ISEventInRange && IsDatesRangeSelected) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                    } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.equals(subFilterEvent)
                            && dateFilter == null && priceFilter != -1)// main + sub + price and no Date filter
                    {
                        if (((priceFilter >= priceEvent && priceFilter != 201) || (tempEventPrice.equals("FREE")) && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                    } else if (mainFilterListFindFilter(currentFilterName,filterEvent) & subFilterName.isEmpty() &
                            dateFilter != null && priceFilter == -1)// main + date , no sub and no Price filter
                    {
                        if (IsDateEqual) //
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (IsWeekendFilter && IsEventInWeekEnd) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (IsDatesRangeSelected && ISEventInRange) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                    } else if (mainFilterListFindFilter(currentFilterName,filterEvent) & subFilterName.isEmpty()
                            && dateFilter == null && priceFilter != -1)// main + price, No sub and no Date filter
                    {
                        if (((priceFilter >= priceEvent && priceFilter != 201) || (tempEventPrice.equals("FREE")) && priceFilter == 0)) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (priceFilter == 201 && priceEvent >= priceFilter) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                    } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter != null //no main + no sub, price and date filters only
                            && priceFilter != -1) {
                        if (IsDateEqual && (priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if ((IsDatesRangeSelected && ISEventInRange && priceFilter != 201 && priceFilter >= priceEvent) || (ISEventInRange && IsDatesRangeSelected && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (priceFilter == 201 && priceEvent >= priceFilter && IsDatesRangeSelected & ISEventInRange) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                    } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter == null // price filter only
                            && priceFilter != -1) {
                        if ((priceFilter >= priceEvent && priceFilter != 201 || (tempEventPrice.equals("FREE") && priceFilter == 0))) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (priceFilter == 201 && priceEvent >= priceFilter) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                    } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter != null // date filter only
                            && priceFilter == -1) {
                        if (IsDateEqual) // other date filters
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                        if (IsWeekendFilter && IsEventInWeekEnd)  // weekend filter
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (ISEventInRange && IsDatesRangeSelected)  // weekend filter
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }
                    }
                }
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return tempEventsList;
    }

    public static ArrayList<EventInfo> filterByCityAndFilterName(String cityName,
                                                                 //String currentFilterName,
                                                                 ArrayList <String> currentFilterName,
                                                                 String subFilterName, Date dateFilter, int priceFilter,
                                                                 List<EventInfo> eventsListToFilter) {
        ArrayList<EventInfo> tempEventsList = new ArrayList<>();
        Date _currentDate = new Date();
        String allCitiesFilterString;
        resultsGet = getData(_context);

        Date[] datesFilter = getRangeDateForFilter();

        try {
            if (Locale.getDefault().getDisplayLanguage().equals("עברית")) {
                allCitiesFilterString = "כל הערים";
            } else {
                allCitiesFilterString = "All Cities";
            }

            if (cityName.equals(allCitiesFilterString) && currentFilterName.isEmpty() && subFilterName.isEmpty()
                    && dateFilter == null && priceFilter == -1) {
                tempEventsList.addAll(eventsListToFilter);
                return tempEventsList;

            } else {
                for (int i = 0; i < eventsListToFilter.size(); i++) {
                    String cityEvent = eventsListToFilter.get(i).getCity();
                    String subFilterEvent = eventsListToFilter.get(i).getSubFilterName();
                    Date dateEvent = eventsListToFilter.get(i).getDate();
                    String filterEvent = eventsListToFilter.get(i).getFilterName();
                    String tempEventPrice = eventsListToFilter.get(i).getPrice();
                    int priceEvent = -1;
                    boolean IsDateEqual = false;
                    Date weekEndFilter = FilterPageActivity.addDays(_currentDate, 1000); // for check if weekend filter was activivated
                    boolean IsWeekendFilter = false;
                    boolean IsEventInWeekEnd = false;
                    boolean IsDatesRangeSelected = false; // 19.10 assaf to support filter by dates Range
                    boolean ISEventInRange = false;
                    if (dateFilter != null && DateCompare(weekEndFilter, dateFilter)) // to check if Weekdnd filter seletced
                    {
                        IsWeekendFilter = true; // to check if weekd end filter selected
                        Date endofWeekDate = FilterPageActivity.getCurrentWeekend(); // end day of the week
                        Date twoDaysBeforeEndOfWeek = FilterPageActivity.addDays(FilterPageActivity.getCurrentWeekend(), -3); // two days before
                        if (dateEvent.after(twoDaysBeforeEndOfWeek) && dateEvent.before(endofWeekDate)) {
                            IsEventInWeekEnd = true;
                        }
                    }

                    if (dateFilter!=null && (DateCompare(dateFilter,FilterPageActivity.addDays(_currentDate,3000))))//19.10 - assaf check if Dates range filter (From or To or Both) were set
                    {
                        IsDatesRangeSelected = true;
                        if (!datesFilter[0].equals("") && !datesFilter[1].equals("")) {// from and To
                            if (dateEvent.after(datesFilter[0]) && dateEvent.before(datesFilter[1])) {
                                ISEventInRange = true;
                            }
                        }
                    }

                    if (dateFilter != null && (!IsWeekendFilter || !IsDatesRangeSelected)) // current and event date compare in case of date filter is activate
                    {
                        IsDateEqual = DateCompare(dateEvent, dateFilter); // Isdateequal = true in all filters except when weekdend selected or Dated in range
                    }

                    // in case that price is FREE
                    if (!tempEventPrice.equals("FREE")) {
                        priceEvent = priceHandler(eventsListToFilter.get(i).getPrice());
                    }


                    //==============Start point of conditions to filters====================== ///


                    if (cityName.equals(allCitiesFilterString) || (cityEvent != null && cityEvent.equals(cityName))) {
                        if (currentFilterName.isEmpty() & dateFilter == null // All filters empty
                                & priceFilter == -1 & subFilterName.isEmpty()) {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        if (mainFilterListFindFilter(currentFilterName, filterEvent) & dateFilter != null // All filters active
                                & priceFilter != -1 & subFilterName.equals(subFilterEvent)) {
                            if ((IsDateEqual && priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if ((IsDatesRangeSelected && ISEventInRange && priceFilter != 201 && priceFilter >= priceEvent) || (IsDatesRangeSelected && ISEventInRange && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDatesRangeSelected & ISEventInRange) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                        } else if (mainFilterListFindFilter(currentFilterName,filterEvent) & dateFilter != null // main ,Price + date filters. no sub
                                & priceFilter != -1 & subFilterName.isEmpty()) {
                            if ((IsDateEqual && priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if ((IsDatesRangeSelected && ISEventInRange && priceFilter != 201 && priceFilter >= priceEvent) || (IsDatesRangeSelected && ISEventInRange && tempEventPrice.equals("FREE") && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDatesRangeSelected & ISEventInRange) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }


                        }


                        else if (mainFilterListFindFilter(currentFilterName,filterEvent) && dateFilter == null
                                && priceFilter == -1 && subFilterName.isEmpty())//only main Filter
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        }

                        else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.equals(subFilterEvent)
                                && dateFilter == null && priceFilter == -1) // main + sub and no date filter and no price filters
                        {
                            tempEventsList.add(eventsListToFilter.get(i));
                        } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.equals(subFilterEvent) &&
                                dateFilter != null && priceFilter == -1)// main + sub + date and no Price filter
                        {
                            if (IsDateEqual) // NEED to HANDLE EOW
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (IsWeekendFilter && IsEventInWeekEnd) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (IsDatesRangeSelected && ISEventInRange) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                        } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.equals(subFilterEvent)
                                && dateFilter == null && priceFilter != -1)// main + sub + price and no Date filter
                        {
                            if (((priceFilter >= priceEvent && priceFilter != 201) || (tempEventPrice.equals("FREE")) && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                        } else if (mainFilterListFindFilter(currentFilterName, filterEvent) & subFilterName.isEmpty() &
                                dateFilter != null && priceFilter == -1)// main + date , no sub and no Price filter
                        {
                            if (IsDateEqual) //
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (IsWeekendFilter && IsEventInWeekEnd) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (IsDatesRangeSelected && ISEventInRange) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                        } else if (mainFilterListFindFilter(currentFilterName,filterEvent) & subFilterName.isEmpty()
                                && dateFilter == null && priceFilter != -1)// main + price, No sub and no Date filter
                        {
                            if (((priceFilter >= priceEvent && priceFilter != 201) || (tempEventPrice.equals("FREE")) && priceFilter == 0)) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (priceFilter == 201 && priceEvent >= priceFilter) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                        } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter != null //no main + no sub, price and date filters only
                                && priceFilter != -1) {
                            if (IsDateEqual && (priceFilter != 201 && priceFilter >= priceEvent) || (IsDateEqual && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDateEqual) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if ((IsWeekendFilter && IsEventInWeekEnd && priceFilter != 201 && priceFilter >= priceEvent) || (IsWeekendFilter && IsEventInWeekEnd && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsWeekendFilter & IsEventInWeekEnd) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if ((IsDatesRangeSelected && ISEventInRange && priceFilter != 201 && priceFilter >= priceEvent) || (IsDatesRangeSelected && ISEventInRange && tempEventPrice.equals("FREE") && priceFilter == 0))  // NEED to HANDLE EOW
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (priceFilter == 201 && priceEvent >= priceFilter && IsDatesRangeSelected & ISEventInRange) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }


                        } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter == null // price filter only
                                && priceFilter != -1) {
                            if ((priceFilter >= priceEvent && priceFilter != 201 || (tempEventPrice.equals("FREE") && priceFilter == 0))) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (priceFilter == 201 && priceEvent >= priceFilter) {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                        } else if (currentFilterName.isEmpty() && subFilterName.isEmpty() && dateFilter != null // date filter only
                                && priceFilter == -1) {
                            if (IsDateEqual) // other date filters
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                            if (IsWeekendFilter && IsEventInWeekEnd)  // weekend filter
                            {
                                tempEventsList.add(eventsListToFilter.get(i));
                            }

                            if (IsDatesRangeSelected && ISEventInRange){ // 19.10 assaf Add Dates Range to
                                tempEventsList.add(eventsListToFilter.get(i));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return tempEventsList;
    }

    private static Integer priceHandler(String price)// parse the price (x-y) got from Parse and take the minmum value
    {
        StringBuilder sb = new StringBuilder(price);
        int result;
        String tempPrice = "";
        try {
            result = sb.indexOf("-");
            if (result != -1) {
                tempPrice = sb.substring(0, result);
            } else {
                tempPrice = price;
            }
        } catch (Exception Ex) {
            Log.e ("TAG", Ex.getMessage ());
        }
        return Integer.parseInt(tempPrice);
    }

    private static boolean DateCompare(Date filterDate, Date eventDate) // compare only date withut hours
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        boolean IsCompare = dateFormat.format(filterDate).equals(dateFormat.format(eventDate));

        if (IsCompare)
            return true;
        else
            return false;
    }

    //==============================================================================================

//    private static boolean DateAfterOrBefore(Date currentDate, Date eventDate) // compare dates, 15/10 assaf
//    {
//       if(currentDate.before(eventDate))
//            return true;
//        else
//            return false;
//    }


//  public static void sortEventListByDate (List<EventInfo> allEvents) {//get expired events ot the en dof events list
//
//      Calendar calendar = Calendar.getInstance();
//      final Date currentDate = calendar.getTime();
//
//      Collections.sort(allEvents, new Comparator<EventInfo>() {
//         @Override
//          public int compare(EventInfo eventDateOne,EventInfo eventDateTwo ) {
//
//              if (eventDateTwo.getCreatedAt().after(eventDateOne.getCreatedAt())&& eventDateTwo.getDate().after(currentDate))
//                  return 1;
//              if (eventDateTwo.getCreatedAt().after(eventDateOne.getCreatedAt()) &&  eventDateTwo.getDate().before(currentDate))
//                  return -1;
//              else if ((eventDateTwo.getCreatedAt().before(eventDateOne.getCreatedAt()) &&  eventDateTwo.getDate().before(currentDate)))
//                  return -1;
//             else
//                 return  0;
//          }
//      });
//  }

    private static String[] getData(Context context) // used for get price and date saved filters
    // display the filter info.
    {

        SharedPreferences _sharedPref = context.getSharedPreferences("filterInfo", Context.MODE_PRIVATE);
      //  String _filterName = _sharedPref.getString("mainFilter", "");
      //  String _date = _sharedPref.getString ("date", "");
        String _dateFrom = _sharedPref.getString ("dateFrom", "");
        String _dateTo =  _sharedPref.getString ("dateTo", "");
      //  String _price = _sharedPref.getString ("price", "");
      //  String _subFilter = _sharedPref.getString("subFilter","");
        //String _mainFilterForFilter = _sharedPref.getString("mainFilterForFilter",""); //
      //  String _subFilterForFilter = _sharedPref.getString("subFilterForFilter","");
      //  Integer _priceForFilter = _sharedPref.getInt("priceFilterForFilter", -5);
       // String _dateForFilter = _sharedPref.getString("dateFilterForFilter", "");

        //String[] values = {_date, _price,_subFilter,_filterName,"",_subFilterForFilter,Integer.toString(_priceForFilter),_dateForFilter,_dateFrom,_dateTo};
        String[] values = {_dateFrom,_dateTo};

        return values;
    }

    private static Date[] getRangeDateForFilter() // Static method for get the Ranges dates
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM d HH:mm:ss zz yyyy",Locale.ENGLISH);

        Date[] rangeDates=null;
        String fromDate = resultsGet[0];
        String toDate = resultsGet[1];
        try {
            if (resultsGet[0] !=null && resultsGet[0] != "" && resultsGet[1] != null && resultsGet[1]!="")
                rangeDates = new Date[]{dateFormat.parse(fromDate),dateFormat.parse(toDate) };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rangeDates;
    }

    private static boolean mainFilterListFindFilter(ArrayList <String> mainFiltersList, String eventTopic) // 01,.12 - assaf - method find if filters selected match events
    {
       if (mainFiltersList.contains(eventTopic))
           return true;
        else
           return false;
    }
}
