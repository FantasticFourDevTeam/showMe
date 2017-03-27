package com.example.FundigoApp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.FundigoApp.Customer.RealTime.RealTimeActivity;
import com.example.FundigoApp.Customer.SavedEvents.SavedEventActivity;

public class CustomerMainActivityTabPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public CustomerMainActivityTabPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super (fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        try {
            switch (position) {
                case 0:
                    MainActivity tab1 = new MainActivity();
                    return tab1;
                case 1:
                    SavedEventActivity tab2 = new SavedEventActivity();
                    return tab2;
               case 2:
                    RealTimeActivity tab3 = new RealTimeActivity();
                    return tab3;
                default:
                    return null;
            }
        }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        return null;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}