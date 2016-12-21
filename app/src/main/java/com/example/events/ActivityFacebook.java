package com.example.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Events.Event;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static android.R.attr.id;

public class ActivityFacebook extends AppCompatActivity {

    LoginButton facebook_login_button;
    CallbackManager callbackManager;
    LoginButton facebook_logout_button;
    TextView facebookUserNameView;
    ImageView profileFacebookPictureView;
    Context context;
    List<String> id = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        context = this;
      //  facebook_login_button = (LoginButton) findViewById (R.id.login_button11);
     //   facebook_logout_button = (LoginButton) findViewById (R.id.logout_button11);
      //  facebookUserNameView = (TextView) findViewById (R.id.profileUserName);
      //  profileFacebookPictureView = (ImageView) findViewById (R.id.faebook_profile);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken ();
        if (accessToken != null) {
//            facebook_login_button.setVisibility (View.GONE);
//            profileFacebookPictureView.setVisibility (View.VISIBLE);
//            facebookUserNameView.setVisibility (View.VISIBLE);
//            facebook_logout_button.setVisibility (View.VISIBLE);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences (ActivityFacebook.this);
            String name = sp.getString (GlobalVariables.FB_NAME, null);
            String pic_url = sp.getString (GlobalVariables.FB_PIC_URL, null);
//            Picasso.with (context).load (pic_url).into (profileFacebookPictureView);
//            facebookUserNameView.setText (name);
        } else {
//            facebook_login_button.setVisibility (View.VISIBLE);
//            facebook_logout_button.setVisibility (View.GONE);
//            profileFacebookPictureView.setVisibility (View.GONE);
//            facebookUserNameView.setVisibility (View.GONE);
        }

