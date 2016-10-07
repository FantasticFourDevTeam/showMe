package com.example.FundigoApp.Customer.Social;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.EditText;

import com.example.FundigoApp.MyServices;
import com.example.FundigoApp.Chat.ChatActivity;
import com.example.FundigoApp.Chat.ChatToCustomersActivity;
import com.example.FundigoApp.Chat.MessageRoomAdapter;
import com.example.FundigoApp.Chat.MessageRoomBean;
import com.example.FundigoApp.Chat.Room;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CustomerMessageConversationsListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    ImageButton mipo;
    ImageView notification;
    ListView listView;
    String customer_id;
    List<MessageRoomBean> listOfConversationsBeans = new ArrayList<> ();
    ArrayList<String> eventsImageList = new ArrayList<> ();
    List<EventInfo> event_info_list = new ArrayList<EventInfo> ();
    private Handler handler = new Handler ();
    MessageRoomAdapter messageRoomAdapter;
	EditText unreadPushMessage,unreadMessage;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_message_producer);
        listView = (ListView) findViewById (R.id.listView_massge_producer);

        mipo = (ImageButton) findViewById (R.id.mipo_MassageProducer);
        notification = (ImageView) findViewById (R.id.notification_MassageProducer);
		unreadPushMessage = (EditText)findViewById(R.id.Push_Message_unread_CustomerMessageConversationsListActivity);
        unreadMessage = (EditText)findViewById(R.id.Message_unread_CustomerMessageConversationsListActivity);

		
        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
            customer_id = GlobalVariables.CUSTOMER_PHONE_NUM;
            messageRoomAdapter = new MessageRoomAdapter (this, listOfConversationsBeans, eventsImageList);
            listView.setAdapter (messageRoomAdapter);
            listView.setOnItemClickListener (this);
            getMassage ();
            handler.postDelayed (runnable, 500);
        }
        mipo.setOnClickListener (this);
        notification.setOnClickListener (this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId () == R.id.notification_MassageProducer) {
            finish();
        } else if (v.getId () == R.id.mipo_MassageProducer) {
            Intent mipoIntent = new Intent (CustomerMessageConversationsListActivity.this, MipoActivity.class);
            startActivity (mipoIntent);
            finish();
        }
    }

    private void getMassage() {
        List<Room> listOfConversationWithProducer = new ArrayList<> ();
        ParseQuery<Room> query = ParseQuery.getQuery (Room.class);
        ParseQuery<Room> query1 = ParseQuery.getQuery(Room.class);//Added to support messages from customers also
        ParseQuery<Room> query2 = ParseQuery.getQuery(Room.class);
        List<ParseQuery<Room>> listOfQueries= new ArrayList<>();
        query.whereEqualTo("customer_id", customer_id);//Added to support messages from customers also
        query1.whereEqualTo("customer1", customer_id);
        query2.whereEqualTo("customer2", customer_id);
        listOfQueries.add(query);
        listOfQueries.add(query1);
        listOfQueries.add(query2);


        ParseQuery<Room> query3 = ParseQuery.or(listOfQueries);//Added to support messages from customers also
        query3.orderByDescending ("updatedAt");
        try {
            listOfConversationWithProducer = query3.find ();
            updateLists (listOfConversationWithProducer);
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }

    private void getMassageInBackGround() {
       // ParseQuery<Room> query = ParseQuery.getQuery (Room.class);
       // query.whereEqualTo ("customer_id", customer_id);
      //  query.orderByDescending ("createdAt");
        ParseQuery<Room> query = ParseQuery.getQuery (Room.class);
        ParseQuery<Room> query1 = ParseQuery.getQuery(Room.class);//Added to support messages from customers also
        ParseQuery<Room> query2 = ParseQuery.getQuery(Room.class);
        List<ParseQuery<Room>> listOfQueries= new ArrayList<>();
        query.whereEqualTo("customer_id", customer_id);//Added to support messages from customers also
        query1.whereEqualTo("customer1", customer_id);
        query2.whereEqualTo("customer2", customer_id);
        listOfQueries.add(query);
        listOfQueries.add(query1);
        listOfQueries.add(query2);

        ParseQuery<Room> query3 = ParseQuery.or(listOfQueries);//Added to support messages from customers also
        query3.orderByDescending ("updatedAt");
        query3.findInBackground (new FindCallback<Room> () {
            public void done(List<Room> listOfConversationWithProducer, ParseException e) {
                if (e == null) {
                    updateLists (listOfConversationWithProducer);
                } else {
                    e.printStackTrace ();
                }
            }
        });
    }

    private void updateLists(List<Room> listOfConversationWithProducer) {
        List<MessageRoomBean> tempConversationsList = new ArrayList<> ();
        ArrayList<String> eventImageListTemp = new ArrayList<> ();
        List<EventInfo> event_info_list_temp = new ArrayList<EventInfo> ();
        for (int i = 0; i < listOfConversationWithProducer.size (); i++) {
            Room room = listOfConversationWithProducer.get (i);
            EventInfo eventInfo = EventDataMethods.getEventFromObjID (room.getEventObjId (), GlobalVariables.ALL_EVENTS_DATA);
            if(eventInfo != null) {
                eventImageListTemp.add (eventInfo.getPicUrl ());
                event_info_list_temp.add (eventInfo);
                tempConversationsList.add (new MessageRoomBean (room.getLastMessage (),
                                                                       eventInfo.getName(),
                                                                       room.getProducer_id(),room.getCustomer_id1(),room.getCustomer_id2()));
            }
        }
        listOfConversationsBeans.clear ();
        listOfConversationsBeans.addAll (tempConversationsList);
        eventsImageList.clear ();
        eventsImageList.addAll (eventImageListTemp);
        event_info_list.clear ();
        event_info_list.addAll (event_info_list_temp);
        messageRoomAdapter.notifyDataSetChanged ();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent;
        String producerIdOfViewListItem = listOfConversationsBeans.get(i).getProducer_id();
        if (!producerIdOfViewListItem.equals("")) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("index", event_info_list.get(i).getIndexInFullList());
            intent.putExtra("customer_phone", GlobalVariables.CUSTOMER_PHONE_NUM);
            startActivity(intent);
        }
        else {
         String recieverCustomerIdForChat=listOfConversationsBeans.get(i).getCustomer1_id();
         if (GlobalVariables.CUSTOMER_PHONE_NUM.equals(recieverCustomerIdForChat))
         {
             recieverCustomerIdForChat = listOfConversationsBeans.get(i).getCustomer2_id();
         }
        intent = new Intent (this,ChatToCustomersActivity.class);
        intent.putExtra("customer_phone",GlobalVariables.CUSTOMER_PHONE_NUM);
        intent.putExtra("senderCustomer", "Customer # "+recieverCustomerIdForChat);
        intent.putExtra("senderUserName", UserDetailsMethod.getUserDetailsFromParseInMainThread(recieverCustomerIdForChat).getCustomerName());
        intent.putExtra("index", event_info_list.get(i).getIndexInFullList());// event index in All Events List
        startActivity(intent);
        }
    }

    private Runnable runnable = new Runnable () {
        @Override
        public void run() {
            getMassageInBackGround ();
            handler.postDelayed (this, 500);
        }
    };

    @Override
    public void onPause() {
        super.onPause ();
        handler.removeCallbacks (runnable);
    }

    @Override
    public void onRestart() {
        super.onRestart ();
        handler.postDelayed (runnable, 500);
    }
	
	
    @Override
    protected void onResume()
    {
        super.onResume();
        MyServices.checkVisibilityForUnreadCustomerAndProducerMessage(this.unreadMessage,getApplicationContext());
        MyServices.checkVisibilityForUnreadPushMessage(this.unreadPushMessage,getApplicationContext());
    }
}
