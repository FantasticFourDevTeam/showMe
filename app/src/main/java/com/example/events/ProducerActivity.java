package com.example.events;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.FundigoApp.Events.CreateEventActivity;
import com.example.FundigoApp.Producer.Artists.QR_producer;
import com.example.FundigoApp.Producer.TabPagerAdapter;
import com.example.FundigoApp.R;

import java.util.Locale;

public class ProducerActivity extends AppCompatActivity {

public static int dialogCounter; //for present the progress dialog only once when statiscics page loaded
public static ProgressDialog dialog;
private static ImageView qr_scan_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView (R.layout.producer_avtivity_main);
        TabLayout tabLayout = (TabLayout) findViewById (R.id.tab_layout);
        if (Locale.getDefault ().getDisplayLanguage ().equals ("עברית")) {
            tabLayout.addTab (tabLayout.newTab ().setText ("אומנים"));
            tabLayout.addTab (tabLayout.newTab ().setText ("ללא אומן"));
            tabLayout.addTab (tabLayout.newTab ().setText ("מדדי אירועים"));
        } else {
            tabLayout.addTab (tabLayout.newTab ().setText ("Artists"));
            tabLayout.addTab(tabLayout.newTab().setText("No Artist"));//12.10 assaf
            tabLayout.addTab (tabLayout.newTab().setText("Events State"));
        }
        dialogCounter =0;
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);//12.10 assaf
        tabLayout.setTabMode(TabLayout.MODE_FIXED);//12.10 assaf
        final ViewPager viewPager = (ViewPager) findViewById (R.id.pager);
        final TabPagerAdapter adapter = new TabPagerAdapter
                (getSupportFragmentManager (), tabLayout.getTabCount ());
        viewPager.setAdapter (adapter);
        viewPager.addOnPageChangeListener (new TabLayout.TabLayoutOnPageChangeListener (tabLayout));
        viewPager.setOffscreenPageLimit(2);//assaf: 13.10: keep the fragments in Memory and prevent refresh the page each time swipe to it , it takes time
       tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 2 && dialogCounter == 2) {
                    dialog = new ProgressDialog(ProducerActivity.this);
                    dialog.setMessage("Loading...");
                    dialog.show();
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1 || tab.getPosition() == 0 ) {
                    dialogCounter++;
                }
               }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        qr_scan_icon =(ImageView)findViewById (R.id._qr_scan_producer);
        qr_scan_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QR_producer.class);
                startActivity(intent);
            }
        });
    }

    public void createEvent(View view) {
        Intent intent = new Intent (this, CreateEventActivity.class);
        intent.putExtra("create", "true");
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {//prevent the back Button to the Activities that sent intents to the Main Activity
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setMessage (R.string.producer_are_you_sure_you_want_to_exit)
                .setCancelable (false)
                .setPositiveButton ("Yes", new DialogInterface.OnClickListener () {
                    public void onClick(DialogInterface dialog, int id) {
                        ProducerActivity.this.finish ();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create ();
        alert.show();
    }


}
