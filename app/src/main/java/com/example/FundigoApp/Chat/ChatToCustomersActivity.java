package com.example.FundigoApp.Chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatToCustomersActivity extends Activity implements Comparator<String> {

    private EditText editTextMessage;
    private ListView chatListView;
    private ArrayList<MessageChat> mMessageChatsList;
    private com.example.FundigoApp.Chat.MessageAdapter mAdapter;
    private boolean messagesFirstLoad;
    private Handler handler = new Handler ();
    String messageBody;
    ImageView profileImage;
    Button profileName;
    Button profileFaceBook;
    String faceBookId;
    String eventName;
    String customerPhone;
    EventInfo eventInfo;
    private Room room;
    ImageLoader loader;
    private String recieverCustomerPhone;

    private String customer1;
    private String customer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        this.requestWindowFeature (Window.FEATURE_NO_TITLE);
        setContentView (R.layout.activity_chat_to_customers);
        loader = FileAndImageMethods.getImageLoader (this);
        profileImage = (ImageView) findViewById (R.id.profileImage_chat);
        profileName = (Button) findViewById (R.id.ProfileName_chat);
        profileFaceBook = (Button) findViewById (R.id.ProfileFacebook_chat);
        Intent intent = getIntent ();
        int eventIndex = intent.getIntExtra ("index", 0);
        customerPhone = intent.getStringExtra ("customer_phone");
        recieverCustomerPhone = intent.getStringExtra("senderCustomer").substring("Customer # ".length());

        eventInfo = GlobalVariables.ALL_EVENTS_DATA.get(eventIndex);
//        if(room == null) {
//            room = getRoomObject ();
//        }
        eventName = eventInfo.getName ();
      //  if (GlobalVariables.IS_PRODUCER) {
        //    profileName.setText (customerPhone);
          //  updateUserDetailsFromParse ();
    //    }
        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
            profileName.setText (recieverCustomerPhone + getResources ().getString (R.string.chat_with_customer));
            setEventInfo (eventInfo.getPicUrl());
        }
        editTextMessage = (EditText) findViewById (R.id.etMessageCustomerChat);
        chatListView = (ListView) findViewById (R.id.messageListviewCustomerChat);
        mMessageChatsList = new ArrayList<MessageChat> ();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        chatListView.setTranscriptMode (1);
        messagesFirstLoad = true;
        mAdapter = new MessageAdapter (this, mMessageChatsList, false);

        chatListView.setAdapter (mAdapter);

        int result=  compare(customerPhone,recieverCustomerPhone);
         if (result>0 || result==0)
         {
             customer1 = recieverCustomerPhone;
             customer2=customerPhone;
         }
        else if (result<0)
         {
             customer2 = recieverCustomerPhone;
             customer1=customerPhone;
         }
    }

