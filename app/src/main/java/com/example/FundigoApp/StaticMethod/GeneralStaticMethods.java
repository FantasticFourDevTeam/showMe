package com.example.FundigoApp.StaticMethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.Social.Profile;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GeneralStaticMethods {

    ///////////////////////////////////////
    //Saved events
    ///////////////////////////////////////
    public static void updateSavedEvents(final List<EventInfo> eventsList, final Context context) {
        try {
            InputStream inputStream = context.openFileInput("saves");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                while ((receiveString = bufferedReader.readLine()) != null) {
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).getParseObjectId().equals(receiveString)) {
                            eventsList.get(i).setIsSaved(true);
                        }
                    }
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER && GlobalVariables.userChanels.size() == 0) {
            ParseQuery<Profile> query = ParseQuery.getQuery("Profile");
            query.whereEqualTo("number", GlobalVariables.CUSTOMER_PHONE_NUM);
            query.setLimit(1000);
            query.findInBackground(new FindCallback<Profile>() {
                @Override
                public void done(List<Profile> objects, ParseException e) {
                    if (e == null) {
                        if (objects.get(0).getChanels() != null) {
                            if (GlobalVariables.userChanels.size() == 0) {
                                GlobalVariables.userChanels.addAll(objects.get(0).getChanels());
                            }
                            for (String eventObjId : GlobalVariables.userChanels) {
                                EventInfo event = EventDataMethods.getEventFromObjID(eventObjId, eventsList);
                                if (event != null && !event.getIsSaved()) {
                                    event.setIsSaved(true);
                                    saveEvent(context, event);
                                }
                            }
                        }
                    } else {
                        e.printStackTrace();
                    }
                }

            });
        } else {
            for (String eventObjId : GlobalVariables.userChanels) {
                EventInfo event = EventDataMethods.getEventFromObjID(eventObjId, eventsList);
                if (event != null && !event.getIsSaved()) {
                    event.setIsSaved(true);
                    saveEvent(context, event);
                }
            }
        }
    }

    public static void handleSaveEventClicked(final EventInfo event,
                                              ImageView save,
                                              final Context context,
                                              int savedImageId,
                                              int unsavedImageId) {
        if (event.getIsSaved()) {
            event.setIsSaved(false);
            save.setImageResource(unsavedImageId);
            Toast.makeText(context, R.string.you_unsaved_this_event, Toast.LENGTH_SHORT).show();
            if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
                UserDetailsMethod.canclePush(event);
            }
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        context.deleteFile("temp");
                        InputStream inputStream = context.openFileInput("saves");
                        OutputStream outputStreamTemp = context.openFileOutput("temp", Context.MODE_PRIVATE);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStreamTemp));
                        String lineToRemove = event.getParseObjectId();
                        String currentLine;
                        while ((currentLine = bufferedReader.readLine()) != null) {
                            // trim newline when comparing with lineToRemove
                            String trimmedLine = currentLine.trim();
                            if (trimmedLine.equals(lineToRemove)) continue;
                            else {
                                bufferedWriter.write(currentLine);
                                bufferedWriter.write(System.getProperty("line.separator"));
                            }
                        }
                        bufferedReader.close();
                        bufferedWriter.close();
                        context.deleteFile("saves");
                        inputStream = context.openFileInput("temp");
                        outputStreamTemp = context.openFileOutput("saves", Context.MODE_PRIVATE);
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStreamTemp));
                        while ((currentLine = bufferedReader.readLine()) != null) {
                            bufferedWriter.write(currentLine);
                            bufferedWriter.write(System.getProperty("line.separator"));
                        }
                        bufferedReader.close();
                        bufferedWriter.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            event.setIsSaved(true);
            save.setImageResource(savedImageId);
            Toast.makeText(context, R.string.you_saved_this_event, Toast.LENGTH_SHORT).show();
            if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
                UserDetailsMethod.registerToPush(event);
            }
            saveEvent(context, event);
        }
    }

    public static void saveEvent(final Context context, final EventInfo event) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = context.openFileOutput("saves", Context.MODE_APPEND + Context.MODE_PRIVATE);
                    outputStream.write(event.getParseObjectId().getBytes());
                    outputStream.write(System.getProperty("line.separator").getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    ///////////////////////////////////////
    //General
    ///////////////////////////////////////
    public static void onActivityResult(final int requestCode, final Intent data, Activity activity) {
        if (data != null && requestCode == GlobalVariables.REQUEST_CODE_MY_PICK) {
            String appName = data.getComponent().flattenToShortString();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
            String name = sp.getString("name", null);
            String date = sp.getString("date", null);
            String place = sp.getString("place", null);
            if (appName.equals("com.facebook.katana/com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias")) {
                ShareDialog shareDialog;
                shareDialog = new ShareDialog(activity);
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("I`m going to " + name)
                        .setImageUrl(Uri.parse("https://lh3.googleusercontent.com/-V5wz7jKaQW8/VpvKq0rwEOI/AAAAAAAAB6Y/cZoicmGpQpc/s279-Ic42/pic0.jpg"))
                        .setContentDescription(
                                "C u there at " + date + " !" + "\n" + "At " + place)
                        .setContentUrl(Uri.parse("http://eventpageURL.com/here"))
                        .build();
                shareDialog.show(linkContent);
            } else {
                activity.startActivity(data);
            }
        }
    }

    //Temp BYPASS for Generate QR codes Tickets instead of Pelepay
    public static void url(String orederId, String phone, String firstName, String lastName, String email) {
        String url1 = "https://WhoGO.parseapp.com/hello?index=6575&ConfirmationCode=0257440&Response=000&amount=3.00&firstname=" + firstName + "&lastname=" + lastName + "&payfor=product&email=" + email + "&phone=" + phone + "&orderid=" + orederId + "&custom=mydata";
        BuyTickets buyTickets = new BuyTickets();
        Log.e("GeneralStaticMethod Url", "before execute");
        buyTickets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url1);
        Log.e("GeneralStaticMethod Url", "after execute");
    }

    private static class BuyTickets extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("onPostExecute", "will start");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.e("onProgressExecute", "work...");
        }

        @Override
        protected String doInBackground(String... params) {
            String ans = null;
            try {
                //URL url = new URL("https://benjamin.parseapp.com/hello?index=1111&ConfirmationCode=0257440&Response=000&amount=3.00&firstname=itzik&lastname=tal&payfor=product&email=itzik@pelepay.co.il&phone=050-9764600&orderid=FNMQunyJqU&custom=mydata");
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                // httpURLConnection.setConnectTimeout(10000);
                //httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                ans = "" + httpURLConnection.getResponseCode();
                return params[0] + "|||||" + ans;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return " not succses";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("onPostExecute", "is " + s);
        }
    }

    public static boolean getLanguage() { //25.10 - assaf
        if (Locale.getDefault().getDisplayLanguage().equals("עברית")) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDateToStringConversion(Date dateToConvert) {

        // SimpleDateFormat dateFormatter = new SimpleDateFormat("E,MMMMMM d, yyyy, hh:mm a", Locale.getDefault());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E,d/M/yyyy, hh:mm a", Locale.getDefault());

        String dateConverted = dateFormatter.format(dateToConvert);

        return dateConverted;
    }

    public static Boolean EmailAddressVerified(String producer_username) { // 27.11 - assaf gglobal method that check if user verify his emil or not

        final boolean[] emailVerified = new boolean[2];
        emailVerified [0] = false;
        final String _prodcuerName = producer_username;
        ParseQuery<ParseUser> query1 = ParseUser.getQuery();
        query1.setLimit(100000);
        try {
            List <ParseUser> listUsers = query1.find();
            for (ParseUser user : listUsers) {
              if (user.getUsername().equals(_prodcuerName)) {
                if (user.get("emailVerified") != null && (boolean) user.get("emailVerified")) {
                     emailVerified[0] = true;
                   }
                }
             }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return emailVerified[0];
    }
}


