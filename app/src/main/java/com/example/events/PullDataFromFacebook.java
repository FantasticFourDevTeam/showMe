package com.example.events;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.FundigoApp.Events.Event;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by benjamin on 2/14/2017.
 */

public class PullDataFromFacebook
{
    List<String> id = new ArrayList<>();
    String cameToThisActivityFrom;
    File tempFacebookImageFile;
    Context context;
    HashMap<String, String> addressPerLanguage = new HashMap<>();
    HashMap<String, String> cityPerLanguage = new HashMap<>();
    String fbName = "";
    String fbPicUrl = "";
    String fbEmail = "";
    String fbID = "";

    public PullDataFromFacebook(String cameToThisActivityFrom,Context context)
    {
        this.cameToThisActivityFrom = cameToThisActivityFrom;
        this.context = context;
    }

    public void getDataFromFacebook()
    {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture.type(large),link,events{id,category,place,picture.type(large),name,start_time,ticket_uri,admins,description,interested_count,attending_count}");
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
                            getProfileDetailsFromFaceBook(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    /**
     * The function pull the all profile from json object
     * @param response is GraphResponse from facebook
     */
    private void getProfileDetailsFromFaceBook(GraphResponse response)
    {
        try
        {
            JSONObject picture = response.getJSONObject().getJSONObject("picture");
            JSONObject data = picture.getJSONObject("data");
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            if(!response.getJSONObject().toString().isEmpty()&& !response.getJSONObject().equals(null)) //assaf 23.01 to read laso mail adress
            {
                //Log.e ("FaceBook" ,"FB user data" +" " + response.getJSONObject().toString() );
                editor.putString(GlobalVariables.FB_EMAIL, response.getJSONObject().getString("email"));
                editor.putString(GlobalVariables.FB_NAME, response.getJSONObject().getString("name"));
                editor.putString(GlobalVariables.FB_PIC_URL, data.getString("url"));
                editor.putString(GlobalVariables.FB_ID, response.getJSONObject().getString("id"));
                editor.apply();

                fbName = sp.getString(GlobalVariables.FB_NAME, "");
                fbPicUrl = sp.getString(GlobalVariables.FB_PIC_URL, "");
                fbEmail = sp.getString(GlobalVariables.FB_EMAIL, "");
                fbID = sp.getString(GlobalVariables.FB_ID, "");
            }
            if(cameToThisActivityFrom.equals("userMenu")||cameToThisActivityFrom.equals("mainMenu")) // Register by SMS only if facebook registration is from User Menu
                ActivityFacebook.PassSMSRegistration(); //26.02 - assaf to pass SMS regitration if not registered user
            else
            {
             if (ActivityFacebook.activity!=null)
                ActivityFacebook.activity.finish();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
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
            if(cameToThisActivityFrom != "producer")parseObject.setProducerId(object.getString("id"));
            else
            {
                if(object.getJSONObject("owner").getString("id").equals(fbID)) parseObject.setProducerId(GlobalVariables.PRODUCER_PARSE_OBJECT_ID);
                else parseObject.setProducerId(object.getString("id"));
            }
            if (object.has("name")) {
                parseObject.setName(object.getString("name"));
                Log.e("NameofEvent", object.getString("name"));
            }
            if (object.has("place")) {
                try {
                    street = object.getJSONObject("place").getJSONObject("location").getString("street"); //assaf 16/01
                }
                catch (Exception ex){
                    street ="";
                    ex.printStackTrace();
                }
                try {
                    place = object.getJSONObject("place").getString("name");
                }
                catch (Exception ex){
                    place= "";
                    ex.printStackTrace();
                }
                try
                {
                    city = object.getJSONObject("place").getJSONObject("location").getString("city"); //assaf 16/01
                }
                catch (Exception ex){
                    city = "";
                    ex.printStackTrace();
                }
                if(city=="" && street==""){
                    address="";
                }
                else {
                    address = street + "" + "," + city;//assaf
                }

                Log.e(" + place", place);
                Log.e("+ street", street);
                Log.e("+Picture", object.getJSONObject("picture").toString());
                Log.e("full object", "object" + object.toString());

                 try {
                    if (object.getJSONObject("place").has("location")) {
                        parseObject.setY(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("longitude")));
                        parseObject.setX(Double.parseDouble(object.getJSONObject("place").getJSONObject("location").getString("latitude")));
                    }
                   }
                  catch (Exception ex){
                      ex.printStackTrace();
                  }

                try {
                    if (address != "" && address != null) {
                        addressPerLanguage.clear();
                        cityPerLanguage.clear();
                        EventDataMethods.addressNameNonEnglish(address, addressPerLanguage, cityPerLanguage);
                    }
                    else
                    {
                        addressPerLanguage.clear();
                        cityPerLanguage.clear();
                        EventDataMethods.addressNameNonEnglish("", addressPerLanguage, cityPerLanguage);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                parseObject.setCity(city);
                parseObject.setAddress(address);
                parseObject.setPlace(place);
                parseObject.setCityPerLanguage(cityPerLanguage);
                parseObject.setAddressPerLanguage(addressPerLanguage);
            }
            else {
                parseObject.setPlace(object.getJSONObject("place").getString("general"));
            }
            if (object.has("admins")) {
                parseObject.setArtist(object.getJSONObject("admins").getJSONArray("data").getJSONObject(0).getString("name"));
            }
            if (object.has("description")) {
                parseObject.setDescription(object.getString("description"));
            }
            if (object.has("ticket_uri") && parseObject.getPrice()==null) {//price 1 , num of seats 10 - prevent upddate of price and seats each time
                parseObject.setPrice("1");
                parseObject.setNumOfTickets(1);
            }
            else if(parseObject.getPrice()==null) {
                parseObject.setPrice("FREE"); // Free and num of tickets unlimited - prevent update of price and seats each time
                parseObject.setNumOfTickets(-1);
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
            parseObject.setEventATMService("Unknown");
            parseObject.setEventCapacityService("Unknown");
            parseObject.setEventParkingService("Unknown");
            parseObject.setEventToiletService("1, Handicapped 0");
            if (object.has("category"))
            {
                parseObject.setFilterName(object.getString("category"));
                if (object.getString("category").contains("_"))
                {
                    parseObject.setTags("#" + object.getString("category").substring(0, object.getString("category").indexOf("_")));
                }
                else
                {
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
                    parseObject.setPic(downloadImageFromUrl(object.getJSONObject("picture").getJSONObject("data").getString("url"), eventNum));
                  //  tempFacebookImageFile.delete();
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
     * The function update event data
     *
     * @param object   json object from facebook
     * @param eventNum number of event
     * @param event    yhe event that need update in parse
     */
    private void updateEventDataFromUserEvent(JSONObject object, int eventNum, Event event) {
        putDataToEvent(object, event, eventNum);
    }


    /**
     * benjamin
     * The function download picture of facebook Event from string
     *
     * @param str url of picture
     * @return ParseFile
     */
    private ParseFile downloadImageFromUrl(String str, int i) throws IOException {
        try {
            URL url = new URL(str);
            //File facebookImage = UrlToFile(url);// 24.02 assaf
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            return changeBitmapToByteAndSaveInParseFile(bitmap, i);
            //return changeBitmapToByteAndSaveInParseFIle(i,facebookImage.getPath());// 24.02 assaf
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * benjamin
     * The function change bitmap to byteArray
     *
     * @param filePath is a picture
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
        Bitmap unscaledBitmap = com.example.events.ScalingUtilities.decodeFile(filePath,50, 50 , com.example.events.ScalingUtilities.ScalingLogic.FIT);
        Bitmap resizedBitMap = com.example.events.ScalingUtilities.createScaledBitmap(unscaledBitmap, 100, 100, com.example.events.ScalingUtilities.ScalingLogic.FIT);

        resizedBitMap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] image = stream.toByteArray();
        ParseFile file = new ParseFile("" + ((GlobalVariables.ALL_EVENTS_DATA.size() + 1) + i), image);
        return file;
    }
	
	private ParseFile changeBitmapToByteAndSaveInParseFile(Bitmap bitmap,int i) { // 24.02 - new Method Assaf
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        /////asaf///
        //  Bitmap resized = Bitmap.createScaledBitmap(bitmap, 600, 600, true); ///assaf added
        //  resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
        /////
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        ////

        byte[] image = stream.toByteArray();
        ParseFile file = new ParseFile("" + ((GlobalVariables.ALL_EVENTS_DATA.size() + 1) + i ) + ".JPEG", image);
        return file;
    }


    //ASSAF

    private File UrlToFile(URL fileUrl) {//Assaf - 20.1 - pull the File from Link an store it on temp File
        URLConnection connection;
        File FacebookImage = getTempFile(context);
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

}