//    private void updateUserDetailsFromParse() {
//        CustomerDetails customerDetails = StaticMethods.getUserDetailsFromParseInMainThread (customerPhone);
//        if (customerDetails.getFaceBookId () == null || customerDetails.getFaceBookId ().isEmpty ()) {
//            profileFaceBook.setText ("");
//            profileFaceBook.setClickable (false);
//        } else {
//            faceBookId = customerDetails.getFaceBookId ();
//        }
//        if (customerDetails.getPicUrl () != null && !customerDetails.getPicUrl ().isEmpty ()) {
//            Picasso.with (this).load (customerDetails.getPicUrl ()).into (profileImage);
//        } else if (customerDetails.getCustomerImage () != null) {
//            loader.displayImage (customerDetails.getCustomerImage (), profileImage);
//        }
//        if (customerDetails.getCustomerImage () == null &&
//                customerDetails.getPicUrl () == null &&
//                customerDetails.getFaceBookId () == null) {
//            profileFaceBook.setText ("");
//            profileFaceBook.setClickable (false);
//        }
//    }

    private void setEventInfo(String picUrl) {
        profileFaceBook.setVisibility (View.GONE);
        float hight = TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_DIP, 55, getResources ().getDisplayMetrics ());
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams (
                        0,
                        Math.round (hight));
        params.weight = 90.0f;
        profileImage.setLayoutParams(params);
        loader.displayImage(picUrl, profileImage);
    }

    private Runnable runnable = new Runnable () {
        @Override
        public void run() {
            getAllMessagesFromParseInBackground (customer1,customer2); // assaf changed
            handler.postDelayed (this, 300);
        }
    };

    public void sendMessageToCustomer(View view) {
        messageBody = editTextMessage.getText().toString();
        MessageToCustomer message = new MessageToCustomer();
        if (!messageBody.isEmpty()) {
            message.setBody(messageBody);
            message.setCustomer1(customer1);
            message.setCustomer2(customer2);
            message.setSenderId(customerPhone);
           //message.setEventObjectId(eventInfo.getParseObjectId());
            try {
                message.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            editTextMessage.setText("");
            getAllMessagesFromParseInMainThread(customer1, customer2);
            // updateMessageRoomItemInBackGround (message);
        } else {
            Toast.makeText(this, "No Message to Send", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAllMessagesFromParseInBackground(final String customer1, final String customer2) {
        ParseQuery<MessageToCustomer> query = ParseQuery.getQuery (MessageToCustomer.class);
        query.whereEqualTo ("customer2", customer2);
        query.whereEqualTo ("customer1", customer1);
        //query.whereEqualTo ("eventObjectId", eventInfo.getParseObjectId ());
        query.orderByAscending ("createdAt");
        query.findInBackground (new FindCallback<MessageToCustomer> () {
            public void done(List<MessageToCustomer> messages, ParseException e) {
                if (e == null) {
                    if (messages.size () > mMessageChatsList.size ()) {
                        updateMessagesList (messages);
                    }
                } else {
                    e.printStackTrace ();
                }
            }
        });
    }

    private void getAllMessagesFromParseInMainThread(String customer1, String customer2) {
        ParseQuery<MessageToCustomer> query = ParseQuery.getQuery (MessageToCustomer.class);
        query.whereEqualTo ("customer1", customer1);
        query.whereEqualTo ("customer2", customer2);
        //query.whereEqualTo ("eventObjectId", eventInfo.getParseObjectId ());
        query.orderByAscending ("createdAt");
        List<MessageToCustomer> messages = null;
        try {
            messages = query.find ();
            if (messages.size () > mMessageChatsList.size ()) {
                updateMessagesList (messages);
            }
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }

    private void updateMessagesList(List<MessageToCustomer> messages) {
        mMessageChatsList.clear();
        for (int i = 0; i < messages.size(); i++) {
            MessageToCustomer msg = messages.get(i);
            String id = GlobalVariables.CUSTOMER_PHONE_NUM;
            boolean isMe;
            if (id.equals(msg.getSenderId())) {
                isMe = true;
            }
            else {
                isMe = false;
            }
                mMessageChatsList.add(new MessageChat (
                        MessageChat.MSG_TYPE_TEXT,
                        MessageChat.MSG_STATE_SUCCESS,
                        id,
                        msg.getBody(),
                        isMe,
                        true,
                        msg.getCreatedAt()));
            }
            mAdapter.notifyDataSetChanged(); // update adapter
            // Scroll to the bottom of the eventList on initial load
            if (messagesFirstLoad) {
                chatListView.setSelection(mAdapter.getCount() - 1);
                messagesFirstLoad = false;
            }
        }


//    public void updateMessageRoomItemInBackGround(final Message message) {
//        String senderType = "";
//        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
//            senderType = "Customer : ";
//        } else if (GlobalVariables.IS_PRODUCER) {
//            senderType = "Producer : ";
//        }
//        final String senderTypeFinal = senderType;
//        saveRoomData (room, senderTypeFinal, message);
//    }

//    private void saveRoomData(Room room, String senderTypeFinal, Message message) {
//        room.setLastMessage (senderTypeFinal + message.getBody ());
//        room.saveInBackground ();
//    }

    @Override
    public void onPause() {
        super.onPause ();
        handler.removeCallbacks (runnable);
        room = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(room == null) {
           // room = getRoomObject ();
        }
        handler.postDelayed (runnable, 0);
    }

    public void oOpenFacebookIntent(View view) {
        startActivity(getOpenFacebookIntent());
    }

    public Intent getOpenFacebookIntent() {
        String facebookUrl = "https://www.facebook.com/" + faceBookId;
        try {
            getPackageManager ().getPackageInfo ("com.facebook.katana", 0);
            return new Intent (Intent.ACTION_VIEW, Uri.parse ("fb://facewebmodal/f?href=" + facebookUrl));
        } catch (Exception e) {
            return new Intent (Intent.ACTION_VIEW, Uri.parse ("https://www.facebook.com/app_scoped_user_id/" + faceBookId));
        }
    }

    @Override
    public int compare(String customer, String sender) {

       if (customer == sender) {
           return 0;
       }
       if (customer.compareTo(sender) <0) {
           return -1;
       }
       if (customer.compareTo(sender) >0) {
           return 1;
       }
        return 0;
    }

    public void startMessageWithCustomer(View view)
    {
        //do nothing .
        // msg_item_left layout used by several activties .
        // this is to prevent error in case user (by mistake) pressed for open private chat
    }


//    private Room getRoomObject() {
//        ParseQuery<Room> query = ParseQuery.getQuery ("Room");
//        query.whereEqualTo ("producer_id", eventInfo.getProducerId());
//        query.whereEqualTo ("customer_id", customerPhone);
//        query.whereEqualTo ("eventObjId", eventInfo.getParseObjectId ());
//        query.orderByDescending ("createdAt");
//        try {
//            List<Room> roomList = query.find ();
//            if (roomList.size () == 0) {
//                Room room = new Room ();
//                ParseACL parseACL = new ParseACL ();
//                parseACL.setPublicWriteAccess (true);
//                parseACL.setPublicReadAccess (true);
//                room.setACL (parseACL);
//                room.setCustomer_id (customerPhone);
//                room.setProducer_id (eventInfo.getProducerId());
//                room.setEventObjId (eventInfo.getParseObjectId ());
//                return room;
//            } else {
//                Room room = roomList.get (0);
//                return room;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace ();
//        }
//        return null;
//    }
}