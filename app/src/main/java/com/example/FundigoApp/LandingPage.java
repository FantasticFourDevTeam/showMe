package com.example.FundigoApp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LandingPage extends AppCompatActivity {


    //01.01 Assaf - changed form OnCreate until the End of Class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LoadPage Load = new LoadPage();
        Load.execute();
    }


    private class LoadPage extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new android.content.Intent(LandingPage.this, MainActivity.class);
            startActivity(intent);
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("success")) {
                finish();
            }
        }

   }
}