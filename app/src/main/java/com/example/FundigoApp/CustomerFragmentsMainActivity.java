package com.example.FundigoApp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerMenu.MenuActivity;
import com.example.FundigoApp.Customer.CustomerMenu.MyEventsTicketsActivity;
import com.example.FundigoApp.Customer.Social.CustomerMessageConversationsListActivity;
import com.example.FundigoApp.Customer.Social.MyNotificationsActivity;
import com.example.FundigoApp.Customer.Social.Profile;
import com.example.FundigoApp.Events.CreateEventActivity;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.Filter.FilterPageActivity;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods;
import com.example.FundigoApp.Verifications.SmsSignUpActivity;
import com.example.events.ActivityFacebook;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class CustomerFragmentsMainActivity extends AppCompatActivity implements View.OnClickListener, EventDataMethods.GetEventsDataCallback,GPSMethods.GpsICallback {
    ImageView search, notification;
    private SharedPreferences _sharedPref;
    Handler gpsMessageHandler;
    AlertDialog.Builder builder;
    Context context;
    private static List<EventInfo> filtered_events_data = new ArrayList<EventInfo>();
    //private static Button mainCityButton;
    private Button mainCityFilterButton;
    private BottomNavigationView BottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_main_activity_fragments);

        GlobalVariables.CUSTOMER_PHONE_NUM = FileAndImageMethods.getCustomerPhoneNumFromFile(this);
        //mainCityButton = (Button) findViewById(R.id.main_fragment_main_city_item);
        mainCityFilterButton = (Button)findViewById(R.id.main_fragment_main_city_item);

        customerLogin();
        registerDialog();

        context = this;
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_customer_layout);
        if (Locale.getDefault().getDisplayLanguage().equals("עברית")) {
            tabLayout.addTab(tabLayout.newTab().setText("אירועים"));
            tabLayout.addTab(tabLayout.newTab().setText("אירועים שמורים"));
            tabLayout.addTab(tabLayout.newTab().setText("לפי מרחק"));

        } else {
            tabLayout.addTab(tabLayout.newTab().setText("Events"));
            tabLayout.addTab(tabLayout.newTab().setText("Saved Events"));
            tabLayout.addTab(tabLayout.newTab().setText("By Distance"));

        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final CustomerMainActivityTabPagerAdapter adapter = new CustomerMainActivityTabPagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(2);// set hwo many fragments will be kept in memory and will not destroy when swipe out from it

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        OnClickMainCityButtonPerFragment((ClickCaller) (viewPager.getAdapter().instantiateItem(viewPager, 0)));// enable onclick over cityMenu button of first fragment

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                OnClickMainCityButtonPerFragment((ClickCaller) (viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem()))); // send the onclick to the coorect Fragment that is is currently visible
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        notification = (ImageView) findViewById(R.id.main_frgamnet_notification_item);
        notification.setOnClickListener(this);

        search = (ImageView) findViewById(R.id.main_fragment_search);
        search.setOnClickListener(this);

        builder = new AlertDialog.Builder(this);
        // dialog that popup if NO gps after 2 minutes that user logged
        builder = new AlertDialog.Builder(context);

        BottomNavigationBar = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        BootomNavigationBarOnSelect();

        gpsMessageHandler = new Handler();
        gpsMessageHandler.postDelayed(gpsRunnable, 300000);// 26.09 assaf - check each 5 minutes from the moment this page uploaded-- if no GPS location then show a message

      //  startService(new Intent(this, checkMessageUnreadChats.class)); // 01.03 - new one
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void OnClickMainCityButtonPerFragment(final ClickCaller fragment){

        mainCityFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerFragmentsMainActivity.MainCityButtonClick mainCityButtonClick = new CustomerFragmentsMainActivity().new MainCityButtonClick(fragment);
                mainCityButtonClick.callFragment();
            }
        });
    }


    public void openFilterPage(View v) {
        Intent filterPageIntent = new Intent(this, FilterPageActivity.class);
        startActivity(filterPageIntent);
    }

    public void openMenuPage(View v) {
        Intent menuPageIntent = new Intent(this, MenuActivity.class);
        startActivity(menuPageIntent);
    }

    @Override
    public void onClick(View v) {
        Intent newIntent = null;
        if (v.getId() == search.getId()) {
            newIntent = new Intent(this, SearchActivity.class);
            startActivity(newIntent);
        } else if (v.getId() == notification.getId()) {
            newIntent = new Intent(this, MyNotificationsActivity.class);
            startActivity(newIntent);
        }
    }

        @Override
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    try {
                        GlobalVariables.deepLink_params = referringParams.getString("objectId");
                        for (int i = 0; i < filtered_events_data.size(); i++) {
                            if (GlobalVariables.deepLink_params.equals(filtered_events_data.get(i).getParseObjectId())) {
                                Intent intent = new Intent(context, EventPageActivity.class);
                                Bundle b = new Bundle();
                                EventDataMethods.onEventItemClick(i, GlobalVariables.ALL_EVENTS_DATA, intent);
                                intent.putExtras(b);
                                context.startActivity(intent);
                                i = filtered_events_data.size();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, this.getIntent().getData(), this);
    }

   @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }


   private void registerDialog () {
       if (GlobalVariables.CUSTOMER_PHONE_NUM == null || GlobalVariables.CUSTOMER_PHONE_NUM.equals("")) {
           //dialog that popup for Guest only for register to Application
           final Dialog dialog = new Dialog(this);
           dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
           dialog.setContentView(R.layout.login_dialog);
           dialog.setCancelable(false);
           dialog.getWindow().setLayout(350, 380);
           final Button sBut = (Button) dialog.findViewById(R.id.skipButton);
           final ImageButton fBut = (ImageButton) dialog.findViewById(R.id.loginFB);
           final ImageButton createBut = (ImageButton) dialog.findViewById(R.id.createAccount);


           View.OnClickListener onClickListener = new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if (v.getId() == sBut.getId()) {
                       dialog.dismiss();
                   } else if (v.getId() == fBut.getId()) {
                       dialog.dismiss();//26.09 assaf fixed t close when login to FB
                       Intent intent = new Intent(CustomerFragmentsMainActivity.this, ActivityFacebook.class);
                       intent.putExtra("cameFrom","mainMenu");
                       startActivity(intent);

                   } else if (v.getId() == createBut.getId()) {
                       dialog.dismiss();
                       Intent intent = new Intent(context, SmsSignUpActivity.class);
                       startActivity(intent);
                   }
               }
           };
           sBut.setOnClickListener(onClickListener);
           createBut.setOnClickListener(onClickListener);
           fBut.setOnClickListener(onClickListener);

           dialog.show();
       }
   }



    /**
     * customerLogin() method from LoginActivity for guest and registered users
     */
    public void customerLogin() {

        if (GlobalVariables.CUSTOMER_PHONE_NUM == null || GlobalVariables.CUSTOMER_PHONE_NUM.equals("")) {
            GlobalVariables.IS_CUSTOMER_REGISTERED_USER = false;
            GlobalVariables.IS_CUSTOMER_GUEST = true;
            GlobalVariables.CUSTOMER_PHONE_NUM = "";
        } else {
            GlobalVariables.IS_CUSTOMER_GUEST = false;
            GlobalVariables.IS_CUSTOMER_REGISTERED_USER = true;
            ParseQuery<Profile> query = ParseQuery.getQuery("Profile");
            query.whereEqualTo("number", GlobalVariables.CUSTOMER_PHONE_NUM);
            query.setLimit(100000);
            query.findInBackground(new FindCallback<Profile>() {
                @Override
                public void done(List<Profile> objects, ParseException e) {
                    if (e == null) {
                        if (objects.get(0).getChanels() != null) {
                            if (GlobalVariables.userChanels.size() == 0) {
                                GlobalVariables.userChanels.addAll(objects.get(0).getChanels());
                            }
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.addAll("Channels", (Collection<?>) GlobalVariables.userChanels);
                            installation.saveInBackground();
                            for (int i = 0; i < GlobalVariables.userChanels.size(); i++) {
                                ParsePush.subscribeInBackground("a" + GlobalVariables.userChanels.get(i));
                            }
                        }
                    } else {
                        e.printStackTrace();
                    }
                }

            });
        }
        GlobalVariables.IS_PRODUCER = false;
        GlobalVariables.PRODUCER_PARSE_OBJECT_ID = null;
        GlobalVariables.ALL_EVENTS_DATA.clear();

    }

    Runnable gpsRunnable = new Runnable() { //Alert in case that NO GPS connection
        @Override
        public void run() {

            if ((GlobalVariables.MY_LOCATION == null || !GPSMethods.isLocationEnabled(context)) && GlobalVariables.IS_PRODUCER == false) {
                try {
                    builder.setMessage(getString(R.string.no_gps));
                    builder.setCancelable(true);
                    builder.setNeutralButton(R.string.gotIt,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };


    @Override
    public void gpsCallback() {
        //do nothing
    }

    @Override
    public void eventDataCallback() {
        //do nothing
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            _sharedPref = getSharedPreferences("filterInfo", MODE_PRIVATE); // 24.09- assaf: to Edit the Shared P. and delete the price and date form filter
            _sharedPref.edit().putString("date", "").commit();// 24.09- assaf:
            _sharedPref.edit().putString("price", "").commit();// 24.09- assaf:
            _sharedPref.edit().putString("dateFrom", "").commit();// 18.10- assaf:
            _sharedPref.edit().putString("dateTo", "").commit();// 18.10- assaf:

            //Save Filters Choice in Parse for Future use. if not empty or null

            if (GlobalVariables.CURRENT_FILTER_NAME != null && !GlobalVariables.CURRENT_FILTER_NAME.isEmpty()) {
                ParseQuery<Profile> query = ParseQuery.getQuery("Profile");
                query.whereEqualTo("number", GlobalVariables.CUSTOMER_PHONE_NUM);
                query.setLimit(100000);
                try {
                    List<Profile> listNumbers = query.find();
                    for (Profile userProfile : listNumbers) {
                        userProfile.put("prefferedTopics", GlobalVariables.CURRENT_FILTER_NAME);
                        userProfile.save();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            cleanDeviceCacheOnDestroy(); //28.01 - clean device background

            System.runFinalization();

            int currentApiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) { //chekc that API version greater then 15
                finishAffinity();
            } else {
                finish();
            }
            System.exit(0);     // for kill  background Threads that operate the Endless loop of present the push messages
        } catch (Exception ex) {
            Log.e(ex.getMessage(), "onDestroy exception");
        }
    }



    private void cleanDeviceCacheOnDestroy() { //Delete App cache directoy and send the sub directory to deleteDir - 28.01 - assaf

        // Add file with content
        try {
            List <File> cacheType = new ArrayList<>();
            cacheType.add(context.getCacheDir());
            cacheType.add(context.getExternalCacheDir());

            for(File cache :cacheType) {
                if (cache != null && cache.exists()) {
                    String[] children = cache.list();
                    for (String filePath : children) {
                        if (!filePath.equals("lib")) {
                            deleteDir(new File(cache, filePath));
                            Log.i("TAG", "**************** File ..." + filePath + " DELETED *******************");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) { // delete the files in the cache directory

        try {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dir.delete();
    }

    @Override
    public void onBackPressed() {//prevent the back Button to the Activities that sent intents to the Main Activity
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure_you_want_to_exit)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CustomerFragmentsMainActivity.this.finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public class MainCityButtonClick // assaf - This class and Interface help with determine which Fragment is filtered by Click the MainButtonCity filter
    {
        ClickCaller fragment;

        public MainCityButtonClick (ClickCaller fragmentContext)
        {
            fragment = fragmentContext;
        }

        public void callFragment() {

            fragment.GetMenuCityButtonClick();
        }
    }

    public interface ClickCaller
    {
        void GetMenuCityButtonClick();
    }

   private void BootomNavigationBarOnSelect()
    {
       BottomNavigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(MenuItem item) {
               Fragment selectedFragemnt = null;
               Intent intentSelected = null;
               switch (item.getItemId()){
                   case R.id.action_item1:
                        intentSelected = new Intent(CustomerFragmentsMainActivity.this,CustomerMessageConversationsListActivity.class);
                        startActivity(intentSelected);
                       break;
                   case R.id.action_item2:
                       intentSelected = new Intent(CustomerFragmentsMainActivity.this,CreateEventActivity.class);
                       startActivity(intentSelected);
                     break;
                   case R.id.action_item3:
                       intentSelected = new Intent(CustomerFragmentsMainActivity.this,MyEventsTicketsActivity.class);
                       startActivity(intentSelected);
                       break;
               }
               return true;
           }
       });
    }

}




