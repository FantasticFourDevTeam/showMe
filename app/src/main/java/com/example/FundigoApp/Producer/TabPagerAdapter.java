package com.example.FundigoApp.Producer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.FundigoApp.Producer.Artists.ProducerNoArtistEventsActivity;
import com.example.FundigoApp.Producer.Artists.ProducerMainActivity;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public TabPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super (fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ProducerMainActivity tab1 = new ProducerMainActivity ();
                return tab1;
            case 1:
                ProducerNoArtistEventsActivity tab2 = new ProducerNoArtistEventsActivity();
                return tab2;
            case 2:
                AllEventsStats tab3 = new AllEventsStats ();
                return tab3;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}