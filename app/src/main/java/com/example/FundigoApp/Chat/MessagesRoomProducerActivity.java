package com.example.FundigoApp.Chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagesRoomProducerActivity extends Activity implements AdapterView.OnItemClickListener {

    ListView list_view;
    List<MessageRoomBean> conversationsList = new ArrayList<MessageRoomBean> ();
    MessageRoomAdapter messageRoomAdapter;
    int event_index;
    private Handler handler = new Handler ();
    HashMap<String, CustomerDetails> customerPhoneToDetailsMap = new HashMap<String, CustomerDetails> ();
    private static TextView noMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_messages_producer_room);

        list_view = (ListView) findViewById (R.id.listView);
        Intent intent = getIntent ();
        event_index = intent.getIntExtra ("index", 0);
        messageRoomAdapter = new MessageRoomAdapter (this, conversationsList);
        list_view.setAdapter (messageRoomAdapter);
        list_view.setOnItemClickListener(this);
        noMessage = (TextView)findViewById(R.id.noMessage);
        getConversationsFromParseMainThread();
        handler.postDelayed(runnable, 500);
    }

    private void getConversationsFromParseMainThread() {
        List<Room> roomsParsList;
        ParseQuery<Room> query = ParseQuery.getQuery (Room.class);
        query.setLimit(1000);
        query.whereEqualTo ("producer_id", GlobalVariables.PRODUCER_PARSE_OBJECT_ID);
        query.whereEqualTo("eventObjId", GlobalVariables.ALL_EVENTS_DATA.get(event_index).getParseObjectId());
        query.orderByDescending("updatedAt");
        try {
            roomsParsList = query.find ();
            if(roomsParsList.size()!=0) {
                noMessage.setVisibility(View.GONE);
                updateConvData(roomsParsList);
            }
            else {
                noMessage.setVisibility(View.VISIBLE);
                noMessage.setText("No waiting Message from Customers");
            }
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }

    private void getConversationsFromParseInBackground() {
        ParseQuery<Room> query = ParseQuery.getQuery (Room.class);
        query.setLimit(1000);
        query.whereEqualTo ("producer_id", GlobalVariables.PRODUCER_PARSE_OBJECT_ID);
        query.whereEqualTo("eventObjId", GlobalVariables.ALL_EVENTS_DATA.get(event_index).getParseObjectId());
        query.orderByDescending("updatedAt");
        query.findInBackground(new FindCallback<Room>() {
            public void done(List<Room> rooms, ParseException e) {
                if (e == null) {
                    updateConvData(rooms);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateConvData(List<Room> roomsParsList) {

        try {
            List<MessageRoomBean> tempConversationsList = new ArrayList<MessageRoomBean>();
            for (int i = 0; i < roomsParsList.size(); i++) {
                tempConversationsList.add(new MessageRoomBean(roomsParsList.get(i).getLastMessage(),
                        roomsParsList.get(i).getCustomer_id(),
                        GlobalVariables.PRODUCER_PARSE_OBJECT_ID,"",""));
            }
            for (MessageRoomBean messageRoomBean : tempConversationsList) {
                if (customerPhoneToDetailsMap.get(messageRoomBean.getCustomer_id()) == null) {
                    updateUserDetailsFromParse(messageRoomBean);
                } else {
                    updateMessageBeanWithCustomerDetails(messageRoomBean,
                            customerPhoneToDetailsMap.get(messageRoomBean.getCustomer_id()));

                }
            }
            conversationsList.clear();
            conversationsList.addAll(tempConversationsList);
            messageRoomAdapter.notifyDataSetChanged();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void updateUserDetailsFromParse(MessageRoomBean messageRoomBean) {
        CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThread (messageRoomBean.getCustomer_id ());
        updateMessageBeanWithCustomerDetails (messageRoomBean, customerDetails);
        customerPhoneToDetailsMap.put (messageRoomBean.getCustomer_id (),
                                              customerDetails);
    }

    private void updateMessageBeanWithCustomerDetails(MessageRoomBean messageRoomBean, CustomerDetails customerDetails) {
        if (customerDetails.getCustomerImage () != null) {
            messageRoomBean.setCustomerImage(customerDetails.getCustomerImage());
        }
        else if (customerDetails.getPicUrl () != null && !customerDetails.getPicUrl ().isEmpty ()) {
            messageRoomBean.setCustomerImageFacebookUrl (customerDetails.getPicUrl ());
        }

        String customerName= customerDetails.getCustomerName();
        if (customerName!=null && !customerName.isEmpty())
              messageRoomBean.setCustomerName(customerName);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent (this, ChatActivity.class);
        intent.putExtra ("index", event_index);
        intent.putExtra ("customer_phone", conversationsList.get (i).getCustomer_id ());
        startActivity (intent);
    }

    private Runnable runnable = new Runnable () {
        @Override
        public void run() {
            getConversationsFromParseInBackground ();
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
}
