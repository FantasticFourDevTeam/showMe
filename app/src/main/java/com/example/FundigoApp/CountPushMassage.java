package com.example.FundigoApp;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.parse.ParsePushBroadcastReceiver;
import org.json.JSONException;
import org.json.JSONObject;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by benjamin on 8/28/2016.
 */
public class CountPushMassage extends ParsePushBroadcastReceiver
{
    public static final String PARSE_DATA_KEY = "com.parse.Data";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String intentAction = intent.getAction();
        Log.e("onReceive ::::",intentAction);
        switch (intentAction) {
            case ACTION_PUSH_RECEIVE:
                onPushReceive(context, intent);
                break;
            case ACTION_PUSH_DELETE:
                onPushDismiss(context, intent);
                break;
            case ACTION_PUSH_OPEN:
                onPushOpen(context, intent);
                break;
        }

    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context,intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        // Implement
        int push = Integer.parseInt( MyServices.getFromSharedPreferences("push",context));

        Log.e("MY TAG onPushOpen",""+push);
        ShortcutBadger.applyCount(context,--push);
        Log.e("MY TAG onPushOpen",""+push);

        if( push == 0)MainActivity.unreadMessage.setVisibility(View.INVISIBLE);
        else MainActivity.unreadMessage.setText(""+push);

        //Intent i = new Intent(context, MenuActivity.class);
        MyServices.saveOnSharedPreferences(""+push,"push",context);
        //context.startActivity(i);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.e("MYTag","onPushReceive");
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:
        int push = Integer.parseInt( MyServices.getFromSharedPreferences("push",context));

        ShortcutBadger.applyCount(context, ++push);
        Log.e("MYTag","onPushReceive : : "+push);
        if(MainActivity.unreadMessage.getVisibility() != View.VISIBLE)
        {
            MainActivity.unreadMessage.setVisibility(View.VISIBLE);
            Log.e("MY TAG","onPushReceive in if"+push);
        }

        MainActivity.unreadMessage.setText(""+push);
        Log.e("MY TAG","onPushReceive : "+push);
        MyServices.saveOnSharedPreferences(""+push,"push",context);
       /*  NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Title");
        builder.setContentText("Text");
        builder.setSmallIcon(R.drawable.notification_icon);
        builder.setAutoCancel(true);

        // OPTIONAL create soundUri and set sound:
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notificationManager.notify("MyTag", 0, builder.build());*/
    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
        } catch (JSONException e) {
            // Json was not readable...
        }
        return data;
    }

}
