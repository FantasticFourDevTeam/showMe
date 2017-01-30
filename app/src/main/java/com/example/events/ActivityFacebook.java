package com.example.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.Customer.Social.Profile;
import com.example.FundigoApp.Events.Event;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.example.FundigoApp.Verifications.SmsSignUpActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;



public class ActivityFacebook extends AppCompatActivity {

    LoginButton facebook_login_button;
    CallbackManager callbackManager;
    LoginButton facebook_logout_button;
    TextView facebookUserNameView;
    ImageView profileFacebookPictureView;
    Context context;
    List<String> id = new ArrayList<>();
    HashMap<String, String> addressPerLanguage = new HashMap<>();
    HashMap<String, String> cityPerLanguage = new HashMap<>();
    File tempFacebookImageFile;


    SharedPreferences sp;
    String fbName = "";
    String fbPicUrl = "";
    String fbEmail = "";
    String fbID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        context = this;
        //  facebook_login_button = (LoginButton) findViewById (R.id.login_button11);
        //   facebook_logout_button = (LoginButton) findViewById (R.id.logout_button11);
        //  facebookUserNameView = (TextView) findViewById (R.id.profileUserName);
        //  profileFacebookPictureView = (ImageView) findViewById (R.id.faebook_profile);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
//            facebook_login_button.setVisibility (View.GONE);
//            profileFacebookPictureView.setVisibility (View.VISIBLE);
//            facebookUserNameView.setVisibility (View.VISIBLE);
//            facebook_logout_button.setVisibility (View.VISIBLE);
             sp = PreferenceManager.getDefaultSharedPreferences(ActivityFacebook.this);
             fbName = sp.getString(GlobalVariables.FB_NAME, "");
             fbPicUrl = sp.getString(GlobalVariables.FB_PIC_URL, "");
             fbEmail = sp.getString(GlobalVariables.FB_EMAIL, "");
             fbID = sp.getString(GlobalVariables.FB_ID, "");
//          Picasso.with (context).load (pic_url).into (profileFacebookPictureView);
//          facebookUserNameView.setText (name);
        } else {
//            facebook_login_button.setVisibility (View.VISIBLE);
//            facebook_logout_button.setVisibility (View.GONE);
//            profileFacebookPictureView.setVisibility (View.GONE);
//            facebookUserNameView.setVisibility (View.GONE);
        }

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().
                logInWithReadPermissions
                        (ActivityFacebook.this,
                                Arrays.asList
                                        ("public_profile",
                                                "user_friends",
                                                "email", "user_events"));
        ///    }
        //    });
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                if (accessToken!=null) {
                    accessToken.setCurrentAccessToken(loginResult.getAccessToken());
                }
                getUserDetailsFromFB();
                Intent databack = new Intent();
                databack.putExtra("Login Success", true);
                setResult(Activity.RESULT_OK, databack); // send data back to Menu as Login is OK