        callbackManager = CallbackManager.Factory.create ();
//        facebook_login_button.setOnClickListener (new View.OnClickListener () {
//            @Override
//            public void onClick(View v) {
                LoginManager.getInstance ().
                        logInWithReadPermissions
                                (ActivityFacebook.this,
                                        Arrays.asList
                                                ("public_profile",
                                                        "user_friends",
                                                        "email","user_events"));
        ///    }
    //    });
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
              @Override
              public void onSuccess(final LoginResult loginResult) {
                  accessToken.setCurrentAccessToken(loginResult.getAccessToken());
                  getUserDetailsFromFB();
                  Intent databack = new Intent();
                  databack.putExtra("Login Success", true);
                  setResult(Activity.RESULT_OK, databack); // send data back to Menu as Login is OK
                  finish();
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
        Bundle parameters = new Bundle ();
        parameters.putString ("fields", "email,name,picture,link,events{id,category,place,picture,name,start_time,ticket_uri,admins,description,interested_count,attending_count}");
        new GraphRequest(
                AccessToken.getCurrentAccessToken (),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback () {
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.e("getUserDetailsFromFB",response.getJSONObject().getJSONObject("events").getJSONArray("data").getJSONObject(0).toString());//benjamin add
                            JSONObject event = response.getJSONObject().getJSONObject("events");//benjamin add
                            JSONArray eventArray = event.getJSONArray("data");//benjamin add
                            new downloadPictureAndSaveDataToParse(eventArray).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//benjamin add
                            JSONObject picture = response.getJSONObject ().getJSONObject ("picture");
                            JSONObject data = picture.getJSONObject ("data");
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences (ActivityFacebook.this);
                            SharedPreferences.Editor editor = sp.edit ();
                            editor.putString (GlobalVariables.FB_NAME, response.getJSONObject ().getString ("name"));
                            editor.putString (GlobalVariables.FB_PIC_URL, data.getString ("url"));
                            editor.putString (GlobalVariables.FB_ID, response.getJSONObject ().getString ("id"));
                            editor.apply ();
                            //Picasso.with (context).load (data.getString ("url")).into (profileFacebookPictureView);
                            //facebookUserNameView.setText (response.getJSONObject ().getString ("name"));
                        } catch (JSONException e) {
                            e.printStackTrace ();
                        }
                    }
                }
        ).executeAsync ();
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        callbackManager.onActivityResult (requestCode, resultCode, data);
    }


    /**
     * benjamin
     * AsyncTack that responsible to create event from faceBook and download picture
     */
    private class downloadPictureAndSaveDataToParse extends AsyncTask<Void,Void,Void>
    {
        JSONArray array;
        public downloadPictureAndSaveDataToParse(JSONArray jsonArray)
        {
            array = jsonArray;
        }
        @Override
        protected Void doInBackground(Void... params) {
            createEventFromFaceBook(array);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("DATA","finish");
        }
    }

    /**
     * benjamin
     * The function create event that pull from faceBook.
     * @param jsonArray json array that contain all user event from facebook
     */
    private void createEventFromFaceBook(JSONArray jsonArray)
    {
        for(int i = 0; i < jsonArray.length() ; i++)
        {
            try
            {
                JSONObject object =jsonArray.getJSONObject(i);
                ParseQuery<Event> query = ParseQuery.getQuery("Event");
                query.whereEqualTo("producerId",object.getString("id"));
                List<Event> arr =query.find();
                Log.e(" arr.size() == ",""+arr.size());

                if(arr.size() == 0 && !id.contains(object.getString("id")))
                {
                    Event parseObject = new Event();
                    id.add(object.getString("id"));

                    parseObject.setFbUrl("https://www.facebook.com/events/"+object.getString("id"));
                    parseObject.setProducerId(object.getString("id"));
                    putDataToEvent(object,parseObject,i);
                }
                else  updateEventDataFromUserEvent(object,i,arr.get(0));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.e("JSONException",e.getMessage());
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("ParseException",e.getMessage());
            }
        }
    }

    /**
     * The functoin put to event his data
     * @param object json object ftom facebook
     * @param parseObject Event (ParseObject)
     */
    private void putDataToEvent(JSONObject object, Event parseObject,int eventNum)
    {
        try
        {
            if(object.has("name"))
            {
                parseObject.setName(object.getString("name"));
                Log.e("NameofEvent",object.getString("name"));
            }

            if(object.has("place"))
            {
                if(object.getJSONObject("place").has("location"))
                {
                    parseObject.setX(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("longitude")));
                    parseObject.setY(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("latitude")));
                    parseObject.setCity(object.getJSONObject("place").getJSONObject("location").getString("city"));
                }else  parseObject.setCity("general");
                parseObject.setAddress(object.getJSONObject("place").getString("name"));
                parseObject.setPlace(object.getJSONObject("place").getString("name"));
            }else  parseObject.setPlace(object.getJSONObject("place").getString(" general"));
            if(object.has("admins"))
            {
                parseObject.setArtist(object.getJSONObject("admins").getJSONArray("data").getJSONObject(0).getString("name"));
            }
            if(object.has("description"))parseObject.setDescription(object.getString("description"));
            if(object.has("ticket_uri"))parseObject.setPrice(object.getString("ticket_uri"));
            else parseObject.setPrice("FREE");
            if(object.has("interested_count"))parseObject.setInterested_count(object.getInt("interested_count"));
            if(object.has("attending_count"))parseObject.setInterested_count(object.getInt("attending_count"));
            parseObject.setEventFromFacebook(true);
            if(object.has("is_canceled"))
            {
                if(object.getBoolean("is_canceled"))
                {
                    parseObject.setCancelEventFromFacebook(true);
                    SimpleDateFormat sdf = new SimpleDateFormat ("dd/MM/yyyy_HH:mm:ss");
                    String currentDateandTime = sdf.format (new Date ());
                    //ParsePush.subscribeInBackground ("a" + eventObjectId);
                    ParsePush push = new ParsePush ();

                    ParseObject query = new ParseObject ("Push");
                    push.setMessage ("Event "+object.getString("name")+" is canceled" + "(" + currentDateandTime + ")");
                    try
                    {
                        push.send ();
                        query.put("pushMessage", "Event "+object.getString("name")+" is canceled");
                        query.put ("Date", currentDateandTime);
                      //  query.put ("EvendId", eventObjectId);
                        query.save();
                        //ParsePush.unsubscribeInBackground("a" + eventObjectId);
                    }
                    catch (com.parse.ParseException e)
                    {
                        e.getStackTrace ();
                    }
                }
                else parseObject.setCancelEventFromFacebook(false);

            }
            parseObject.setTags("general");
            parseObject.setNumOfTickets(-1);
            parseObject.setEventATMService("no");
            parseObject.setEventCapacityService("Up To 100");
            parseObject.setEventParkingService("Up To 50");
            parseObject.setEventToiletService("1, Handicapped 0");
            if(object.has("category"))parseObject.setFilterName(object.getString("category"));
            else parseObject.setFilterName("other");
            parseObject.setIsStadium(false);
            parseObject.setAccessToken(AccessToken.getCurrentAccessToken().getToken());
            if(object.has("start_time"))
            {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = formatter.parse(object.getString("start_time").split(Pattern.quote("+"))[0]);
                parseObject.setRealDate(date);
            }
            if(object.has("picture") && object.getJSONObject("picture").has("data") && object.getJSONObject("picture").getJSONObject("data").has("url"))
            {
                parseObject.setPic(downloadImageFromUrl(object.getJSONObject("picture").getJSONObject("data").getString("url"),eventNum));
            }
            String id =object.getString("id");
            if(object.getString("name").equals("testing"))
            {
                Log.e("EventById", object.getString("id"));
                Log.e("EventById", object.getString("name"));
            }
            parseObject.save();
            if(object.getString("id").equals(id))Log.e("EventById","event save");
        }

        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (java.text.ParseException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

    }


    /**
     * benjamin
     * The function download picture of facebook Event from string
     * @param str url of picture
     * @return ParseFile
     */
    private ParseFile downloadImageFromUrl(String str,int i)
    {
        try
        {
            URL url = new URL(str);
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            return changeBitmapToByteAndSaveInParseFIle(bitmap,i);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * benjamin
     * The function change bitmap to byteArray
     * @param bitmap picture
     * @return ParseFile
     */
    private ParseFile changeBitmapToByteAndSaveInParseFIle(Bitmap bitmap,int i)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();
        ParseFile file = new ParseFile(""+((GlobalVariables.ALL_EVENTS_DATA.size()+1)+i)+".jpeg",image);
        return file;
    }

    /**
     * The function update event data
     * @param object json object from facebook
     * @param eventNum number of event
     * @param event yhe event that need update in parse
     */
    private void updateEventDataFromUserEvent(JSONObject object,int eventNum,Event event)
    {
        putDataToEvent(object,event,eventNum);
    }
}
