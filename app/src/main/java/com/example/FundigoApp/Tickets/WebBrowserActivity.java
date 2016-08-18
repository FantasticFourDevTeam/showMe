package com.example.FundigoApp.Tickets;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.FundigoApp.GlobalVariables;
import com.parse.ParseException;

public class WebBrowserActivity extends AppCompatActivity {
    private String amount;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        amount = i.getStringExtra("eventPrice");

        String isSeats = i.getStringExtra("isChoose");
        if (isSeats.equals("no")) {
            String eventObjectId = i.getStringExtra("eventObjectId");
            EventsSeats eventsSeats = new EventsSeats();
            if(!amount.equals("FREE")) {
                eventsSeats.put("price", Integer.parseInt(amount));
                eventsSeats.setIsSold(false);
            }
            else{
                eventsSeats.put("price", 0);
                eventsSeats.setIsSold(true);
            }
            eventsSeats.put("eventObjectId", eventObjectId);
            eventsSeats.setCustomerPhone(GlobalVariables.CUSTOMER_PHONE_NUM);
            try {
                eventsSeats.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            orderId = eventsSeats.getObjectId();
        } else {
            orderId = i.getStringExtra("seatParseObjId");
        }

        if (!amount.equals("FREE")) {

            MyWebView view = new MyWebView(this);
            view.getSettings().setJavaScriptEnabled(true);
            view.getSettings().setDomStorageEnabled(true);
            view.getSettings().setLoadWithOverviewMode(true);
            view.getSettings().setUseWideViewPort(true);
            //view.loadUrl ("https://akimbotest.parseapp.com/");
            view.loadUrl("https://www.pelepay.co.il/pay/paypage.aspx?description=fundigo&business=nesizagury@gmail.com?orderid=" + orderId + "&amount=" + amount);

            view.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView v, String url) {
                    return false;
                }

//            @Override
//            public void onPageFinished(WebView v, String url) {
//                v.loadUrl ("javascript:" +
//                                   "var y = document.getElementsByName('amount')[0].value='" + amount + "';" +
//                                   "var x = document.getElementsByName('orderid')[0].value='" + orderId + "';");
//
//            }
            });
            setContentView(view);
        }
        else
        {
            Toast.makeText(this,"You are successfully registered to the Event",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class MyWebView extends WebView {
        Context context;

        public MyWebView(Context context) {
            super (context);
            this.context = context;
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
