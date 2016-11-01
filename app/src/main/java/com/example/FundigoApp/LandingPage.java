package com.example.FundigoApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class LandingPage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        launchMain();
       }


    private void launchMain()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new android.content.Intent(LandingPage.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000);
     }

//    if (SystemClock.elapsedRealtime() - mLastClickTime < 12000) {// prevent double clicks on ticket buy
//        Toast.makeText(EventPageActivity.this, "Last operation is still in process, please wait", Toast.LENGTH_SHORT).show();
//        return;
//    }
//    else {
//        mLastClickTime = SystemClock.elapsedRealtime();
//    }

}