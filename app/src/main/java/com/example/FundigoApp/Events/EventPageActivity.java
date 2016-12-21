package com.example.FundigoApp.Events;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Chat.ChatActivity;
import com.example.FundigoApp.Chat.MessagesRoomProducerActivity;
import com.example.FundigoApp.Chat.RealTimeChatActivity;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.Producer.ProducerSendPuchActivity;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.GPSMethods;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;
import com.example.FundigoApp.Tickets.EventsSeats;
import com.example.FundigoApp.Tickets.SelectSeatActivity;
import com.example.FundigoApp.Tickets.WebBrowserActivity;
import com.example.FundigoApp.Verifications.SmsSignUpActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class EventPageActivity extends Activity implements View.OnClickListener {
    //save for customer, push for producer
    ImageView saveOrPushBotton;
    private ImageView iv_share;
    private ImageView iv_chat;
    Button getTicketsButton;
    Intent intent;
    private static final int REQUEST_IMAGE_CAPTURE = 1; //code for picture taking by device camera
    private String date;
    private String eventName;
    private String eventPlace;
    private String driving;
    private String walking;
    private boolean walkNdrive = false;
    private int walkValue = -1;
    EventInfo eventInfo;
    String i = "";
    private static Button eventPicsUpload;
    private static Button eventPicsView;
	long mLastClickTime=0;
    private String faceBookUrl;
    ImageLoader loader;
    private static Button findLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_event_page);
        getTicketsButton = (Button) findViewById (R.id.button);

        intent = getIntent ();
        eventInfo = GlobalVariables.ALL_EVENTS_DATA.get
                                                            (intent.getIntExtra ("index", 0));

        Date currentDate = new Date ();
        Date eventDate = eventInfo.getDate ();
        eventInfo.setIsFutureEvent (eventDate.after (currentDate));
        saveOrPushBotton = (ImageView) findViewById (R.id.imageEvenetPageView3);
        eventPicsUpload = (Button) findViewById(R.id.uploadPics);//04.11 assaf updated to upload pics of events by prodcuer
        eventPicsView = (Button) findViewById(R.id.viewPics);
        findLocation = (Button) findViewById(R.id.findLocation);
        findLocation.setOnClickListener(this);

        if (!GlobalVariables.IS_PRODUCER) //29.09 - support free and fix logic
        {
			if(eventInfo.getIsCanceled())
            {
                getTicketsButton.setText("Event Canceled");
                getTicketsButton.setClickable(false);
            }
            else if(eventInfo.getNumOfTickets()==0)// in case by mistake someone filled 0 mainly in case of Free events
            {
                getTicketsButton.setText(getString(R.string.no_tickets));
                getTicketsButton.setClickable(false);
            }
            else if (eventInfo.getPrice().equals("FREE") && eventInfo.getNumOfTickets() > 0 && eventInfo.isFutureEvent()) { //29.09 - assaf to suport free events
                //getTicketsButton.setText("Free Event");
                 getTicketsButton.setText(getString(R.string.register)); //09.08 - Assaf changed for the option to register also fr Free events
                getTicketsButton.setClickable(true);
                checkIfTicketsLeft();
                //getTicketsButton.setClickable (false);
            } else if (eventInfo.getPrice().equals("FREE") && eventInfo.getNumOfTickets() < 0 && eventInfo.isFutureEvent()) {
                //getTicketsButton.setText("Free Event");
                getTicketsButton.setText(getString(R.string.schedule)); //29.09 - Assaf changed for the option to register also fr Free events
                getTicketsButton.setClickable(true);
            } else if (!GlobalVariables.IS_PRODUCER && !eventInfo.isFutureEvent()) {
                getTicketsButton.setText(getString(R.string.event_expiration));
                getTicketsButton.setClickable(false);
            } else {
                if (eventInfo.isFutureEvent()&& !(eventInfo.getPrice().equals("FREE") && eventInfo.getNumOfTickets() < 0)) {
                    //29.09 -assaf - check if tickets left excpet case that Free event and no limited place
                    checkIfTicketsLeft();
                }
            }

            eventPicsView.setVisibility(View.VISIBLE); // 18.12 - assaf - view pictures uploaded by producer
            eventPicsView.setOnClickListener(this);
        }

        else if (GlobalVariables.IS_PRODUCER) {

            getTicketsButton.setText (this.getString (R.string.tickets_status));//09.08 - Assaf changed for the option to register also for Free events
            saveOrPushBotton.setImageResource(R.drawable.ic_micro_send_push_frame);
            eventPicsUpload.setVisibility(View.VISIBLE);
            eventPicsUpload.setOnClickListener(this);
          }
        faceBookUrl = intent.getStringExtra ("fbUrl");//get link from the Intent
        GlobalVariables.deepLinkEventObjID = "";
        GlobalVariables.deepLink_params = "";
        ImageView event_image = (ImageView) findViewById (R.id.eventPage_image);
        loader = FileAndImageMethods.getImageLoader (this);
        loader.displayImage (eventInfo.getPicUrl(), event_image);
        date = eventInfo.getDateAsString();
        TextView event_date = (TextView) findViewById (R.id.eventPage_date);
        event_date.setText (date);
        eventName = intent.getStringExtra ("eventName");
        i = getIntent ().getStringExtra ("i");
        TextView event_name = (TextView) findViewById (R.id.eventPage_name);
        event_name.setText (eventName);
        String eventTags = intent.getStringExtra ("eventTags");
        TextView event_tags = (TextView) findViewById (R.id.eventPage_tags);
        event_tags.setText (eventTags);
        String eventPrice = intent.getStringExtra ("eventPrice");
        TextView event_price = (TextView) findViewById (R.id.priceEventPage);
        if (GlobalVariables.IS_PRODUCER) {
            event_price.setText (getString(R.string.edit_event));
        } else {
            if (!eventInfo.getPrice().equals("FREE"))
                event_price.setText (EventDataMethods.getDisplayedEventPrice (eventPrice));
            else{
                event_price.setText (getString(R.string.free));
            }
        }
        String eventDescription = intent.getStringExtra ("eventInfo");
        TextView event_info = (TextView) findViewById (R.id.eventInfoEventPage);
        event_info.setText (eventDescription);
        eventPlace = intent.getStringExtra ("eventPlace");
        TextView event_place = (TextView) findViewById (R.id.eventPage_location);
        event_place.setText (eventPlace + ", " + eventInfo.getAddress ());
        iv_share = (ImageView) findViewById (R.id.imageEvenetPageView2);
        iv_share.setOnClickListener (this);
        iv_chat = (ImageView) findViewById (R.id.imageEvenetPageView5);

        iv_chat.setOnClickListener (this);

        ImageView imageEvenetPageView4 = (ImageView) findViewById (R.id.imageEvenetPageView4);
        imageEvenetPageView4.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent (EventPageActivity.this, EventServiceActivity.class);
                intent2.putExtra ("toilet", intent.getStringExtra ("toilet"));
                intent2.putExtra ("parking", intent.getStringExtra ("parking"));
                intent2.putExtra ("capacity", intent.getStringExtra ("capacity"));
                intent2.putExtra ("atm", intent.getStringExtra ("atm"));
                intent2.putExtra ("driving", driving);
                intent2.putExtra ("walking", walking);
                intent2.putExtra ("walkValue", walkValue);
                intent2.putExtra ("artist", eventInfo.getArtist ());
                startActivity (intent2);
            }
        });
        checkIfChangeColorToSaveButtton ();
        String even_addr = eventInfo.getAddress ();
        even_addr = even_addr.replace (",", "");
        even_addr = even_addr.replace (" ", "+");
        if (GlobalVariables.MY_LOCATION != null && GPSMethods.isLocationEnabled (this)) {
            new GetEventDis2 (EventPageActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                            getLocation2().getLatitude() +
                            "," +
                            getLocation2().getLongitude() +
                            "&destinations=" +
                            even_addr +
                            "+Israel&mode=driving&language=" + Locale.getDefault().getLanguage() + "&key=AIzaSyAuwajpG7_lKGFWModvUIoMqn3vvr9CMyc","Driving");
            new GetEventDis2 (EventPageActivity.this).executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR,
                                                                      "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                                                                              getLocation2 ().getLatitude () +
                                                                              "," +
                                                                              getLocation2 ().getLongitude () +
                                                                              "&destinations=" +
                                                                              even_addr +
                                                                              "+Israel&mode=walking&language=" + Locale.getDefault ().getLanguage () + "&key=AIzaSyAuwajpG7_lKGFWModvUIoMqn3vvr9CMyc","Walking");
        }
    }

    public void openTicketsPage(View view) {
		
        if (!GlobalVariables.IS_PRODUCER) {

            if (SystemClock.elapsedRealtime() - mLastClickTime < 12000) {// prevent double clicks on ticket buy
                Toast.makeText(EventPageActivity.this,"Last operation is still in process, please wait",Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                mLastClickTime = SystemClock.elapsedRealtime();
            }

            if (GlobalVariables.IS_CUSTOMER_GUEST) {
                dialogForGuestToRegister (); // in case of Guest
            } else {
                if (eventInfo.isStadium) {
                    Bundle b = new Bundle ();
                    Intent intentSeat = new Intent (EventPageActivity.this, SelectSeatActivity.class);
                    intentSeat.putExtras (b);
                    intentSeat.putExtra ("eventPrice", eventInfo.getPrice ());
                    intentSeat.putExtra ("eventName", eventInfo.getName ());
                    intentSeat.putExtra ("phone", GlobalVariables.CUSTOMER_PHONE_NUM);
                    intentSeat.getStringExtra ("eventPrice");
                    intentSeat.putExtra ("eventObjectId", eventInfo.getParseObjectId ());
                    startActivity (intentSeat);
                } else {
                    if (!eventInfo.getPrice().equals("FREE")) { //Save a Seat and buy ticket is only for events that not free
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 3; i++) {
                                    Toast.makeText(getApplicationContext(),
                                            "You Have 20 Minutes to complete the purchase, Otherwise the ticket will be available to all again",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 0);
                    }
                    Intent intentPelePay = new Intent (EventPageActivity.this, WebBrowserActivity.class);
                    intentPelePay.putExtra ("eventObjectId", eventInfo.getParseObjectId ());
                    intentPelePay.putExtra ("isChoose", "no");
                    intentPelePay.putExtra("eventPrice", eventInfo.getPrice());
                    intentPelePay.putExtra("eventNumOfTickets", eventInfo.getNumOfTickets());
                    startActivity(intentPelePay);
                }

                //29.09 - assaf Sceduel The event date
                AlertDialog.Builder _builder = new AlertDialog.Builder(this);
                final int i = this.intent.getIntExtra ("index", 0);
                _builder.setPositiveButton(R.string.save_to_calander, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            saveToCalendar (i);
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
                _builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dialog.dismiss();
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                })
                        .setCancelable(true);
                AlertDialog _alert = _builder.create();
                _alert.show();
                ////
           }
        } else {
            Intent intent = new Intent (EventPageActivity.this, EventStatusActivity.class);
            intent.putExtra ("name", getIntent ().getStringExtra ("eventName"));
            intent.putExtra ("eventObjectId", eventInfo.getParseObjectId ());
            startActivity (intent);
        }
	}

    private void loadMessagesPageProducer() {
        Intent intent = new Intent (this, MessagesRoomProducerActivity.class);
        intent.putExtra ("index", eventInfo.getIndexInFullList ());
        startActivity (intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId ()) {
            case R.id.imageEvenetPageView2:
                //Assaf: Dialog option currently was marked and the option to share open directly a deep link dialog
                //Once we wil add more options to share it will be back

              //  AlertDialog.Builder builder = new AlertDialog.Builder (this);
                //       builder.setCancelable(false)
                  //      .setPositiveButton (this.getString (R.string.share_app_page), new DialogInterface.OnClickListener () {
                    //        public void onClick(DialogInterface dialog, int id) {
                               shareDeepLink ();
                        //    }
                       // })
                    //    .setNegativeButton (this.getString (R.string.share_web_page), new DialogInterface.OnClickListener () {
                  //          public void onClick(DialogInterface dialog, int id) {
                                // Assaf: OPEN THE EVENT FACEBBOK PAGE IF EXIST . THE PAGE STORED IN PARSE
                                //THISD OPTION NEED TO be replaced by Open the Event Web Page and not Facebook

//                                Intent webIntent;
//                                if (faceBookUrl != "" && faceBookUrl != null) {
//                                    try {
//                                        getPackageManager ().getPackageInfo ("com.facebook.katana", 0);
//                                        webIntent = new Intent (Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + faceBookUrl));
//                                        startActivity (webIntent);
//                                    } catch (Exception e) {
//                                        Log.e (e.toString (), "Open link to FaceBook App is fail, sending to Browser");
//                                        try {
//                                            webIntent = new Intent (Intent.ACTION_VIEW, Uri.parse (faceBookUrl));
//                                            startActivity (webIntent);
//                                        } catch (Exception e1) {
//                                            Log.e (e1.toString (), "Open link to FaceBook Browser is fail");
//                                        }
//                                    }
//                                } else
//                                    Toast.makeText (EventPageActivity.this, "No FaceBook Page to Present", Toast.LENGTH_SHORT).show ();
//                            }
//                        })
//                        .setCancelable (true);
//                AlertDialog alert = builder.create ();
//                alert.show ();
                break;
            case R.id.imageEvenetPageView3:
                if (GlobalVariables.IS_PRODUCER) {
                    Intent pushIntent = new Intent (EventPageActivity.this, ProducerSendPuchActivity.class);
                    pushIntent.putExtra ("id", eventInfo.getParseObjectId ());
                    startActivity (pushIntent);
                } else {
                    handleSaveEventClicked (this.intent.getIntExtra ("index", 0));
                }
                break;
            case R.id.imageEvenetPageView5:
                AlertDialog.Builder builder2 = new AlertDialog.Builder (this);

                if (!GlobalVariables.IS_PRODUCER) {
                    builder2.setTitle (this.getString (R.string.you_can_get_more_info_about_the_event));
                    builder2.setPositiveButton (this.getString (R.string.Send_message_to_producer), listener);
                } else {
                    builder2.setTitle (this.getString (R.string.you_can_get_more_info_about_the_event_1));
                    builder2.setPositiveButton (this.getString (R.string.see_customers_massages), listener);
                }
                builder2.setNegativeButton (this.getString (R.string.real_time_chat), listener);
                builder2.setNeutralButton (this.getString (R.string.cancel), listener);
                AlertDialog dialog = builder2.create ();
                dialog.show ();
                TextView messageText = (TextView) dialog.findViewById (android.R.id.message);
                messageText.setGravity (Gravity.CENTER);
                break;

            case R.id.uploadPics:
                picturesTakeAndUpload();
                break;

            case R.id.viewPics:
                viewPictures();
                break;
            case R.id.findLocation:
                navigateToEventLocation();
                break;
        }
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener () {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intentToSend;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
                        intentToSend = new Intent (EventPageActivity.this, ChatActivity.class);
                        intentToSend.putExtra ("index", intent.getIntExtra ("index", 0));
                        intentToSend.putExtra ("customer_phone", GlobalVariables.CUSTOMER_PHONE_NUM);
                        startActivity (intentToSend);
                    } else if (GlobalVariables.IS_PRODUCER) {
                        loadMessagesPageProducer ();
                    } else if (GlobalVariables.IS_CUSTOMER_GUEST) {
                        dialogForGuestToRegister (); // in case of Guest
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if (GlobalVariables.IS_CUSTOMER_GUEST) {
                        dialogForGuestToRegister (); //in case of Guest
                    } else {
                        intentToSend = new Intent (EventPageActivity.this, RealTimeChatActivity.class);
                        intentToSend.putExtra ("eventName", eventName);
                        intentToSend.putExtra ("eventObjectId", eventInfo.getParseObjectId ());
                        intentToSend.putExtra ("index", intent.getIntExtra ("index", 0));
                        startActivity (intentToSend);
                    }
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    dialog.dismiss ();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//05.11 - assaf added pictiures add to event by Prodcuer
        IntentResult scan = null;
        if (data != null && requestCode != REQUEST_IMAGE_CAPTURE) {
            scan = IntentIntegrator.parseActivityResult(requestCode,
                    resultCode,
                    data);

            if (scan != null) {
                String result = scan.getContents();
                String objectId = result.substring(13, 23);
                Toast.makeText(EventPageActivity.this, "" + scan.getFormatName() + " " + scan.getContents() + " ObjectId is " + objectId, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(EventPageActivity.this, R.string.scan_didnt_finish, Toast.LENGTH_SHORT).show();
            }
        }
        if (data != null && requestCode == GlobalVariables.REQUEST_CODE_MY_PICK && requestCode != REQUEST_IMAGE_CAPTURE) {
            GeneralStaticMethods.onActivityResult(requestCode,
                    data,
                    this);
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {// camera get picture result
            //Bundle extras = data.getExtras();
            //Bitmap picBitmap = (Bitmap) extras.get("data");
            Bitmap picBitmap = FileAndImageMethods.getImageFromDevice(data,this.getApplicationContext());
            try {
                if (picBitmap!= null)
                    saveImageToParse(picBitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(),getString(R.string.upload_picture_failure),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkIfChangeColorToSaveButtton() {
        if (!GlobalVariables.IS_PRODUCER) {
            int index = intent.getIntExtra("index", 0);
            if (GlobalVariables.ALL_EVENTS_DATA.get (index).getIsSaved ())
                saveOrPushBotton.setImageResource (R.mipmap.whsavedd);
            else {
                saveOrPushBotton.setImageResource (R.mipmap.wh);
            }
        }
    }

    public void handleSaveEventClicked(int index) {
        EventInfo event = GlobalVariables.ALL_EVENTS_DATA.get (index);
        GeneralStaticMethods.handleSaveEventClicked(event,
                saveOrPushBotton,
                this.getApplicationContext(),
                R.mipmap.whsavedd,
                R.mipmap.wh);
        final int i = index; //Assaf added: call to calander for savifn the Event
        boolean IsNotSaved = event.getIsSaved();
        if (IsNotSaved) {// only if user want to save event (event is unsaved) , calendar open
            AlertDialog.Builder _builder = new AlertDialog.Builder(this);
            _builder.setPositiveButton(R.string.save_to_calander, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        saveToCalendar(i);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
            _builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                         dialog.dismiss();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            })
                    .setCancelable(true);
            AlertDialog _alert = _builder.create();
            _alert.show();
        }
    }

    public boolean dialogForGuestToRegister() {
        //Assaf:show dialog in case  Guest want to Chat
        final AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setMessage (getString(R.string.please_register))
                .setCancelable (true)
                .setNeutralButton (getString(R.string.register), new DialogInterface.OnClickListener () {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent smsRegister = new Intent (EventPageActivity.this, SmsSignUpActivity.class);
                        startActivity (smsRegister);
                    }
                });

        builder.setPositiveButton (getString(R.string.cancel), new DialogInterface.OnClickListener () {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel ();
            }
        });
        AlertDialog smsAlert = builder.create ();
        smsAlert.show ();
        return true;
    }

    public Location getLocation2() {
        LocationManager locationManager = (LocationManager) this.getSystemService (Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation (LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                return lastKnownLocationGPS;
            } else {
                if (ActivityCompat.checkSelfPermission (this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                Location loc = locationManager.getLastKnownLocation (LocationManager.PASSIVE_PROVIDER);
                return loc;
            }
        } else {
            return null;
        }
    }

    private class GetEventDis2 extends AsyncTask<String, Integer, String> {
        String jsonStr;
        String drivingDuration="";
        String walkingDuration="";
        boolean toLongToWalk = false;

        public GetEventDis2(EventPageActivity eventPageActivity) {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL (params[0]);
                String durationMeasureFor = params[1]; //23.10 - assaf - measure walking or driving
                HttpURLConnection con = (HttpURLConnection) url.openConnection ();
                con.setRequestMethod ("GET");
                con.connect ();
                if (con.getResponseCode () == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader (new InputStreamReader (con.getInputStream ()));
                    StringBuilder sr = new StringBuilder ();
                    String line = "";
                    while ((line = br.readLine ()) != null) {
                        sr.append (line);
                    }
                    jsonStr = sr.toString ();
                    parseJSON (jsonStr,durationMeasureFor);
                } else {
                }
            } catch (MalformedURLException e) {
                e.printStackTrace ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String re) { // 23.10 assaf updated the methods . walking and dring were mixed. it is running two unsafety threads
         //   if (!walkNdrive) {
            if (drivingDuration!=""){
                driving = drivingDuration; // drivign duration
                walkNdrive = true;
            } else if (walkingDuration!="" && walkValue !=-1) {
                if (!toLongToWalk) {
                    walking = walkingDuration;//walking duration
                    walkNdrive = false;
                    toLongToWalk = false;
                }
            }
        }

        public void parseJSON(String jsonStr,String durationMeasureFor) {
            try {
                JSONObject obj = new JSONObject (jsonStr);
                if (durationMeasureFor.equals("Walking"))
                {
                    walkingDuration = obj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").get("text").toString();//walking string value text
                    walkValue = (int) obj.getJSONArray ("rows").getJSONObject (0).getJSONArray ("elements").getJSONObject (0).getJSONObject ("duration").get ("value"); // walking int duration
                }
                else if (durationMeasureFor.equals("Driving")) {
                    drivingDuration = obj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").get("text").toString(); //driving duration
                }
              } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void shareDeepLink() {

        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject ()
                                                              .setCanonicalIdentifier ("item/1234")
                                                              .setTitle(eventInfo.getName() + " is amazing event, one of your friends shared it through WhoGO App")
                                                              .setContentDescription(this.getString(R.string.my_content_description))
                                                              .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                                                              .setContentImageUrl(eventInfo.getPicUrl())
                                                              .addContentMetadata("objectId", eventInfo.getParseObjectId());

        final io.branch.referral.util.LinkProperties linkProperties = new LinkProperties ()
                                                                         .setChannel("My Application")
                                                                         .setFeature("sharing");



        ShareSheetStyle shareSheetStyle = new ShareSheetStyle (EventPageActivity.this, "", eventInfo.getName() + " is amazing event join it by using WhoGO App")
                .setCopyUrlStyle(getResources().getDrawable(android.R.drawable.ic_menu_send), "Copy Link", "Copied to clipboard")
                .setMoreOptionStyle(getResources().getDrawable(android.R.drawable.ic_menu_search), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP);

        branchUniversalObject.showShareSheet(this,
                   linkProperties,
                   shareSheetStyle,
                   new Branch.BranchLinkShareListener() {
                       @Override
                       public void onShareLinkDialogLaunched() {
                       }

                       @Override
                       public void onShareLinkDialogDismissed() {
                       }

                       @Override
                       public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
                       }

                       @Override
                       public void onChannelSelected(String channelName) {

                       }
                   });


        branchUniversalObject.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                   // Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage() + "", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void editEvent(View view) {
        if (GlobalVariables.IS_PRODUCER) {

            DialogInterface.OnClickListener listenerEdit = new DialogInterface.OnClickListener () { //01.12 - assaf -added dialog before Edit
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent (EventPageActivity.this, EditEventActivity.class);
                            intent.putExtra(GlobalVariables.OBJECTID, eventInfo.getParseObjectId ());
                            startActivity(intent);
                            GlobalVariables.refreshArtistsList = true;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss ();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL: // open Push notifications to users before event delete
                            Intent intentPush = new Intent(EventPageActivity.this, ProducerSendPuchActivity.class);
                            intentPush.putExtra("id",eventInfo.getParseObjectId ());
                            startActivity(intentPush);
                            break;
                    }
                }
            };
            AlertDialog.Builder builderEdit = new AlertDialog.Builder (this);
            builderEdit.setIcon(R.drawable.warning);
            builderEdit.setMessage(getString(R.string.are_you_sure_edit_event));
            builderEdit.setPositiveButton(getString(R.string.yes), listenerEdit);
            builderEdit.setNegativeButton(getString(R.string.no), listenerEdit);
            builderEdit.setNeutralButton(getString(R.string.send_push), listenerEdit);
            AlertDialog dialogEdit = builderEdit.create ();
            dialogEdit.show();
//
//            Intent intent = new Intent (this, EditEventActivity.class);
//            intent.putExtra (GlobalVariables.OBJECTID, eventInfo.getParseObjectId ());
//            startActivity (intent);
        }
    }

    public void checkIfTicketsLeft() {
        ParseQuery<EventsSeats> query = ParseQuery.getQuery ("EventsSeats");
        // Retrieve the object by id
        query.whereEqualTo ("eventObjectId", eventInfo.getParseObjectId ()).whereEqualTo ("sold", true);
        query.findInBackground (new FindCallback<EventsSeats> () {
            public void done(List<EventsSeats> eventsSeatsList, ParseException e) {
                if (e == null) {
                    if (eventsSeatsList.size () >= eventInfo.getNumOfTickets ()) {
                        getTicketsButton.setText (getString(R.string.no_tickets));
                        getTicketsButton.setClickable (false);
                    }
                } else {
                    e.printStackTrace ();
                }
            }
        });
    }

    private void saveToCalendar(int eventId) { //Assaf: intent to open calander diaplog and save event detailes
        try {
            EventInfo event = GlobalVariables.ALL_EVENTS_DATA.get (eventId);
            Date date = event.getDate();
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(date);
            Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginTime.getTimeInMillis()+1000*3600*2);// event length is 2 hours
            intent.putExtra(CalendarContract.Events.TITLE, event.getName());
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getAddress());
            intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(ex.getMessage(), "save in Calendar was failed");
        }
    }

    private void picturesTakeAndUpload() { //05.11 - assaf - take pictures and upload to app
        PackageManager packageManager = this.getPackageManager();
       // if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            try {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
       // }
    }

    private void saveImageToParse(Bitmap eventPicture) { // save event picture to Parse , evnets pictirses table with pointer to Event
        int MAX_IMAGES = 4;
        if (checkNumberOfImages() <MAX_IMAGES) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (eventPicture != null) {
                eventPicture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] eventPicData = stream.toByteArray();
                try {
                    final ParseFile pictureFile = new ParseFile("picture.jpeg", eventPicData);
                    pictureFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            ParseObject eventsPictures = new ParseObject("EventMultiMedia");
                            eventsPictures.put("eventPointer", ParseObject.createWithoutData("Event", eventInfo.getParseObjectId()));
                            eventsPictures.put("MultiMedia", pictureFile);
                            eventsPictures.saveInBackground();
                        }
                    });
                    Toast.makeText(this, getString(R.string.upload_picture) + " " + String.valueOf(MAX_IMAGES), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, getString(R.string.upload_picture_failure), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            Toast.makeText(this, getString(R.string.max_images) + " " + String.valueOf(MAX_IMAGES), Toast.LENGTH_SHORT).show();
        }
    }

      private void viewPictures()
      {
          Intent intent = new Intent(this, EventPicturesGridView.class);
          intent.putExtra("eventID", eventInfo.getParseObjectId());
          startActivity(intent);
      }

    private int checkNumberOfImages()
    {
        int NumOfImagesInParse =0;
        try {
            ParseQuery innerQuery = new ParseQuery("Event");
            innerQuery.whereEqualTo("objectId", eventInfo.getParseObjectId());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("EventMultiMedia");
            query.whereMatchesQuery("eventPointer", innerQuery);
            List<ParseObject> list = query.find();
            NumOfImagesInParse = list.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
       return NumOfImagesInParse;
    }

    private void navigateToEventLocation()
    {
		// final String url = String.format("waze://?ll=%f,%f&navigate=yes", eventInfo.getX(),eventInfo.getY());
		// final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		//  startActivity(intent);
		try 
		{
			String uri = "http://maps.google.com/maps?q=loc:" + eventInfo.getX() + "," + eventInfo.getY() + " (" + eventInfo.getAddress() + ")";
			Intent navigateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			navigateIntent.setData(Uri.parse(uri));
			startActivity(navigateIntent);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
    }
	
   
}
