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

import com.example.FundigoApp.Events.CreateEventActivity;
import com.example.FundigoApp.Producer.TabPagerAdapter;
import com.example.FundigoApp.R;

import java.util.Locale;

public class ProducerActivity extends AppCompatActivity {

public static int dialogCounter; //for present the progress dialog only once when statiscics page loaded
public static ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView (R.layout.producer_avtivity_main);
        TabLayout tabLayout = (TabLayout) findViewById (R.id.tab_layout);
        if (Locale.getDefault ().getDisplayLanguage ().equals ("עברית")) {
            tabLayout.addTab (tabLayout.newTab ().setText ("אמנים"));
            tabLayout.addTab (tabLayout.newTab ().setText ("מידע"));
        } else {
            tabLayout.addTab (tabLayout.newTab ().setText ("Artist"));
            tabLayout.addTab (tabLayout.newTab ().setText ("State"));
        }
        dialogCounter =0;
        tabLayout.setTabGravity (TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById (R.id.pager);
        final TabPagerAdapter adapter = new TabPagerAdapter
                (getSupportFragmentManager (), tabLayout.getTabCount ());
        viewPager.setAdapter (adapter);
        viewPager.addOnPageChangeListener (new TabLayout.TabLayoutOnPageChangeListener (tabLayout));
        tabLayout.setOnTabSelectedListener (new TabLayout.OnTabSelectedListener () {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1 && dialogCounter == 0) {
                    dialog = new ProgressDialog(ProducerActivity.this);
                    dialog.setMessage("Loading...");
                    dialog.show();
                }
                viewPager.setCurrentItem (tab.getPosition ());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    dialogCounter++;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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