//                facebook_login_button.setVisibility (View.GONE);
//                facebook_logout_button.setVisibility (View.VISIBLE);
//                profileFacebookPictureView.setVisibility (View.VISIBLE);
//                facebookUserNameView.setVisibility (View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Toast.makeText(context, R.string.canceled_logging_facebook, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(context, R.string.error_logging_facebook, Toast.LENGTH_SHORT).show();
                Log.e("error_logging_facebook", exception.getMessage());
                exception.printStackTrace();
                finish();
            }
        });

    }

    private void getUserDetailsFromFB() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture,link,events{id,category,place,picture,name,start_time,ticket_uri,admins,description,interested_count,attending_count}");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.e("getUserDetailsFromFB", response.getJSONObject().getJSONObject("events").getJSONArray("data").getJSONObject(0).toString());//benjamin add
                            JSONObject event = response.getJSONObject().getJSONObject("events");//benjamin add
                            JSONArray eventArray = event.getJSONArray("data");//benjamin add
                            new downloadPictureAndSaveDataToParse(eventArray).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//benjamin add
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityFacebook.this);
                            SharedPreferences.Editor editor = sp.edit();

                             if(!response.getJSONObject().toString().isEmpty()&& !response.getJSONObject().equals(null)) //assaf 23.01 to read laso mail adress
                             {
                                 //Log.e ("FaceBook" ,"FB user data" +" " + response.getJSONObject().toString() );
                                 editor.putString(GlobalVariables.FB_EMAIL, response.getJSONObject().getString("email"));
                                 editor.putString(GlobalVariables.FB_NAME, response.getJSONObject().getString("name"));
                                 editor.putString(GlobalVariables.FB_PIC_URL, data.getString("url"));
                                 editor.putString(GlobalVariables.FB_ID, response.getJSONObject().getString("id"));
                                 editor.apply();

                                 fbName =   sp.getString(GlobalVariables.FB_NAME, "");
                                 fbPicUrl = sp.getString(GlobalVariables.FB_PIC_URL, "");
                                 fbEmail = sp.getString(GlobalVariables.FB_EMAIL, "");
                                 fbID = sp.getString(GlobalVariables.FB_ID, "");
                                 //Picasso.with (context).load (data.getString ("url")).into (profileFacebookPictureView);
                                 //facebookUserNameView.setText (response.getJSONObject ().getString ("name"));
                             }
                            PassSMSRegistration(); //23.01 - assaf to pass SMS regitration if not done
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * benjamin
     * AsyncTack that responsible to create event from faceBook and download picture
     */
    private class downloadPictureAndSaveDataToParse extends AsyncTask<Void, Void, Void> {
        JSONArray array;

        public downloadPictureAndSaveDataToParse(JSONArray jsonArray) {
            array = jsonArray;
        }

        @Override
        protected Void doInBackground(Void... params) {
            createEventFromFaceBook(array);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("DATA", "finish");
        }
    }

    /**
     * benjamin
     * The function create event that pull from faceBook.
     *
     * @param jsonArray json array that contain all user event from facebook
     */
    private void createEventFromFaceBook(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                ParseQuery<Event> query = ParseQuery.getQuery("Event");
                query.whereEqualTo("producerId", object.getString("id"));
                List<Event> arr = query.find();
                Log.e(" arr.size() == ", "" + arr.size());

                if (arr.size() == 0 && !id.contains(object.getString("id"))) {
                    Event parseObject = new Event();
                    id.add(object.getString("id"));
                    parseObject.setFbUrl("https://www.facebook.com/events/" + object.getString("id"));
                    parseObject.setProducerId(object.getString("id"));
                    putDataToEvent(object, parseObject, i);
                } else updateEventDataFromUserEvent(object, i, arr.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSONException", e.getMessage());
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("ParseException", e.getMessage());
            }
        }
    }

    /**
     * The functoin put to event his data
     *
     * @param object      json object ftom facebook
     * @param parseObject Event (ParseObject)
     */
    private void putDataToEvent(JSONObject object, Event parseObject, int eventNum) {

        String street = "";
        String place = "";
        String city = "";
        String address = "";
        try {
            if (object.has("name")) {
                parseObject.setName(object.getString("name"));
                Log.e("NameofEvent", object.getString("name"));
            }

            if (object.has("place")) {

                street = object.getJSONObject("place").getJSONObject("location").getString("street"); //assaf 16/01
                place = object.getJSONObject("place").getString("name");
                city = object.getJSONObject("place").getJSONObject("location").getString("city"); //assaf 16/01
                address = street + "" + "," + city;//assaf

                Log.e(" + object", object.getJSONObject("place").toString());
                Log.e("+ street", street);
                Log.e("+Picture" ,object.getJSONObject("picture").toString());
                Log.e ("full object" , "object" + object.toString());

                if (object.getJSONObject("place").has("location")) {
                    parseObject.setY(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("longitude")));
                    parseObject.setX(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("latitude")));
                    parseObject.setCity(city);
                   ///assaf
                    try {
                        if (address != "" && address != null) {
                            addressPerLanguage.clear();
                            cityPerLanguage.clear();
                            EventDataMethods.addressNameNonEnglish(address, addressPerLanguage, cityPerLanguage);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                  ///assaf

                } else {
                    parseObject.setCity("general");
                }
                parseObject.setAddress(address);
                parseObject.setPlace(place);
                parseObject.setCityPerLanguage(cityPerLanguage);
                parseObject.setAddressPerLanguage(addressPerLanguage);
            } else parseObject.setPlace(object.getJSONObject("place").getString(" general"));
            if (object.has("admins")) {
                parseObject.setArtist(object.getJSONObject("admins").getJSONArray("data").getJSONObject(0).getString("name"));
            }
            if (object.has("description")) {
                parseObject.setDescription(object.getString("description"));
            }
            if (object.has("ticket_uri")) {
                parseObject.setPrice("1");
            }
            else {
                parseObject.setPrice("FREE");
            }
            if (object.has("interested_count")) {
                parseObject.setInterested_count(object.getInt("interested_count"));
            }
            if (object.has("attending_count")) {
                parseObject.setInterested_count(object.getInt("attending_count"));
            }
            parseObject.setEventFromFacebook(true);
            if (object.has("is_canceled")) {
                if (object.getBoolean("is_canceled")) {
                    parseObject.setCancelEventFromFacebook(true);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    //ParsePush.subscribeInBackground ("a" + eventObjectId);
                    ParsePush push = new ParsePush();
                    ParseObject query = new ParseObject("Push");
                    push.setMessage("Event " + object.getString("name") + " is canceled" + "(" + currentDateandTime + ")");
                    try {
                        push.send();
                        query.put("pushMessage", "Event " + object.getString("name") + " is canceled");
                        query.put("Date", currentDateandTime);
                        //  query.put ("EvendId", eventObjectId);
                        query.save();
                        //ParsePush.unsubscribeInBackground("a" + eventObjectId);
                    } catch (com.parse.ParseException e) {
                        e.getStackTrace();
                    }
                } else parseObject.setCancelEventFromFacebook(false);

            }
            parseObject.setNumOfTickets(-1);
            parseObject.setEventATMService("no");
            parseObject.setEventCapacityService("Unknown");
            parseObject.setEventParkingService("Unknown");
            parseObject.setEventToiletService("1, Handicapped 0");
            if (object.has("category")) {
                parseObject.setFilterName(object.getString("category"));
                if (object.getString("category").contains("_")) {
                    parseObject.setTags("#" + object.getString("category").substring(0, object.getString("category").indexOf("_")));
                } else {
                    parseObject.setTags("#" + object.getString("category"));
                }
            } else {
                parseObject.setFilterName("other");
                parseObject.setTags("#" + "other");
            }
            parseObject.setIsStadium(false);
            if (object.has("start_time")) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = formatter.parse(object.getString("start_time").split(Pattern.quote("+"))[0]);
                parseObject.setRealDate(date);
            }
            if (object.has("picture") && object.getJSONObject("picture").has("data") && object.getJSONObject("picture").getJSONObject("data").has("url")) {
                //assaf 16.01
                try {
                     //binyamin
                     parseObject.setPic(downloadImageFromUrl(object.getJSONObject("picture").getJSONObject("data").getString("url"),eventNum));
                    tempFacebookImageFile.delete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //assaf
            }
            String id = object.getString("id");
            if (object.getString("name").equals("testing")) {
                Log.e("EventById", object.getString("id"));
                Log.e("EventById", object.getString("name"));
            }
            parseObject.setAccessToken(AccessToken.getCurrentAccessToken().getToken());
            parseObject.save();
            if (object.getString("id").equals(id)) {
                Log.e("EventById", "event save");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }


    /**
     * benjamin
     * The function download picture of facebook Event from string
     *
     * @param str url of picture
     * @return ParseFile
     */
    private ParseFile downloadImageFromUrl(String str, int i) {
        try {
            URL url = new URL(str);
            File facebookImage = UrlToFile(url);
           // URLConnection conn = url.openConnection();
            //Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
           // return changeBitmapToByteAndSaveInParseFIle(bitmap, i);
            return changeBitmapToByteAndSaveInParseFIle(i,facebookImage.getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * benjamin
     * The function change bitmap to byteArray
     *
     * @param String picture
     * @return ParseFile
     */
    private ParseFile changeBitmapToByteAndSaveInParseFIle(int i,String filePath) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        /////asaf///
      //  Bitmap resized = Bitmap.createScaledBitmap(bitmap, 100, 100, true); ///assaf added
      //  resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
         /////
       // bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ////

      //
        //Bitmap resizedBitMap = scaleBitmap(bitmap, 650, 650); ///assaf added

        //
        Bitmap unscaledBitmap = ScalingUtilities.decodeFile(filePath,50, 50 ,ScalingUtilities.ScalingLogic.FIT);
        Bitmap resizedBitMap = ScalingUtilities.createScaledBitmap(unscaledBitmap, 100, 100,ScalingUtilities.ScalingLogic.FIT);

        resizedBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] image = stream.toByteArray();
        ParseFile file = new ParseFile("" + ((GlobalVariables.ALL_EVENTS_DATA.size() + 1) + i), image);
        return file;
    }

    /**
     * The function update event data
     *
     * @param object   json object from facebook
     * @param eventNum number of event
     * @param event    yhe event that need update in parse
     */
    private void updateEventDataFromUserEvent(JSONObject object, int eventNum, Event event) {
        putDataToEvent(object, event, eventNum);
    }

    //ASSAF

    private File UrlToFile(URL fileUrl) {//Assaf - 20.1 - pull the File from Link an store it on temp File
        URLConnection connection;
        File FacebookImage = getTempFile(this);
        try {
            connection = fileUrl.openConnection();
            InputStream inputStream = connection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(FacebookImage);
            byte[] buffer = new byte[512];
            while (true) {
                int length = inputStream.read(buffer);
                if (length == -1) {
                    break;
                }
                fileOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FacebookImage;
    }


    private File getTempFile(Context context) { // 20.10 - assaf- Create Tem FILE on OS
        tempFacebookImageFile = null;
        try {
            String fileName = "FaceBookTempEventPicture";
            tempFacebookImageFile = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFacebookImageFile;
    }


//    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
////        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
//  //      Canvas canvas = new Canvas(output);
//        int width = bitmap.getWidth();
//        int height =bitmap.getHeight();
//        float scaleWidth = ((float) wantedWidth) / width;
//        float scaleHeight = ((float) wantedHeight) / height;
//        Matrix m = new Matrix();
//       // m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
//        m.postScale(scaleWidth, scaleHeight);
//       // canvas.drawBitmap(bitmap, m, new Paint());
//
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
//
//
//       return resizedBitmap;
//
//        //return output;
//    }


    private void PassSMSRegistration(){

        Intent smsVerificationIntent = new Intent(ActivityFacebook.this, SmsSignUpActivity.class);
        startActivity(smsVerificationIntent);
        finish();
    }

//    private void UpdateUserDetailes ()
//    {
//
//        ParseQuery<Profile> query = ParseQuery.getQuery ("Profile");
//        query.whereEqualTo ("number", user_number);
//        query.findInBackground (new FindCallback<Profile>() {
//            public void done(List<Profile> numbers, ParseException e) {
//                if (e == null) {
//                    if (numbers.size () > 0) {
//                        previousDataFound = numbers.get (0);
//                        CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsWithBitmap(numbers);
//                        if (usernameTE.getText ().toString ().isEmpty ()) {
//                            usernameTE.setText (customerDetails.getCustomerName () + "");
//                            usernameTE.setSelection (usernameTE.getText ().length ());
//                        }
//                        if (!image_selected) {
//                            Bitmap customerImage = customerDetails.getBitmap ();
//                            if (customerImage != null) {
//                                customerImageView.setImageBitmap (customerImage);
//                                image_was_before = true;
//                            }
//                        }
//                        if(customerDetails.getEmail() != null) {
//                            emailAddress.setText(customerDetails.getEmail().toString());
//                        }
//                    }
//                } else {
//                    e.printStackTrace ();
//                }
//            }
//        });
//    }
//
//    private void CheckIfUserExist()
//    {
//
//    }
}


