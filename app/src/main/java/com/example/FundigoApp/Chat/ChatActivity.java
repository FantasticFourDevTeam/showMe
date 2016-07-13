package com.example.FundigoApp.Chat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

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

    //private String recipientId;
    //private EditText messageBodyField;
    //private String messageBody1;
    private MessageService.MessageServiceInterface messageService
            ;
   // private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener myMessageClientListener =new MyMessageClientListener();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        this.requestWindowFeature (Window.FEATURE_NO_TITLE);
        setContentView (R.layout.activity_main_chat);
        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
//broadcast receiver to listen for the broadcast
//from MessageService
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                //show a toast message if the Sinch
                //service failed to start
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.sinch.com.example.FundigoApp.Chat.ChatActivity"));









        loader = FileAndImageMethods.getImageLoader (this);
        profileImage = (ImageView) findViewById (R.id.profileImage_chat);
        profileName = (Button) findViewById (R.id.ProfileName_chat);
        profileFaceBook = (Button) findViewById (R.id.ProfileFacebook_chat);
        Intent intent = getIntent ();
        int eventIndex = intent.getIntExtra ("index", 0);
        customerPhone = intent.getStringExtra ("customer_phone");

        eventInfo = GlobalVariables.ALL_EVENTS_DATA.get (eventIndex);
        if(room == null) {
            room = getRoomObject ();
        }        
		eventName = eventInfo.getName ();
        if (GlobalVariables.IS_PRODUCER) {
            profileName.setText (customerPhone);
            updateUserDetailsFromParse ();
        } else if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
            profileName.setText (eventName + getResources ().getString (R.string.chat_with_producer));
            setEventInfo (eventInfo.getPicUrl());
        }
        getAllMessagesFromParseInBackground(eventInfo.getProducerId (), customerPhone);
        editTextMessage = (EditText) findViewById (R.id.etMessageChat);
        chatListView = (ListView) findViewById (R.id.messageListviewChat);
        mMessageChatsList = new ArrayList<MessageChat> ();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        chatListView.setTranscriptMode (1);
        messagesFirstLoad = true;
        mAdapter = new MessageAdapter (this, mMessageChatsList, false);

        chatListView.setAdapter (mAdapter);
    }

    private void updateUserDetailsFromParse() {
        CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThread (customerPhone);
        if (customerDetails.getFaceBookId () == null || customerDetails.getFaceBookId ().isEmpty ()) {
            profileFaceBook.setText ("");
            profileFaceBook.setClickable (false);
        } else {
            faceBookId = customerDetails.getFaceBookId ();
        }
        if (customerDetails.getPicUrl () != null && !customerDetails.getPicUrl ().isEmpty ()) {
            Picasso.with (this).load (customerDetails.getPicUrl ()).into (profileImage);
        } else if (customerDetails.getCustomerImage () != null) {
            loader.displayImage (customerDetails.getCustomerImage (), profileImage);
        }
        if (customerDetails.getCustomerImage () == null &&
                    customerDetails.getPicUrl () == null &&
                    customerDetails.getFaceBookId () == null) {
            profileFaceBook.setText ("");
            profileFaceBook.setClickable (false);
        }
    }

    private void setEventInfo(String picUrl) {
        profileFaceBook.setVisibility (View.GONE);
        float hight = TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_DIP, 55, getResources ().getDisplayMetrics ());
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams (
                                                      0,
                                                      Math.round (hight));
        params.weight = 90.0f;
        profileImage.setLayoutParams (params);
        loader.displayImage (picUrl, profileImage);
    }

  /*  private Runnable runnable = new Runnable () {
        @Override
        public void run() {
            getAllMessagesFromParseInBackground (eventInfo.getProducerId (), customerPhone);
            handler.postDelayed (this, 300);
        }
    };*/

    public void sendMessage(View view) {
        messageBody = editTextMessage.getText ().toString ();

        if(messageService==null)
        {
            Toast.makeText(this,"Can't Send",Toast.LENGTH_LONG).show();
        }
        else
        {
            if(GlobalVariables.IS_PRODUCER)messageService.sendMessage(customerPhone, messageBody);
            else messageService.sendMessage(eventInfo.getProducerId(), messageBody);
        }
        editTextMessage.setText ("");
       // getAllMessagesFromParseInMainThread (eventInfo.getProducerId (), customerPhone);
        //updateMessageRoomItemInBackGround (message);
    }

    private void getAllMessagesFromParseInBackground(final String producer, final String customer) {
        ParseQuery<Message> query = ParseQuery.getQuery (Message.class);
        query.whereEqualTo ("producer", producer);
        query.whereEqualTo ("customer", customer);
        query.whereEqualTo ("eventObjectId", eventInfo.getParseObjectId ());
        query.orderByAscending ("createdAt");
        query.findInBackground (new FindCallback<Message> () {
            public void done(List<Message> messages, ParseException e) {
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

    private void getAllMessagesFromParseInMainThread(String producer, String customer) {
        ParseQuery<Message> query = ParseQuery.getQuery (Message.class);
        query.whereEqualTo ("producer", producer);
        query.whereEqualTo ("customer", customer);
        query.whereEqualTo ("eventObjectId", eventInfo.getParseObjectId ());
        query.orderByAscending ("createdAt");
        List<Message> messages = null;
        try
        {
            messages = query.find ();
            if (messages.size () > mMessageChatsList.size ()) {
                updateMessagesList (messages);
            }
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }

    private void updateMessagesList(List<Message> messages) {
        mMessageChatsList.clear ();
        for (int i = 0; i < messages.size (); i++) {
            Message msg = messages.get (i);
            String id = msg.getUserId ();
            boolean isMe = false;
            if (!GlobalVariables.IS_PRODUCER) {
                if (id.equals (customerPhone)) {
                    isMe = true;
                } else {
                    id = "Producer # " + id;
                }
            } else {
                if (id.equals (eventInfo.getProducerId ())) {
                    isMe = true;
                } else {
                    id = "Customer " + id;
                }
            }
            mMessageChatsList.add (new MessageChat (
                                                           MessageChat.MSG_TYPE_TEXT,
                                                           MessageChat.MSG_STATE_SUCCESS,
                                                           id,
                                                           msg.getBody (),
                                                           isMe,
                                                           true,
                                                           msg.getCreatedAt ()));
        }
        mAdapter.notifyDataSetChanged (); // update adapter
        // Scroll to the bottom of the eventList on initial load
        if (messagesFirstLoad) {
            chatListView.setSelection (mAdapter.getCount () - 1);
            messagesFirstLoad = false;
        }
    }

    public void updateMessageRoomItemInBackGround(final Message message) {
        String senderType = "";
        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
            senderType = "Customer : ";
        } else if (GlobalVariables.IS_PRODUCER) {
            senderType = "Producer : ";
        }
        final String senderTypeFinal = senderType;
        saveRoomData (room, senderTypeFinal, message);
    }

    private void saveRoomData(Room room, String senderTypeFinal, Message message) {
        room.setLastMessage (senderTypeFinal + message.getBody ());
        room.saveInBackground ();
    }

    @Override
    public void onPause() {
        super.onPause ();
        //handler.removeCallbacks (runnable);
        room = null;
    }

    @Override
    public void onResume() {
        super.onResume ();
        if(room == null) {
            room = getRoomObject ();
        }
        //handler.postDelayed (runnable, 0);
    }

    public void oOpenFacebookIntent(View view) {
        startActivity (getOpenFacebookIntent ());
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

    private Room getRoomObject() {
        ParseQuery<Room> query = ParseQuery.getQuery ("Room");
        query.whereEqualTo ("producer_id", eventInfo.getProducerId ());
        query.whereEqualTo ("customer_id", customerPhone);
        query.whereEqualTo ("eventObjId", eventInfo.getParseObjectId ());
        query.orderByDescending ("createdAt");
        try {
            List<Room> roomList = query.find ();
            if (roomList.size () == 0) {
                Room room = new Room ();
                ParseACL parseACL = new ParseACL ();
                parseACL.setPublicWriteAccess (true);
                parseACL.setPublicReadAccess (true);
                room.setACL (parseACL);
                room.setCustomer_id (customerPhone);
                room.setProducer_id (eventInfo.getProducerId ());
                room.setEventObjId (eventInfo.getParseObjectId ());
                return room;
            } else {
                Room room = roomList.get (0);
                return room;
            }
        } catch (ParseException e) {
            e.printStackTrace ();
        }
        return null;
    }




    /**
     * Created by benjamin on 04/05/2016.
     */
    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        messageService.removeMessageClientListener(myMessageClientListener);
        super.onDestroy();
    }


    private MessageChat fromMeessageToMessageChat(Message messages,String from) {
            Message msg = messages;
            String id = msg.getUserId ();
            boolean isMe = false;
            if (!GlobalVariables.IS_PRODUCER) {
                if (id.equals (customerPhone)) {
                    isMe = true;
                } else {
                    id = "Producer # " + id;
                }
            } else {
                if (id.equals (eventInfo.getProducerId ())) {
                    isMe = true;
                } else {
                    id = "Customer " + id;
                }
            }
        if(from.equals("onIncomingMessage"))isMe=false;
            return new MessageChat (
                    MessageChat.MSG_TYPE_TEXT,
                    MessageChat.MSG_STATE_SUCCESS,
                    id,
                    msg.getBody (),
                    isMe,
                    true,
                    msg.getCreatedAt ());

    }

    private void updateAdapterFromSinch(MessageChat messageChat)
    {
        mMessageChatsList.add(messageChat);
        mAdapter.notifyDataSetChanged (); // update adapter
        // Scroll to the bottom of the eventList on initial load
            chatListView.setSelection(mAdapter.getCount() - 1);
            messagesFirstLoad = false;
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(myMessageClientListener);
            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(), "ServiceConnection line 382", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;

        }
    }



    public class MyMessageClientListener implements MessageClientListener
    {
        //Notify the user if their message failed to send
        @Override
        public void onMessageFailed(MessageClient messageClient, com.sinch.android.rtc.messaging.Message message, MessageFailureInfo messageFailureInfo) {
            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(), "Message failed to send.", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onIncomingMessage(MessageClient messageClient, com.sinch.android.rtc.messaging.Message message) {
            //Display an incoming message
            Message parseMessage = new Message ();
            parseMessage.setBody (message.getTextBody());
            if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
                parseMessage.setUserId (customerPhone);
            } else {
                parseMessage.setUserId (eventInfo.getProducerId ());
            }
            parseMessage.setCustomer (customerPhone);
            parseMessage.setProducer (eventInfo.getProducerId ());
            parseMessage.setEventObjectId (eventInfo.getParseObjectId ());
            updateAdapterFromSinch(fromMeessageToMessageChat(parseMessage,"onIncomingMessage"));
           Log.e("onIncomingMessage",message.getTextBody());
            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(), message.getTextBody(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onMessageSent(MessageClient client, com.sinch.android.rtc.messaging.Message message, String recipientId) {
            //Display the message that was just sent
            //Later, I'll show you how to store the
            //message in Parse, so you can retrieve and
            //display them every time the conversation is opened
            Message parseMessage = new Message ();
            parseMessage.setBody (message.getTextBody());
            if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
                parseMessage.setUserId (customerPhone);
            } else {
                parseMessage.setUserId (eventInfo.getProducerId ());
            }
            parseMessage.setCustomer (customerPhone);
            parseMessage.setProducer (eventInfo.getProducerId ());
            parseMessage.setEventObjectId (eventInfo.getParseObjectId ());
            parseMessage.put("sinchId", message.getMessageId());
            parseMessage.saveInBackground();
            updateAdapterFromSinch(fromMeessageToMessageChat(parseMessage,"onMessageSent"));





            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(), "onMessageSent="+message.getTextBody(), Toast.LENGTH_LONG).show();
            Log.e("onMessageSent",message.getTextBody());
           // final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
            //only add message to parse database if it doesn't already exist there
            /*arseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject("Message");
                            parseMessage.put("userId", customerPhone);
                            parseMessage.put("producer", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();
                          //  messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                        }
                    }
                }
            });*/
        }

        //Do you want to notify your user when the message is delivered?
        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo)
        {
            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(),"onMessageDelivered line 441", Toast.LENGTH_LONG).show();
        }

        //Don't worry about this right now
        @Override
        public void onShouldSendPushData(MessageClient client,com.sinch.android.rtc.messaging.Message message, List<PushPair> pushPairs)
        {
            Toast.makeText(ChatActivity.this.getApplication().getBaseContext(),"onShouldSendPushData line 448", Toast.LENGTH_LONG).show();
        }
    }

}
