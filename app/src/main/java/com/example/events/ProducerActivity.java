package com.example.events;

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

        tabLayout.setTabGravity (TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById (R.id.pager);
        final TabPagerAdapter adapter = new TabPagerAdapter
                (getSupportFragmentManager (), tabLayout.getTabCount ());
        viewPager.setAdapter (adapter);
        viewPager.addOnPageChangeListener (new TabLayout.TabLayoutOnPageChangeListener (tabLayout));
        tabLayout.setOnTabSelectedListener (new TabLayout.OnTabSelectedListener () {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem (tab.getPosition ());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public void createEvent(View view) {
        Intent intent = new Intent (this, CreateEventActivity.class);
        intent.putExtra ("create", "true");
        startActivity (intent);
    }

}
