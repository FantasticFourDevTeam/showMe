package com.example.FundigoApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.Events.EventsListAdapter;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.lang.Runnable;

public class SearchActivity extends AppCompatActivity implements SearchView.OnClickListener, AdapterView.OnItemClickListener {
    AutoCompleteTextView autoCompleteTextView;

    SearchView search;
    ListView listView;
    EventsListAdapter listAdpter;
    Button b_history, b_clear;
    static String wordSearch;
    ArrayList<String> history = new ArrayList<>();
    ArrayAdapter<String> autoStrings;
    PopupMenu historyPop;
    List<EventInfo> eventsResultList;
    Thread saveThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        readHistory();

        search = (SearchView) findViewById(R.id.b_search);
        b_history = (Button) findViewById(R.id.b_history);
        b_clear = (Button) findViewById(R.id.b_clear);
        listView = (ListView) findViewById(R.id.listView_Search);

        b_clear.setOnClickListener(this);
        search.setOnClickListener(this);

        String[] auto = getResources().getStringArray(R.array.autoComplateStrings);
        autoStrings = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, auto);
        eventsResultList = new ArrayList<EventInfo>();
        listAdpter = new EventsListAdapter(this, eventsResultList, false);
        listView.setAdapter(listAdpter);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(this);

        //Creating the instance of PopupMenu
        historyPop = new PopupMenu(SearchActivity.this, b_history);
        //Inflating the Popup using xml file
        historyPop.getMenuInflater().inflate(R.menu.popup_city, historyPop.getMenu());
        b_history = (Button) findViewById(R.id.b_history);

        b_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (history.size() > 0) {
                    //Creating the instance of PopupMenu
                    historyPop = new PopupMenu(SearchActivity.this, b_history);
                    //Inflating the Popup using xml file
                    historyPop.getMenuInflater().inflate(R.menu.popup_history, historyPop.getMenu());
                    //registering popup with OnMenuItemClickListener
                    for (int i = 0; i < history.size(); i++) {
                        historyPop.getMenu().add(history.get(i));
                    }
                    historyPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                          //  autoCompleteTextView.setText(item.getTitle());
                            search.setQuery(item.getTitle(),true);
                            return true;
                        }
                    });
                    historyPop.show();//showing popup menu
                } else {
                    Toast.makeText(SearchActivity.this, R.string.not_have_history, Toast.LENGTH_SHORT).show();
                }
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                wordSearch = search.getQuery().toString();
                if (wordSearch.length() == 0 || wordSearch==null){
                    Toast.makeText(SearchActivity.this, R.string.input_word_to_search, Toast.LENGTH_SHORT).show();
                } else {

                    if (wordSearch.length() > 0) {
                            if (saveThread == null) {
                            saveThread = new Thread(new saveHistoryRunnable());
                            saveThread.start();//saveThread.start();
                        }
                    }
                    eventsResultList.clear();
                    eventsResultList.addAll(searchInfo(wordSearch));
                    listAdpter.notifyDataSetChanged();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                wordSearch = newText;
                eventsResultList.clear();
                eventsResultList.addAll(searchInfo(wordSearch));

                if (wordSearch.length() > 0) {
                   // history.add(0, wordSearch);
                    //saveHistory();
                    if (saveThread == null) {
                        saveThread = new Thread(new saveHistoryRunnable());
                        saveThread.start();//saveThread.start();
                    }
                  }
                listAdpter.notifyDataSetChanged();
                return false;
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId () == search.getId ()) {
            wordSearch = search.getQuery ().toString ();
            if (wordSearch.length() == 0 || wordSearch==null) {
                Toast.makeText(this, "No text to search", Toast.LENGTH_SHORT).show();
            }
        }
        if (view.getId() == b_clear.getId()) {
            clearHistory();
        }
    }

    private ArrayList<EventInfo> searchInfo(String search) {

            boolean flag = true;
            ArrayList<EventInfo> ans = new ArrayList<>();
            ArrayList<String> checkIfInside = new ArrayList<>();
            List<EventInfo> tempEventListForFilterSearchResults = new ArrayList<>();
            tempEventListForFilterSearchResults.addAll(GlobalVariables.ALL_EVENTS_DATA);
            EventDataMethods.RemoveExpiredAndCanceledEvents(tempEventListForFilterSearchResults);
          try {
            for (int i = 0; i < tempEventListForFilterSearchResults.size(); i++) {
                if (!checkIfInside.contains(tempEventListForFilterSearchResults.get(i).getParseObjectId())) {
                    Log.e("search", "eventinfo" + tempEventListForFilterSearchResults.get(i).getInfo().toString());
                    Log.e("search", "searchtext" + search.toLowerCase().toString());

                    if (tempEventListForFilterSearchResults.get(i).getInfo()!=null) {
                        if (tempEventListForFilterSearchResults.get(i).getInfo().toLowerCase().contains(search.toLowerCase())) {
                            ans.add(tempEventListForFilterSearchResults.get(i)); // search in even description
                            checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                        }
                        flag = false;
                    }

                    if (flag && tempEventListForFilterSearchResults.get(i).getName().toLowerCase().contains(search.toLowerCase())) {
                        ans.add(tempEventListForFilterSearchResults.get(i));
                        checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                        flag = false;
                    }

                    if (flag && tempEventListForFilterSearchResults.get(i).getFilterName().toLowerCase().contains(search.toLowerCase())) {
                        ans.add(tempEventListForFilterSearchResults.get(i));
                        checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                        flag = false;
                    }

                    if (flag && tempEventListForFilterSearchResults.get(i).getTags().toLowerCase().contains(search.toLowerCase())) {
                        ans.add(tempEventListForFilterSearchResults.get(i));
                        checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                        flag = false;
                    }

                    //Assaf added - search also in Address
                    if (flag && tempEventListForFilterSearchResults.get(i).getAddress().toLowerCase().contains(search.toLowerCase())) {
                        ans.add(tempEventListForFilterSearchResults.get(i));
                        checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                        flag = false;
                    }
                    //09.10 . assaf added to search in artist value
                    if (flag && tempEventListForFilterSearchResults.get(i).getArtist().toLowerCase().contains(search.toLowerCase())) {
                        ans.add(tempEventListForFilterSearchResults.get(i));
                        checkIfInside.add(tempEventListForFilterSearchResults.get(i).getParseObjectId());
                    }
                    flag = true;
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return ans;
    }

    private void clearHistory() {

        try {
            history.clear();
            File inputFile = new File("history");
            File tempFile = new File("myTempFile");
            tempFile.renameTo(inputFile);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        Toast.makeText(this, "History was cleaned", Toast.LENGTH_SHORT).show();
    }


    private void readHistory() {
        FileInputStream inputStream;
        String readLine = "";
        int index = 0;
        try {
            inputStream = openFileInput("history.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((readLine = bufferedReader.readLine()) != null) {
                history.add(index++, readLine);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class saveHistoryRunnable implements Runnable {// search text is Saved  in other Thread
        String tempWordSearch;
        public void run() {
            FileOutputStream outputStream;
            try {
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
            startSave();

             if (wordSearch.length() !=0) {
                history.add(0, wordSearch);
             }
             if (history.size()!=0) {
                 try {
                     outputStream = openFileOutput("history.txt", Context.MODE_PRIVATE);
                     for (int i = 0; i < history.size(); i++) {
                         outputStream.write(history.get(i).getBytes());
                         outputStream.write("\n".getBytes());
                     }
                     outputStream.close();
                     if (saveThread != null) {
                         saveThread.interrupt();// close the Thread after save
                         saveThread = null;
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }

             }
        }

        public void startSave() {
            if (tempWordSearch==null)
               tempWordSearch = wordSearch;// intinalize for the first time
            try {
                Thread.sleep(2000); // sleep for giving the option to wrote a full Phrase (word) and save full phrase and not letter by letter
            }
            catch (Exception ex) {
                Log.e ("TAG",ex.getMessage());
            }
            checkHistoryContent();
        }
        public void checkHistoryContent()
        {
           if (tempWordSearch.equals(wordSearch)) { // giving the option to wrote a full Phrase (word) and save full phrase and not letter
               Log.i("TAG","Word Search can be saved");
            }
            else
            {
                tempWordSearch = wordSearch;
                 this.startSave();
            }
        }
    }



//    private void saveHistory() {
//        FileOutputStream outputStream;
//        try {
//            outputStream = openFileOutput ("history.txt", Context.MODE_PRIVATE);
//            for (int i = 0; i < history.size (); i++) {
//                outputStream.write (history.get (i).getBytes ());
//                outputStream.write("\n".getBytes());
//            }
//            outputStream.close ();
//        } catch (Exception e) {
//            e.printStackTrace ();
//        }
//    }

//    @Override
//    public void onBackPressed() {
////        if (history.size () > 0) {
////            saveHistory ();
////        }
//        finish();
//    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        GeneralStaticMethods.onActivityResult (requestCode,
                                                      data,
                                                      this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle b = new Bundle ();
        Intent intent = new Intent (this, EventPageActivity.class);
        EventDataMethods.onEventItemClick (position, eventsResultList, intent);
        intent.putExtras (b);
        startActivity (intent);
    }
}
