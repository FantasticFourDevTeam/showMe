package com.example.FundigoApp.Customer.CustomerMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.Tickets.EventsSeatsInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomerTicketsMoreDetailesActivity extends AppCompatActivity {

    private Intent intent;
    private TextView purchaseTv;
    private TextView eventLinkTv;
    private ImageView qrImg;
    private EventsSeatsInfo eventsSeatsInfo;
    Context context;
    ImageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_tickets_more_detailes);
        intent = getIntent ();
        purchaseTv = (TextView) findViewById (R.id.dateRow);
        qrImg = (ImageView) findViewById (R.id.qrTicketImage);
        eventLinkTv = (TextView) findViewById (R.id.linkEventRow);
        context = this;
        loader = FileAndImageMethods.getImageLoader(context); //09.10 assaf
        getIntentData ();
    }

    public void getIntentData() {
        //Get data from Intent sent by EventTickets

        final int index = intent.getIntExtra("index", -1);
        eventsSeatsInfo = MyEventsTicketsActivity.my_tickets_list.get(index);
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd/M/yyyy hh:mm a", Locale.getDefault());
        if (eventsSeatsInfo.getSoldTickets() != null){
            purchaseTv.setText(dateFormat.format(eventsSeatsInfo.getSoldTickets().getUpdatedAt()));// actaully purchase ticket time
           }
        else {
            purchaseTv.setText("No Information");
        }
       // qrImg.setImageBitmap (eventsSeatsInfo.getQR ());
        if (eventsSeatsInfo.getQRPath()!= null && eventsSeatsInfo.getQRPath()!= "")
            loader.displayImage(eventsSeatsInfo.getQRPath(),qrImg); // 09.10 load QR path
        //Intent for Presnet the Event when click the link
        eventLinkTv.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                try {
                    Bundle b = new Bundle ();
                    Intent intent = new Intent (context, EventPageActivity.class);
                    EventDataMethods.onEventItemClick (index, MyEventsTicketsActivity.my_tickets_events_list, intent);
                    intent.putExtras (b);
                    startActivity (intent);
                } catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        });
    }
}
