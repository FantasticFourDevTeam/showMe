package com.example.FundigoApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;

import com.example.FundigoApp.Chat.Room;
import com.example.FundigoApp.Events.EventInfo;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by mirit-binbin on 8/31/2016.
 */
public class MyServices
{
    public static void clearSharedPreferences(Context context,String key)
    {
        SharedPreferences preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    
    public static void saveOnSharedPreferences(String saveData, String key, Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(key,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("pushNumber",saveData);
        edit.commit();
    }

    public static String getFromSharedPreferences(String key, Context context)
    {
        SharedPreferences sp = context.getSharedPreferences(key,Context.MODE_PRIVATE);
        return sp.getString("pushNumber","0");
    }

    public static void checkVisibilityForUnreadCustomerAndProducerMessage(EditText unreadMessage, Context context)
    {
        String unreadMessageFromCustomer = MyServices.getFromSharedPreferences("unreadMessageFromCustomer",context);
        String unreadMessageFromProducer = MyServices.getFromSharedPreferences("unreadMessageFromProducer",context);
        int unread = Integer.parseInt(unreadMessageFromCustomer)+Integer.parseInt(unreadMessageFromProducer);
        if(unreadMessage.getVisibility() != View.VISIBLE && unread > 0)
        {
            unreadMessage.setVisibility(View.VISIBLE);
            unreadMessage.setText(""+unread);
        }
        else
        {
            if(unread > 0)
            {
                unreadMessage.setText(""+unread);
            }
            else
            {
                unreadMessage.setVisibility(View.INVISIBLE);
            }
        }
    }



    public static void checkVisibilityForUnreadPushMessage(EditText unreadPushMessage, Context context)
    {
        String push = MyServices.getFromSharedPreferences("push",context);
        if(unreadPushMessage.getVisibility() != View.VISIBLE && push != "0")
        {
            unreadPushMessage.setVisibility(View.VISIBLE);
            unreadPushMessage.setText(push);
        }
        else
        {
            if(push != "0")
            {
                unreadPushMessage.setText(push);
            }
            else
            {
                unreadPushMessage.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * update parse that message read.
     */
    public static void updateDataForUnreadMessage(String dataSave,String dataPull, Context context, EventInfo eventInfo)
    {
        ParseQuery<Room> roomParseQuery = new ParseQuery<Room>("Room");
        roomParseQuery.whereEqualTo("Read",false);
        List<Room> roomList = null;
        int counter = 0;
        try{
            roomList = roomParseQuery.find();
            for (int i = 0 ; i < roomList.size() ; i++)
            {
                if(roomList.get(i).getEventObjId().equals(eventInfo.getParseObjectId()))
                {
                    roomList.get(i).put("Read",true);
                    roomList.get(i).save();
                    counter++;
                }
            }
            int numberOfUnreadMessage = Integer.parseInt(MyServices.getFromSharedPreferences(dataSave,context))-counter;
            MyServices.saveOnSharedPreferences(""+numberOfUnreadMessage,dataSave,context);
            ShortcutBadger.applyCount(context,numberOfUnreadMessage+Integer.parseInt(getFromSharedPreferences("push",context))+Integer.parseInt(getFromSharedPreferences(dataPull,context)));
        }catch (ParseException e){}
    }

}