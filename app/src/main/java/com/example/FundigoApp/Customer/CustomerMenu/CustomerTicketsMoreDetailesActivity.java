package com.example.FundigoApp.Customer.CustomerMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.example.FundigoApp.Tickets.EventsSeatsInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomerTicketsMoreDetailesActivity extends AppCompatActivity {

    private Intent intent;
    private TextView purchaseTv;
    private TextView eventLinkTv;
    private ImageView qrImg;
    private EventsSeatsInfo eventsSeatsInfo;
    Context context;
    ImageLoader loader;
    private String eventName;
    private Date eventDate;
    private String emailTo;
    private static Button sendTicketToEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets_more_detailes);
        intent = getIntent ();
        purchaseTv = (TextView) findViewById (R.id.dateRow);
        qrImg = (ImageView) findViewById (R.id.qrTicketImage);
        eventLinkTv = (TextView) findViewById (R.id.linkEventRow);
        sendTicketToEmail = (Button) findViewById(R.id.sendTicketToMail);
        context = this;
        loader = FileAndImageMethods.getImageLoader(context); //09.10 assaf
        sendTicketToEmail.setOnClickListener(sendTicketMailClick);
        getIntentData ();
    }

    public void getIntentData() {
        //Get data from Intent sent by EventTickets

        final int index = intent.getIntExtra("index", -1);
        eventsSeatsInfo = MyEventsTicketsActivity.my_tickets_list.get(index);
        eventDate = eventsSeatsInfo.getEventInfo().getDate();
        eventName = eventsSeatsInfo.getEventInfo().getName();
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

    Button.OnClickListener sendTicketMailClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThread(GlobalVariables.CUSTOMER_PHONE_NUM);
                emailTo = customerDetails.getEmail();
                sendTicketParseMail(v);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };

    public void sendTicketParseMail(View view) { // send mail by using Parse Cloud function
        String ticketNameToEmail = "";
        try {
            if (eventsSeatsInfo.getTicketName() != "undefined" && eventsSeatsInfo.getTicketName() != null) {
                String ticketName = eventsSeatsInfo.getTicketName().trim();

                if (!ticketName.isEmpty()) {
                    ticketNameToEmail = "Seat Name: " + ticketName + '\n';
                }
            }

            String Body = "Purchase date: " + eventsSeatsInfo.getPurchaseDate() + '\n' +
                    "Event Name: " + eventName + '\n' +
                    "Event Date: " + eventDate + '\n' +
                    "Price: " + eventsSeatsInfo.getPrice() + '\n' +
                    ticketNameToEmail;

            String qrCodeString = eventsSeatsInfo.getQRPath();

            Map<String, Object> params = new HashMap<>();
            params.put("text", Body);
            params.put("qrCode", qrCodeString);//QR URL
            params.put("subject", eventName + " Information");
            params.put("toEmail", emailTo);


        ParseCloud.callFunctionInBackground("sendEmailToUser", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object response, ParseException exc) {
                Log.e("send mail response", "response: " + response);
                if (exc!=null)
                    exc.printStackTrace();
                if (response.equals("Email sent!")) {
                    Toast.makeText(context, "Your ticket was sent to: " + emailTo, Toast.LENGTH_LONG).show();
                }
            }
        });
       }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Toast.makeText(this, "Mail was not sent due to error", Toast.LENGTH_SHORT).show();
        }
    }
}
