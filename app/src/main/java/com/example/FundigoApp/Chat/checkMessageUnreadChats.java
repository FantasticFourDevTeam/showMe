package com.example.FundigoApp.Chat;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.FundigoApp.MyServices;
import com.parse.ParseException;
import com.parse.ParseQuery;
import java.util.ArrayList;
import java.util.List;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by mirit-binbin on 9/1/2016.
 */
public class checkMessageUnreadChats extends Service
{
    List<Room> unread;
    static ArrayList<String> messageToCustomerId  = new ArrayList<>();
    static ArrayList<String> messageToProducerId  = new ArrayList<>();
    Thread pullNumberOfUnreadMessageFromParse;

    @Override
    public void onCreate()
    {
        super.onCreate();
        pullNumberOfUnreadMessageFromParse  = new Thread(new Runnable() {
            @Override
            public void run()
            {
                while(true)
                {
                    checkMassage();
                    try
                    {

                        Log.e("TAGTAGTAG","before wait");
                        Thread.sleep(60*1000);
                        Log.e("TAGTAGTAG","after wait");
                    }catch (Exception e){ Log.e("TAGTAGTAG",e.getMessage());}
                }
            }
        });
        pullNumberOfUnreadMessageFromParse.start();

    }

    private void checkMassage()
    {
            ParseQuery <Room> query = ParseQuery.getQuery(Room.class);
            query.whereEqualTo("Read",false).whereEqualTo("Take_UnRead_Message",false);
            try
            {
                unread = query.find();
                sortMessage();
            }
            catch (ParseException e) {}
    }

    private void saveInParseUnreadMessagePull()
    {
        for(int i = 0; i < unread.size(); i++)
        {
            unread.get(i).put("Take_UnRead_Message",true);
            try {
                unread.get(i).save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    private void sortMessage()
    {
        if(unread != null)
        {
            String eventObjectId;
            for(int i = 0; i < unread.size(); i++)
            {
                eventObjectId = unread.get(i).getEventObjId();
                Log.e("aaaaaaaaaaaaaaaa",unread.get(i).getObjectId());
                if(unread.get(i).getProducer_id() != null)
                {
                    if(!messageToProducerId.contains(eventObjectId))messageToProducerId.add(eventObjectId);
                }
                else
                {
                    if(!messageToCustomerId.contains(eventObjectId))messageToCustomerId.add(eventObjectId);
                }
            }
            saveInParseUnreadMessagePull();

            Log.e("messageToCustomerId =",""+(messageToCustomerId.size()));
            Log.e("messageToProducerId =",""+(messageToProducerId.size()));

            int push = Integer.parseInt( MyServices.getFromSharedPreferences("push",getApplicationContext()));
            int unreadMessageCustomer =  Integer.parseInt(MyServices.getFromSharedPreferences("unreadMessageFromCustomer",getApplicationContext()));
            int unreadMessageProducer =  Integer.parseInt(MyServices.getFromSharedPreferences("unreadMessageFromProducer",getApplicationContext()));

            Log.e("checkMessageUnreadChats","unreadMessageCustomer = "+unreadMessageCustomer+" ;;;unreadMessageProducer = "+unreadMessageProducer);

            MyServices.saveOnSharedPreferences(""+(unreadMessageCustomer+messageToCustomerId.size()),"unreadMessageFromProducer",getApplicationContext());
            MyServices.saveOnSharedPreferences(""+(unreadMessageProducer+messageToProducerId.size()),"unreadMessageFromProducer",getApplicationContext());

            ShortcutBadger.applyCount(getApplicationContext(),(push+messageToCustomerId.size()+messageToProducerId.size()+unreadMessageProducer+unreadMessageCustomer));
            messageToCustomerId.clear();
            messageToProducerId.clear();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
