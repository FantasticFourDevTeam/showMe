package com.example.FundigoApp.Events;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.Tickets.EventsSeats;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity {
    ImageView iv_qr;
    TextView tv_buyer;
    TextView tv_date;
    TextView tv_price;
    TextView tv_ticket_name_details;
    EventsSeats eventsSeats;
    Bitmap bmp;
    private ProgressDialog dialog;
    private Intent callingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_event_details);
        dialog = new ProgressDialog (this);
        dialog.setMessage ("Loading...");
        dialog.show();

        iv_qr = (ImageView) findViewById (R.id.iv_qr_details);
        tv_buyer = (TextView) findViewById (R.id.buyer_details);
        tv_date = (TextView) findViewById (R.id.date_details);
        tv_price = (TextView) findViewById (R.id.price_details);
        tv_ticket_name_details = (TextView) findViewById (R.id.ticket_name_details);
        callingIntent = getIntent ();

        getData(callingIntent.getStringExtra(GlobalVariables.OBJECTID));
    }

    private void setAll() {

      Date date;
      try {
            ParseObject soldTickets = eventsSeats.getSoldTicketsPointer();

        if (eventsSeats.getSeatNumber() != null && !eventsSeats.getSeatNumber().equals("")) {
            tv_ticket_name_details.setText(eventsSeats.getSeatNumber());
        } else {
            tv_ticket_name_details.setText(getString(R.string.Registered_seats)+ " - " + eventsSeats.getCustomerPhone());
        }

        if (eventsSeats.getCustomerPhone() != null) {
            tv_buyer.setText(eventsSeats.getCustomerPhone());
        }

        if (soldTickets!=null) { // Events with saved seats
             // date = eventsSeats.getPurchaseDate();
            date = eventsSeats.getUpdatedAt();//29.09 - Assaf fixed
        }
        else // for free events
        {
            date = eventsSeats.getUpdatedAt();
        }
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            tv_date.setText(format.format(cal.getTime()));

          tv_price.setText(eventsSeats.getPrice() + "₪");

        dialog.dismiss();
    }
//        catch (ParseException ex)
//        {
//            ex.printStackTrace();
//        }
        catch (Exception ex1){
            ex1.printStackTrace();
        }
    }

    private void getData(String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventsSeats");

        query.getInBackground(id, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    eventsSeats = (EventsSeats) object;
                    ParseFile file = (ParseFile) object.get("QR_Code");
                    if (file == null) {
                        tv_price.setText(eventsSeats.getPrice() + "₪");
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } else {
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    if (data.length != 0) {
                                        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        iv_qr.setVisibility(View.VISIBLE);
                                        iv_qr.setImageBitmap(bmp);
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    } else {
                                        tv_price.setText(eventsSeats.getPrice() + "₪");
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }

                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    e.printStackTrace();
                }
                setAll();
            }
        });
    }

}
