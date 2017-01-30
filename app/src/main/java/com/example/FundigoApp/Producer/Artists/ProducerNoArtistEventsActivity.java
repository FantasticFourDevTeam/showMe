package com.example.FundigoApp.Producer.Artists;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.FundigoApp.Events.EditEventActivity;
import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.Events.EventsListAdapter;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.Producer.ProducerSendPuchActivity;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.FilterMethods;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ProducerNoArtistEventsActivity extends android.support.v4.app.Fragment implements AdapterView.OnItemClickListener,EventDataMethods.GetEventsDataCallback {
    private static List<EventInfo> eventsList = new ArrayList<EventInfo>();
    ListView eventsListView;
    private static EventsListAdapter eventsListAdapter;
   // TextView artistTV;
    String eventObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_noartist_events, container, false);
        eventsListView = (ListView) rootView.findViewById(R.id.noArtistEventList);
        eventsListAdapter = new EventsListAdapter(getActivity(),
                eventsList,
                false);
        eventsListView.setAdapter(eventsListAdapter);
        eventsListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        eventsListView.setOnItemClickListener(this);
        FilterMethods.filterEventsByArtist(GlobalVariables.No_Artist_Events,
                eventsList);
        eventsListAdapter.notifyDataSetChanged();
        registerForContextMenu(eventsListView);

        eventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //Assaf added /09/10 to support long click and mark the event
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                eventsListAdapter.notifyDataSetChanged();
                return false;/////
            }
        });

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) {
        Bundle b = new Bundle();
        Intent intent = new Intent(getActivity(), EventPageActivity.class);
        EventDataMethods.onEventItemClick(i, eventsList, intent);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        GeneralStaticMethods.onActivityResult(requestCode,
                data,
                this.getActivity());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //The menu Appear when Prodcuer stand on the Event frame and press the Event long time
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(GlobalVariables.refreshArtistsList){
            GlobalVariables.refreshArtistsList = false;
            Intent intent = new Intent (this.getActivity (), EventPageActivity.class);
            EventDataMethods.downloadEventsData(ProducerNoArtistEventsActivity.this, GlobalVariables.PRODUCER_PARSE_OBJECT_ID, this.getContext(), intent);
        }
    }

    @Override
    public void eventDataCallback() {
        EventDataMethods.uploadArtistData();
        eventsList.clear();
        FilterMethods.filterEventsByArtist(GlobalVariables.No_Artist_Events,
                eventsList);
        eventsListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int pos = info.position;

        switch (item.getItemId()) {
            case R.id.delete_event:
                eventObjectId = eventsList.get(pos).getParseObjectId();
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //deleteEvent(eventObjectId);
                                cancelEvent(eventObjectId);
                                GlobalVariables.refreshArtistsList = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEUTRAL: // open Push notifications to users before event delete
                                Intent intent = new Intent(getActivity(), ProducerSendPuchActivity.class);
                                intent.putExtra("id",eventObjectId);
                                startActivity(intent);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder (this.getActivity());
                builder.setTitle (getString(R.string.going_to_delete)+ eventsList.get(pos).getName());
                builder.setIcon(R.drawable.warning);
                builder.setMessage(getString(R.string.are_you_sure));
                builder.setPositiveButton(getString(R.string.yes), listener);
                builder.setNegativeButton(getString(R.string.no), listener);
                builder.setNeutralButton(getString(R.string.send_push), listener);
                AlertDialog dialog = builder.create ();
                dialog.show ();
                return true;
            case R.id.edit_event:
                eventObjectId = eventsList.get (pos).getParseObjectId ();
                DialogInterface.OnClickListener listenerEdit = new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent (getActivity(), EditEventActivity.class);
                                intent.putExtra(GlobalVariables.OBJECTID, eventObjectId);
                                startActivity(intent);
                                GlobalVariables.refreshArtistsList = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss ();
                                break;
                            case DialogInterface.BUTTON_NEUTRAL: // open Push notifications to users before event delete
                                Intent intentPush = new Intent(getActivity(), ProducerSendPuchActivity.class);
                                intentPush.putExtra("id",eventObjectId);
                                startActivity(intentPush);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builderEdit = new AlertDialog.Builder (this.getActivity());
                builderEdit.setIcon(R.drawable.warning);
                builderEdit.setMessage(getString(R.string.are_you_sure_edit_event));
                builderEdit.setPositiveButton(getString(R.string.yes), listenerEdit);
                builderEdit.setNegativeButton(getString(R.string.no), listenerEdit);
                builderEdit.setNeutralButton(getString(R.string.send_push), listenerEdit);
                AlertDialog dialogEdit = builderEdit.create ();
                dialogEdit.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void cancelEvent(final String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery ("Event");
        query.whereEqualTo("objectId", objectId);
        try {
            ParseObject parseObject = query.getFirst();
            parseObject.put("eventCanceled",true);
            parseObject.save ();
        } catch (ParseException e1) {
            e1.printStackTrace ();
        }
    }



  /*  public void deleteEvent(final String objectId) { // Delet option was canceled for now
        ParseQuery<ParseObject> query = ParseQuery.getQuery ("Event");
        query.whereEqualTo ("objectId", objectId);
        query.orderByDescending("createdAt");
        try {
            ParseObject parseObject = query.getFirst ();
            parseObject.delete ();
            parseObject.save ();
            for (int i = 0; i < eventsList.size (); i++) {
                EventInfo eventInfo = eventsList.get (i);
                if (eventInfo.getParseObjectId ().equals (objectId)) {
                    eventsList.remove (i);
                    eventsListAdapter.notifyDataSetChanged ();
                    break;
                }
            }
        } catch (ParseException e1) {
            e1.printStackTrace ();
        }

        ParseQuery<ParseObject> querySeats = ParseQuery.getQuery ("EventsSeats");
        querySeats.whereEqualTo ("eventObjectId", objectId);
        querySeats.findInBackground (new FindCallback<ParseObject> () {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size () != 0) {
                    ParseObject.deleteAllInBackground (objects);
                }
            }
        });
        ParseQuery<ParseObject> queryMessages = new ParseQuery("Message");
        queryMessages.whereEqualTo("eventObjectId",objectId);
        queryMessages.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (objects.size() != 0) {
                    ParseObject.deleteAllInBackground(objects);
                }
            }
        });
        ParseQuery<ParseObject> queryRoom = new ParseQuery("Room");
        queryRoom.whereEqualTo("eventObjId", objectId);
        queryRoom.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() != 0) {
                    ParseObject.deleteAllInBackground(objects);
                }
            }
        });
        ParseQuery<ParseObject> queryMsgRealTime = new ParseQuery("MsgRealTime");
        queryMsgRealTime.whereEqualTo("eventObjectId", objectId);
        queryMsgRealTime.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() != 0) {
                    ParseObject.deleteAllInBackground(objects);
                }
            }
        });
        ParseQuery<ParseObject> queryPush = new ParseQuery("Push");
        queryPush.whereEqualTo("EventId", objectId);
        queryPush.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() != 0) {
                    ParseObject.deleteAllInBackground(objects);
                }
            }
        });
        getActivity().finish();
    }*/

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) { // refresh the fragment When swipe to it
//        super.setUserVisibleHint(isVisibleToUser);
//
//        if (isVisibleToUser)
//        {
//            if (getView()!=null)
//            {
//
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//                Fragment _fragment = new ProducerNoArtistEventsActivity();
//                fragmentTransaction.replace(R.id.noArtistFragment,_fragment);
//                fragmentTransaction.commit();
//            }
//        }
//    }



}
