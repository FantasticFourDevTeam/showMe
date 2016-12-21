package com.example.FundigoApp.Events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.Tickets.TicketsPriceActivity;
import com.google.gson.Gson;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class CreateEventActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "CreateEventActivity";
    private static HashMap<String,String> addressPerLanguage = new HashMap<>();
    private static HashMap<String,String> cityPerLanguage = new HashMap<>();

    TextView tv_create;
    TextView tv_price;
    TextView tv_name;
    TextView tv_artist;
    TextView tv_description;
    EditText et_name;
    EditText et_artist;
    EditText et_description;
    EditText et_price;
    EditText et_quantity;
    EditText et_address;
    EditText et_place;
    EditText et_capacity;
    EditText et_parking;
    EditText et_tags;
    Button btn_validate_address;
    ImageView iv_val_add;
    Button btn_next;
    Button btn_next1;
    Button btn_next2;
    Button btn_pic;
    ImageView pic;
    Button btn_price_details;
    ScrollView create_event2;
    ScrollView create_event3;
    LinearLayout ll_name;
    LinearLayout ll_date;
    LinearLayout ll_artist;
    LinearLayout ll_description;
    private static final int SELECT_PICTURE = 1;
    private boolean pictureSelected = false;
    private boolean address_ok = false;
    Gson gson;
    Result result;
    String address;
    private String valid_address;
    private double lat;
    private double lng;
    private String city;
    private String region1="";
    private String region2="";
    private Button btn_date;
    private TextView tv_date_new;
    private String date="";
    int year;
    int monthOfYear;
    int dayOfMonth;
    private boolean timeOk = false;
    private Date realDate;
    private boolean freeEvent = false;
    private CheckBox freeBox;
    private TextView tv_quantity;
    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    String[] FILTERS;
    String[] ATMS;
    String[] TOILETS;
    private MaterialSpinner filterSpinner;
    private String filter;
    private MaterialSpinner atmSpinner;
    private String atmStatus = "";
    private MaterialSpinner toiletSpinner;
    private MaterialSpinner handicapToiletSpinner;
    private String numOfToilets = "";
    private String numOfHandicapToilets = "";
    private String eventObjectId;
    int blueIncome;
    int pinkIncome;
    int greenIncome;
    int orangeIncome;
    int yellowIncome;
    int totalIncome;
    private boolean seats = false;
    private LinearLayout linearLayout;
    private CheckBox checkBoxPrice;
    SharedPreferences sp;
    Boolean ISOpened = false; // prevent timepicker open twice
	long mLastClickTime=0;
    int IMAGE_MAX_SIZE = 650;
    private static Bitmap image;
    private Boolean toSaveEvent = false; // 17.11 assaf - check if vdlaidation pass before save event to Parse


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //29.09 - Assaf remove seates price from shared P.
        sp.edit().putInt(GlobalVariables.YELLOW, -1).apply();
        sp.edit().putInt(GlobalVariables.PINK, -1).apply();
        sp.edit().putInt(GlobalVariables.BLUE, -1).apply();
        sp.edit().putInt(GlobalVariables.GREEN, -1).apply();
        sp.edit().putInt(GlobalVariables.ORANGE,-1).apply();
        componentInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        seats = sp.getBoolean(GlobalVariables.SEATS, false);
		//condition for after share event and save. this condition return to list of producer event
        if(SHARE)
        {
            SHARE = false;
            finish();
        }

        //for debugging
        Log.i ("prodCretae" , "ID" + GlobalVariables.PRODUCER_PARSE_OBJECT_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next: // NOT Relevant Button was remarked
                if (timeOk) {
                  //  showSecondStage();
                } else {
                    Toast.makeText(CreateEventActivity.this, getString(R.string.enter_valid_date), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_next1:  // NOT Relevant Button was remarked
                if (freeEvent) { //29.09 -Assaf - quantity is not mandatory for Free event
                     if (address_ok && et_place.length()!= 0)
                    {
                       // showThirdStage(); 16.11 assaf - not in use anymore
                    } else {
                        Toast.makeText(CreateEventActivity.this, R.string.please_enter_valid_address, Toast.LENGTH_SHORT).show();
                    }

                    if (!validateQuantity()) {
                        Toast.makeText(CreateEventActivity.this, getString(R.string.enter_valid_quantity), Toast.LENGTH_SHORT).show();
                    }
                }
                if (seats) {
                    if (!validateQuantity()) {
                        Toast.makeText(CreateEventActivity.this, getString(R.string.enter_valid_quantity), Toast.LENGTH_SHORT).show();
                    }else if (address_ok) {
                       // showThirdStage(); 16.11 assaf - not in use anymore
                    } else {
                        Toast.makeText(CreateEventActivity.this, R.string.please_enter_valid_address, Toast.LENGTH_SHORT).show();
                    }
                } else if (!seats && !freeEvent) {
                    if (!validatePrice() || !validateQuantity()) {
                        Toast.makeText(CreateEventActivity.this,  getString(R.string.enter_valid_quantity_price), Toast.LENGTH_SHORT).show();
                    } else {
                        if (address_ok) {
                          //  showThirdStage();  16.11 assaf - not in use anymore
                        } else {
                            Toast.makeText(CreateEventActivity.this, R.string.please_enter_valid_address, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.btn_validate_address: // SAVE Event Button
                validateAddress();
                break;
            case R.id.btn_next2:
                if (validateBeforeSaveEvent()) {
                    if (filter != null && filter!="") {   // 16.11 - assaf added for validation of Information before save event in Parse
                        seats = sp.getBoolean(GlobalVariables.SEATS, false);
                        saveEvent();
                    } else {
                        Toast.makeText(CreateEventActivity.this, getString(R.string.choose_a_filter), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_pic:
                uploadPic();
                break;
            case R.id.btn_date:
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                datePickerDialog = new DatePickerDialog(this, listener, year, month, day);
                ISOpened = false;
                datePickerDialog.show();
                break;
            case R.id.btn_price_details:
                Intent intent = new Intent(this, TicketsPriceActivity.class);
                startActivity(intent);
                break;
			case R.id.btn_pickContact:
                saveAndShare();
                break;
        }
    }

    public boolean validatePrice() {
        String str = et_price.getText().toString();
        if (str.equals("0")) {
            return false;
        }
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean validateQuantity() {
        String str = et_quantity.getText().toString();
        if (str.equals("")&& freeEvent)
            return true;
        try {
            if (Integer.parseInt(str) <= 0) {
                return false;
            }
            else if (Integer.parseInt(str) >0)
               return true;
          }
        catch(Exception ex)
          {
            ex.printStackTrace();
              return false;
          }
        return false;
    }

    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int y, int m, int d) {
            year = y;
            monthOfYear = m;
            dayOfMonth = d;
            date = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
            tv_date_new.setText(date);
            tv_date_new.setVisibility(View.VISIBLE);
            //assaf added to prevent open timepicker twice
            if (!ISOpened) {
                timePickerDialog = new TimePickerDialog(CreateEventActivity.this, timeListener, 12, 12, true);
                ISOpened = true;
                timePickerDialog.show();
            }
        }
    };

    TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String min;
            if (minute < 10) {
                min = "0" + minute;
            } else {
                min = "" + minute;
            }
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            realDate = new Date(cal.getTimeInMillis());

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                Toast.makeText(CreateEventActivity.this, getString(R.string.enter_valid_date), Toast.LENGTH_LONG).show();
                timeOk = false;
                ISOpened = false;
            } else {
                timeOk = true;
            }
        }
    };


    private void uploadPic() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
         //14.10 assaf changed this method , to use Static one
        //excpetions
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {

            try //29.09 add try catch
            {
                image = FileAndImageMethods.getImageFromDevice(data,getApplicationContext()); //15.10 assaf
                pic.setImageBitmap(image);
                pic.setVisibility(View.VISIBLE);
                pictureSelected = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (OutOfMemoryError err) {
                err.printStackTrace();
            }
        }
    }

 //    private void showSecondStage() { //16.11 not in use any more
//        if (et_name.length() != 0 && date.length() != 0 && et_description.length() != 0) {
//            tv_create.setVisibility(View.GONE);
//            ll_name.setVisibility(View.GONE);
//            ll_date.setVisibility(View.GONE);
//            ll_artist.setVisibility(View.GONE);
//            ll_description.setVisibility(View.GONE);
//            btn_next.setVisibility(View.GONE);
//            create_event2.setVisibility(View.VISIBLE);
//        } else {
//            Toast.makeText(CreateEventActivity.this, R.string.please_fill_empty_forms, Toast.LENGTH_SHORT).show();
//        }
//    }

    private void validateAddress() {
        address = et_address.getText().toString();

        try {
            if (!address.isEmpty()) {
                iv_val_add.setVisibility(View.INVISIBLE);
                new ValidateAddress().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, GlobalVariables.GEO_API_ADDRESS);
            } else {
                Toast.makeText(CreateEventActivity.this, getString(R.string.Event_Address_is_empty), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Toast.makeText(CreateEventActivity.this, getString(R.string.validation_failed), Toast.LENGTH_SHORT).show();
        }
    }

//    private void showThirdStage() { // assaf - 16.11 - not in use anymore
//        if (freeEvent && address_ok && et_place.length() != 0) {//29.09 assaf updated to support free event with no place limit
//            create_event2.setVisibility(View.GONE);
//            create_event3.setVisibility(View.VISIBLE);
//        } else if (seats) {
//            if (address_ok && et_place.length() != 0) {
//                create_event2.setVisibility(View.GONE);
//                create_event3.setVisibility(View.VISIBLE);
//            } else {
//                Toast.makeText(CreateEventActivity.this, R.string.please_fill_empty_forms, Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            if (et_quantity.length() != 0 && et_price.length() != 0 && address_ok && et_place.length() != 0) {
//                create_event2.setVisibility(View.GONE);
//                create_event3.setVisibility(View.VISIBLE);
//            } else {
//                Toast.makeText(CreateEventActivity.this, R.string.please_fill_empty_forms, Toast.LENGTH_SHORT).show();
//            }
//        }
 //   }

 
    public void saveAndShare()
    {
        SHARE = true;
        saveEvent();
    }

    public void saveEvent() {
        final Event event = new Event();
        event.setName(et_name.getText().toString());
        event.setDescription(et_description.getText().toString());

		if (SystemClock.elapsedRealtime() - mLastClickTime < 10000) {// prevent double clicks on save event
            return;
        }
        else {
         try
            {
            mLastClickTime = SystemClock.elapsedRealtime();


            event.setAddress(valid_address);
            event.setAddressPerLanguage(addressPerLanguage); //assaf - 28.10 save address per Lnagauge as an object in Parse
            event.setCity(city);
            event.setCityPerLanguage(cityPerLanguage);//assaf - 28.10 save city name per Lanagauge as an object in Parse
            event.setX(lat);
            event.setY(lng);
            //===========================Setting tags the right way==============
            StringBuilder stringBuilder = new StringBuilder();
            if (et_tags.length() == 0) {
                event.setTags("#" + filter);
            } else {
                stringBuilder.append("#" + filter);
                String str = et_tags.getText().toString();
                str = str.replaceAll(",", " ");
                str = str.replaceAll("#", "");
                String[] arr = str.split(" ");

                for (String ss : arr) {
                    if (!ss.equals(" ") && !ss.equals("")) {
                        stringBuilder.append(" #" + ss);
                    }
                }
                String finalString = stringBuilder.toString();
                // finalString.replaceAll("# ","");
                event.setTags(finalString);

            }
            //===================================================================
            if (seats) {
                blueIncome = 4 * sp.getInt(GlobalVariables.BLUE, 0);
                orangeIncome = 17 * sp.getInt(GlobalVariables.ORANGE, 0);
                pinkIncome = 17 * sp.getInt(GlobalVariables.PINK, 0);
                pinkIncome = pinkIncome + 16 * sp.getInt(GlobalVariables.PINK, 0);
                yellowIncome = 17 * sp.getInt(GlobalVariables.YELLOW, 0);
                yellowIncome = yellowIncome + 16 * sp.getInt(GlobalVariables.YELLOW, 0);
                greenIncome = 7 * sp.getInt(GlobalVariables.GREEN, 0);
                greenIncome = greenIncome + 7 * sp.getInt(GlobalVariables.GREEN, 0);
                totalIncome = pinkIncome + yellowIncome + greenIncome + blueIncome + orangeIncome;

            } else {
                if (!freeEvent) {
                    totalIncome = Integer.parseInt(et_price.getText().toString()) * Integer.parseInt(et_quantity.getText().toString());
                } else {
                    totalIncome = 0;
                }
            }
            event.setFilterName(filter);
            event.setProducerId(GlobalVariables.PRODUCER_PARSE_OBJECT_ID);
            event.setRealDate(realDate);
            event.setPlace(et_place.getText().toString());
            event.setArtist(et_artist.getText().toString());
            event.setEventToiletService(numOfToilets + ", Handicapped " + numOfHandicapToilets);
            String eventParkingService;
            if (et_parking.getText().toString().equals("")) {
                eventParkingService = "";
            } else {
                eventParkingService = "Up To " + et_parking.getText().toString();
            }
            event.setEventParkingService(eventParkingService);
            String eventCapacityService;
            if (et_capacity.getText().toString().equals("")) {
                eventCapacityService = "";
            } else {
                eventCapacityService = "Up To " + et_capacity.getText().toString();
            }
            event.setEventCapacityService(eventCapacityService);
            event.setEventATMService(atmStatus);

        if (pictureSelected) {

            if (freeEvent) {
                event.setPrice("FREE"); // 29.09- assaf to support free events without minimal quantity
                if(et_quantity.length() ==0) {
                    et_quantity.setText("-1"); // -1 means no minimal quantitiy- free entry to the free event
                }
                event.setNumOfTickets(Integer.parseInt(et_quantity.getText().toString()));
                //event.setNumOfTickets (99999);  Assaf removed the hardcoded amount oכ tickets
            } else if (!seats) {
                event.setNumOfTickets(Integer.parseInt(et_quantity.getText().toString()));
                event.setPrice(et_price.getText().toString());
            } else if (seats) {
                List<Integer> sum = new ArrayList<>();
                sum.add(sp.getInt(GlobalVariables.ORANGE, 0));
                sum.add(sp.getInt(GlobalVariables.PINK, 0));
                sum.add(sp.getInt(GlobalVariables.BLUE, 0));
                sum.add(sp.getInt(GlobalVariables.YELLOW, 0));
                sum.add(sp.getInt(GlobalVariables.GREEN, 0));
                int max = Collections.max(sum);
                int min = Collections.min(sum);
                event.setPrice("" + min + "-" + max + "");
                //event.setNumOfTickets (101);// Assaf removed the hardcoded amount of tickets
                event.setNumOfTickets(Integer.parseInt(et_quantity.getText().toString()));
            }

            Bitmap bitmap = image; //15.10 assaf changed
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] image = stream.toByteArray();
            ParseFile file = new ParseFile("picturePath", image);
            try {
                file.saveInBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
            event.put("ImageFile", file);
                if (seats) {
                    event.setIsStadium(true);
                } else {
                    event.setIsStadium(false);
                }
                totalIncome = 0;
                if (!freeEvent) {
                    eventObjectId = event.getObjectId();
                    if (seats) {
                        saveTicketsPrice(eventObjectId);
                        //the producer did not chose colored seats
                    } else {
                        totalIncome = Integer.parseInt(et_price.getText().toString()) * Integer.parseInt(et_quantity.getText().toString());
                    }
                    Snackbar snackbar = Snackbar
                            .make(linearLayout, getString(R.string.future_income) + totalIncome, Snackbar.LENGTH_LONG)
                            .setAction("", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //deleteEvent(eventObjectId); 01.12 - assaf - cancled thsi option
                                 }
                            });
                    snackbar.setActionTextColor(Color.YELLOW);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.DKGRAY);
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                }
                event.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            GlobalVariables.refreshArtistsList = true;
                            Toast.makeText(CreateEventActivity.this, R.string.event_has_created_successfully, Toast.LENGTH_LONG).show();//TODO
                            if(SHARE)
                            {
                                shareDeepLink(et_name.getText().toString(),file.getUrl(),event.getObjectId());//benjamin add
                            }
                            else
                            {
                                finish();
                            }
                        } else {
                            Toast.makeText(CreateEventActivity.this, R.string.event_hasnot_created_successfully, Toast.LENGTH_LONG).show();//TODO
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this,getString(R.string.upload_a_picture),Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            e.printStackTrace();
             Toast.makeText(CreateEventActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
	}
    }

    /*public void deleteEvent(final String objectId) { 01.12 - assaf cnacled this option
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereEqualTo("objectId", objectId);
        query.orderByDescending("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        object.delete();
                        Log.e(TAG, "Event deleted");
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                        Log.e(TAG, "Event not deleted " + e1.toString());
                    }
                    object.saveInBackground();
                }
            }
        });
        if (seats) {
            ParseQuery<ParseObject> querySeats = ParseQuery.getQuery("EventsSeats");
            querySeats.whereEqualTo("eventObjectId", objectId);
            querySeats.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (objects.size() != 0) {
                        ParseObject.deleteAllInBackground(objects);

                    }
                }
            });
            //query and delete again because there is one last ticket left
            ParseQuery<ParseObject> querySeats1 = ParseQuery.getQuery("EventsSeats");
            querySeats1.whereEqualTo("eventObjectId", objectId);
            querySeats1.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        try {

                            object.delete();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "" + e.toString());
                    }
                }
            });
            //===================================================================
        }
        finish();
    }*/

    @Override
    public void onBackPressed() {
        if (timePickerDialog != null && timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }
        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }

        if (et_name.getVisibility() == View.VISIBLE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.are_you_sure_you_want_to_exit)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CreateEventActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void componentInit() {
        linearLayout = (LinearLayout) findViewById(R.id.create_profile_layout);
        tv_create = (TextView) findViewById(R.id.tv_create);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_artist = (TextView) findViewById(R.id.tv_address);
        tv_description = (TextView) findViewById(R.id.tv_description);
        tv_quantity = (TextView) findViewById(R.id.tv_quantity);
        tv_price = (TextView) findViewById(R.id.tv_price);
        et_name = (EditText) findViewById(R.id.et_name);
        et_artist = (EditText) findViewById(R.id.et_artist);
        et_description = (EditText) findViewById(R.id.et_description);
        et_price = (EditText) findViewById(R.id.et_price);
        et_quantity = (EditText) findViewById(R.id.et_quantity);
        et_address = (EditText) findViewById(R.id.et_address);
        et_place = (EditText) findViewById(R.id.et_place);
        et_capacity = (EditText) findViewById(R.id.et_capacity);
        et_parking = (EditText) findViewById(R.id.et_parking);
        et_tags = (EditText) findViewById(R.id.et_tags);
        btn_validate_address = (Button) findViewById(R.id.btn_validate_address);
        iv_val_add = (ImageView) findViewById(R.id.iv_val_add);
        freeBox = (CheckBox) findViewById(R.id.checkBoxFree);
        checkBoxPrice = (CheckBox) findViewById(R.id.checkBoxPrice);
        tv_date_new = (TextView) findViewById(R.id.tv_date_new);
        btn_date = (Button) findViewById(R.id.btn_date);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next1 = (Button) findViewById(R.id.btn_next1);
        btn_next2 = (Button) findViewById(R.id.btn_next2);
        btn_pic = (Button) findViewById(R.id.btn_pic);
        pic = (ImageView) findViewById(R.id.pic);
        btn_next.setOnClickListener(this);
        btn_next1.setOnClickListener(this);
        btn_next2.setOnClickListener(this);
        btn_pic.setOnClickListener(this);
        btn_validate_address.setOnClickListener(this);
        btn_date.setOnClickListener(this);
        freeBox.setOnCheckedChangeListener(this);
        checkBoxPrice.setOnCheckedChangeListener(this);
        create_event2 = (ScrollView) findViewById(R.id.create_event2);
        create_event3 = (ScrollView) findViewById(R.id.create_event3);
        ll_name = (LinearLayout) findViewById(R.id.ll_name);
        ll_date = (LinearLayout) findViewById(R.id.ll_date);
        ll_artist = (LinearLayout) findViewById(R.id.ll_artist);
        ll_description = (LinearLayout) findViewById(R.id.ll_description);
        btn_price_details = (Button) findViewById(R.id.btn_price_details);
        btn_price_details.setOnClickListener(this);

//===============================Filter Spinner stuff==================================
        FILTERS = getResources().getStringArray(R.array.filters);
        ArrayAdapter<String> filterSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FILTERS);
        filterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner = (MaterialSpinner) findViewById(R.id.filterSpinner);
        filterSpinner = (MaterialSpinner) findViewById(R.id.filterSpinner);
        filterSpinner.setAdapter(filterSpinnerAdapter);
        filterSpinner.setOnItemSelectedListener(this);
//=============================================================================

// ===============================ATM Spinner  stuff==================================
        ATMS = getResources().getStringArray(R.array.atms);
        ArrayAdapter<String> atmSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ATMS);
        atmSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        atmSpinner = (MaterialSpinner) findViewById(R.id.atmSpinner);
        atmStatus = ATMS[0];
        atmSpinner.setAdapter(atmSpinnerAdapter);
        atmSpinner.setOnItemSelectedListener(this);
//==============================================================================
// ===============================Toilet Spinner  stuff==================================
        TOILETS = getResources().getStringArray(R.array.toilets);
        ArrayAdapter<String> toiletSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TOILETS);
        toiletSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toiletSpinner = (MaterialSpinner) findViewById(R.id.toiletSpinner);
        numOfToilets = TOILETS[11];
        toiletSpinner.setAdapter(toiletSpinnerAdapter);
        toiletSpinner.setOnItemSelectedListener(this);
//==============================================================================
// ===============================handicapToilet Spinner  stuff==================================
        handicapToiletSpinner = (MaterialSpinner) findViewById(R.id.handicapToiletSpinner);
        handicapToiletSpinner.setAdapter(toiletSpinnerAdapter);
        numOfHandicapToilets = TOILETS[11];
        handicapToiletSpinner.setOnItemSelectedListener(this);
//==============================================================================
    }

    /**
     * FREE CHECKBOX LISTENER:
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        switch (buttonView.getId()) {
            case R.id.checkBoxFree:
                if (isChecked) {
                    freeEvent = true;
                    et_quantity.setVisibility(View.VISIBLE);
                    tv_quantity.setVisibility(View.VISIBLE);
                    tv_price.setVisibility(View.GONE);
                    et_price.setVisibility(View.GONE);
                    btn_price_details.setVisibility(View.GONE);
                    checkBoxPrice.setVisibility(View.GONE);
                    et_quantity.setHint(getString(R.string.limited_free_seats)); //29.09 assaf added
                } else {
                    freeEvent = false;
                    et_quantity.setVisibility(View.VISIBLE);
                    tv_quantity.setVisibility(View.VISIBLE);
                    tv_price.setVisibility(View.VISIBLE);
                    et_price.setVisibility(View.VISIBLE);
                    et_quantity.setHint(""); //29.09 assaf added

                    btn_price_details.setVisibility(View.GONE);
                    //   }
                   // checkBoxPrice.setVisibility(View.VISIBLE); //Assaf 16.11 - for now ticket per Price was hidden
                    // need to think about this feature
                }
                break;
            case R.id.checkBoxPrice: // assaf - 16.11 this option of ticket per price was hidden for now (in Layout)
                if (isChecked) {
                    et_quantity.setVisibility(View.VISIBLE);
                    tv_quantity.setVisibility(View.VISIBLE);
                    tv_price.setVisibility(View.GONE);
                    et_price.setVisibility(View.GONE);
                    btn_price_details.setVisibility(View.VISIBLE);
                    freeBox.setVisibility(View.GONE);
                    editor.putBoolean(GlobalVariables.SEATS, true);
                    editor.apply();
                } else {
                    et_quantity.setVisibility(View.VISIBLE);
                    tv_quantity.setVisibility(View.VISIBLE);
                    tv_price.setVisibility(View.VISIBLE);
                    et_price.setVisibility(View.VISIBLE);
                    btn_price_details.setVisibility(View.GONE);
                    freeBox.setVisibility(View.VISIBLE);
                    editor.putBoolean(GlobalVariables.SEATS, false);
                    editor.apply();
                }

                break;
        }
    }

    /**
     * Spinner items selected
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override //29.09 - assaf updated
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.filterSpinner:
                et_tags.setHint("");
                switch (position) {
                    case 0:
                        filter = FILTERS[0];
                        break;
                    case 1:
                        filter = FILTERS[1];
                        break;
                    case 2:
                        filter = FILTERS[2];
                        break;
                    case 3:
                        filter = FILTERS[3];
                        break;
                    case 4:
                        filter = FILTERS[4];
                        break;
                    case 5:
                        filter = FILTERS[5];
                        break;
                    case 6:
                        filter = FILTERS[6];
                        break;
                    case 7:
                        filter = FILTERS[7];
                        break;
                    case 8:
                        filter = FILTERS[8];
                        break;
                    case 9:
                        filter = FILTERS[9];
                        break;
                    case 10:
                        filter = FILTERS[10];
                        break;
                    case 11:
                        filter = FILTERS[11];
                        break;
                    case 12:
                        filter = FILTERS[12];
                        break;
                    case 13:
                        filter = FILTERS[13];
                        break;
                    case 14:
                        filter = FILTERS[14];
                        break;
                    case 15:
                        filter = FILTERS[15];
                        break;
                    case 16:
                        filter = FILTERS[16];
                        break;
                    case 17:
                        filter = FILTERS[17];
                        break;
                    case 18:
                        filter = FILTERS[18];
                        break;
                    case 19:
                        filter = FILTERS[19];
                        break;
                    case 20:
                        filter = FILTERS[20];
                        break;
                    case 21:
                        filter = FILTERS[21];
                        break;
                    case 22:
                        filter = FILTERS[22];
                        break;
                    case 23:
                        filter = FILTERS[23];
                        break;
                    case 24:
                        filter = FILTERS[24];
                        break;
                    case 25:
                        filter = FILTERS[25];
                        break;
                    case 26:
                        filter = FILTERS[26];
                        break;
                    case 27:
                        filter = FILTERS[27];
                        break;
                    case 28:
                        filter = FILTERS[28];
                        break;
                    case 29:
                        filter = FILTERS[29];
                        break;
                    case 30:
                        filter = FILTERS[30];
                        break;
                    default:
                        filter ="";
                }
                et_tags.setHint(getString(R.string.Add_tags));
                break;

            case R.id.atmSpinner:
                switch (position) {
                    case 0:
                        atmStatus = ATMS[0];
                        break;
                    case 1:
                        atmStatus = ATMS[1];
                        break;
                    case 2:
                        atmStatus = ATMS[2];
                        break;
                }
                break;
            case R.id.toiletSpinner:
                switch (position) {
                    case 0:
                        numOfToilets = TOILETS[0];
                        break;
                    case 1:
                        numOfToilets = TOILETS[1];
                        break;
                    case 2:
                        numOfToilets = TOILETS[2];
                        break;
                    case 3:
                        numOfToilets = TOILETS[3];
                        break;
                    case 4:
                        numOfToilets = TOILETS[4];
                        break;
                    case 5:
                        numOfToilets = TOILETS[5];
                        break;
                    case 6:
                        numOfToilets = TOILETS[6];
                        break;
                    case 7:
                        numOfToilets = TOILETS[7];
                        break;
                    case 8:
                        numOfToilets = TOILETS[8];
                        break;
                    case 9:
                        numOfToilets = TOILETS[9];
                        break;
                    case 10:
                        numOfToilets = TOILETS[10];
                        break;
                    case 11:
                        numOfToilets = TOILETS[11];
                        break;

                }
                break;
            case R.id.handicapToiletSpinner:
                //               numOfHandicapToilets = TOILETS[position];
                // java.lang.ArrayIndexOutOfBoundsException: length=11; index=-1
                switch (position) {
                    case 0:
                        numOfHandicapToilets = TOILETS[0];
                        break;
                    case 1:
                        numOfHandicapToilets = TOILETS[1];
                        break;
                    case 2:
                        numOfHandicapToilets = TOILETS[2];
                        break;
                    case 3:
                        numOfHandicapToilets = TOILETS[3];
                        break;
                    case 4:
                        numOfHandicapToilets = TOILETS[4];
                        break;
                    case 5:
                        numOfHandicapToilets = TOILETS[5];
                        break;
                    case 6:
                        numOfHandicapToilets = TOILETS[6];
                        break;
                    case 7:
                        numOfHandicapToilets = TOILETS[7];
                        break;
                    case 8:
                        numOfHandicapToilets = TOILETS[8];
                        break;
                    case 9:
                        numOfHandicapToilets = TOILETS[9];
                        break;
                    case 10:
                        numOfHandicapToilets = TOILETS[10];
                        break;
                    case 11:
                        numOfHandicapToilets = TOILETS[11];
                        break;
                }
                break;
        }


    }


    /**
     * Nothing selected in the Spinners
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        switch (parent.getId()) {
            case R.id.filterSpinner:
                filter = null;
                et_tags.setHint("");
                break;
            case R.id.atmSpinner:
                atmStatus = ATMS[0];
                break;
            case R.id.toiletSpinner:
                numOfToilets = TOILETS[11];
                break;
            case R.id.handicapToiletSpinner:
                numOfHandicapToilets = TOILETS[11];
                break;
        }
    }


    class ValidateAddress extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(CreateEventActivity.this);
            dialog.setMessage("" + getString(R.string.validating));
            dialog.show();
        }

        // ----------------------------------------------------
        @Override
        protected String doInBackground(String... params) {
            String queryString = null;
            String addressResult = "";
            try {
                queryString = "" +
                        "&address=" + URLEncoder.encode(address, "utf-8") +
                        "&key=" + GlobalVariables.GEO_API_KEY ;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                addressResult = HttpHandler.get(params[0], queryString);

                if (addressResult != null && addressResult!="") { // get address and city in othr languages only if English works ok
                    addressPerLanguage.clear();
                    cityPerLanguage.clear();
                    EventDataMethods.addressNameNonEnglish(address, addressPerLanguage,cityPerLanguage);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return addressResult;
        }

        // ----------------------------------------------------
        @Override
        protected void onPostExecute(String s) {
            String street= "";
            String number = "";

                if (s == null) {
                    dialog.dismiss();
                    Toast.makeText(CreateEventActivity.this, R.string.something_went_wrong_plese_try_again, Toast.LENGTH_SHORT).show();
                    iv_val_add.setImageResource(R.drawable.x);
                    iv_val_add.setVisibility(View.VISIBLE);

                } else {
                    gson = new Gson();
                    result = gson.fromJson(s, Result.class);
                    if (result.getStatus().equals("OK")) {
                        address_ok = true;
                        iv_val_add.setImageResource(R.drawable.v);
                        iv_val_add.setVisibility(View.VISIBLE);

                      try {
                            String long_name = result.getResults().get(0).getAddress_components().get(1).getLong_name();
                            street = long_name.replaceAll("Street", "");
                            number = result.getResults().get(0).getAddress_components().get(0).getShort_name();
                            lat = result.getResults().get(0).getGeometry().getLocation().getLat();
                            lng = result.getResults().get(0).getGeometry().getLocation().getLng();
                            city = result.getResults().get(0).getAddress_components().get(2).getLong_name();
                            region1 = result.getResults().get(0).getAddress_components().get(4).getLong_name(); // sub region was added - 25.10 assaf
                            region2 = result.getResults().get(0).getAddress_components().get(5).getLong_name();
                            valid_address = street + " " + number + ", " + city;
                            //25.10 - print to the user what address that found
                            Toast.makeText(getApplicationContext(), getString(R.string.address_found) + valid_address + " " + region1 + " " + region2, Toast.LENGTH_LONG).show();

                          dialog.dismiss();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if (ex instanceof IndexOutOfBoundsException) {//if region and city not appear in the results of json
                                valid_address = street + " " + number + ", " + city; //Assaf 25.10 in case that region1 or Region2 not provided by the resposnse from Json and exc[etion was thrown
                                 Toast.makeText(getApplicationContext(), getString(R.string.address_found) + valid_address + " " + region1 + " " + region2, Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                          else {
                                Toast.makeText(getApplicationContext(), "error occur please try again later", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                        }else if (result.getStatus().equals("ZERO_RESULTS")) {
                            address_ok = false;
                            iv_val_add.setImageResource(R.drawable.x);
                            iv_val_add.setVisibility(View.VISIBLE);
                            Toast.makeText(CreateEventActivity.this, R.string.problem_is + result.getStatus(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        }

                    }
                }
            }

        private void saveTicketsPrice(String eventObjectId) {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            HashMap<String, String> map = new HashMap<>();
            map.put("start", "1");
            map.put("end", "4");
            map.put("price", "" + sp.getInt(GlobalVariables.BLUE, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Floor ");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });

            map.put("start", "11");
            map.put("end", "27");
            map.put("price", "" + sp.getInt(GlobalVariables.ORANGE, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Orange");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });

            map.put("start", "101");
            map.put("end", "117");
            map.put("price", "" + sp.getInt(GlobalVariables.PINK, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Pink");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });

            map.put("start", "121");
            map.put("end", "136");
            map.put("price", "" + sp.getInt(GlobalVariables.PINK, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Pink");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });

            map.put("start", "201");
            map.put("end", "217");
            map.put("price", "" + sp.getInt(GlobalVariables.YELLOW, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Yellow");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });


            map.put("start", "221");
            map.put("end", "236");
            map.put("price", "" + sp.getInt(GlobalVariables.YELLOW, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "Yellow");
            try {
                Integer result = ParseCloud.callFunction("saveTicketsPrice", map);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            map.put("start", "207");
            map.put("end", "213");
            map.put("price", "" + sp.getInt(GlobalVariables.GREEN, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "green");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });

            map.put("start", "225");
            map.put("end", "231");
            map.put("price", "" + sp.getInt(GlobalVariables.GREEN, 0));
            map.put("eventObjectId", eventObjectId);
            map.put("seatNumber", "green");
            ParseCloud.callFunctionInBackground("saveTicketsPrice", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object response, ParseException exc) {
                    Log.e("cloud code example", "response: " + response);
                }
            });
            editor.putBoolean(GlobalVariables.SEATS, false);
            editor.apply();
        }

    private boolean validateBeforeSaveEvent()
    {
        if (timeOk) {
                 //do nothing
        } else {
            Toast.makeText(CreateEventActivity.this,getString(R.string.enter_valid_date), Toast.LENGTH_SHORT).show();
            toSaveEvent = false;
        }

        if (et_name.length() != 0) {
            // do nothing
            et_name .getBackground().clearColorFilter();
        } else {
            et_name .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toSaveEvent = false;
        }

         if (date.length() != 0) {
                // do nothing
            } else {
                Toast.makeText(CreateEventActivity.this, getString(R.string.enter_valid_date), Toast.LENGTH_SHORT).show();
                toSaveEvent = false;
          }

        if (et_description.length() != 0) {
            et_description .getBackground().clearColorFilter();
        } else {
            et_description .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toSaveEvent = false;
        }

        if (address_ok)
        {
            et_address .getBackground().clearColorFilter();
        } else {
            et_address .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toSaveEvent = false;
        }

        if (et_place.length()!= 0)
        {
            et_place .getBackground().clearColorFilter();
        } else {
            et_place .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toSaveEvent = false;
        }

        if (freeEvent) { //29.09 -Assaf - quantity is not mandatory for Free event
            if (!validateQuantity()) {
                et_quantity.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toSaveEvent = false;
            }
            else if (validateQuantity()){
                et_quantity.getBackground().clearColorFilter();
            }
        }
        if (seats) {
            if (!validateQuantity()) {
                et_quantity .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toSaveEvent = false;
            } else if (validateQuantity()) {
                et_quantity .getBackground().clearColorFilter();
            }
        } else if (!seats && !freeEvent) {
            if (!validatePrice()) {
                et_price .getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toSaveEvent = false;
            }
            else{
                et_price .getBackground().clearColorFilter();
            }
            if (!validateQuantity()) {
                et_quantity.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                toSaveEvent = false;
            }
            else{
                et_quantity.getBackground().clearColorFilter();
              }
            }

        if (timeOk && et_place.length()!= 0&& et_description.length() != 0 && et_name.length() != 0 &&
                date.length() != 0 && address_ok && (filter!=null || !filter.isEmpty()))
            {
              if (seats && validateQuantity() || ((!seats && !freeEvent)&& validatePrice()&&validateQuantity()) || freeEvent&&validateQuantity()) {
                  toSaveEvent = true;
                  return toSaveEvent;
            }
              else
                  Toast.makeText(CreateEventActivity.this,getString(R.string.please_fill_empty_fields), Toast.LENGTH_SHORT).show();
            }
          else
               Toast.makeText(CreateEventActivity.this,getString(R.string.please_fill_empty_fields), Toast.LENGTH_SHORT).show();


        return toSaveEvent;
      }

 }


