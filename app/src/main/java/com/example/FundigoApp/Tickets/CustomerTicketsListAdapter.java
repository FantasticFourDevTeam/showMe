package com.example.FundigoApp.Tickets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.GeneralStaticMethods;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CustomerTicketsListAdapter extends ArrayAdapter<EventsSeatsInfo> {
    SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM dd, yyyy, hh:mm a", Locale.getDefault());

    public CustomerTicketsListAdapter(Context context, int resource, List objects) {
        super (context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            convertView = LayoutInflater.from (getContext ()).inflate (R.layout.content_events_tickets, parent, false);
            EventsSeatsInfo eventsSeatsInfo = (EventsSeatsInfo) getItem (position);

            if (eventsSeatsInfo != null) {
                TextView eventName = (TextView) convertView.findViewById (R.id.eventName);
                TextView ticketNameBody = (TextView) convertView.findViewById (R.id.ticketName);
                TextView ticketNameTitle = (TextView) convertView.findViewById (R.id.seatNameTitle);
                TextView eventDate = (TextView) convertView.findViewById (R.id.eventDate);
                TextView price = (TextView) convertView.findViewById (R.id.price);
                Button listViewButton = (Button) convertView.findViewById (R.id.moreDetailesButton);
                Button eventEndedButton = (Button) convertView.findViewById (R.id.eventEnded);
                TextView purchaseDate = (TextView) convertView.findViewById (R.id.purchaseDate);
                RelativeLayout seatLayout = (RelativeLayout)convertView.findViewById(R.id.seatLinearLayout);

                String priceString = String.valueOf (eventsSeatsInfo.getPrice ());
                eventName.setText (eventsSeatsInfo.getEventInfo().getName());
                eventDate.setText (eventsSeatsInfo.getEventInfo().getDateAsString());
                if (GeneralStaticMethods.getLanguage()) //27.10 - assaf presnet dates in Hebrew
                   purchaseDate.setText(GeneralStaticMethods.getDateToStringConversion(eventsSeatsInfo.getPurchaseDate()));
                else
                   purchaseDate.setText(dateFormat.format(eventsSeatsInfo.getPurchaseDate()));
                price.setText (priceString);
                listViewButton.setTag (position);

                String seatName = eventsSeatsInfo.getTicketName (); // for a case that No Seat Same , just regular Ticket
                if (seatName == null || seatName.isEmpty ()) {
                    seatLayout.setVisibility(View.GONE);
                    ticketNameBody.setVisibility (View.GONE);
                    ticketNameTitle.setVisibility (View.GONE);
                } else {
                    ticketNameBody.setText (eventsSeatsInfo.getTicketName ());
                }
                if(!eventsSeatsInfo.getEventInfo ().isFutureEvent () && !eventsSeatsInfo.getEventInfo().getIsCanceled()){
                    eventEndedButton.setVisibility(View.VISIBLE);
                }
                else if(eventsSeatsInfo.getEventInfo().getIsCanceled()){
                    eventEndedButton.setVisibility (View.VISIBLE);
                    eventEndedButton.setText(R.string.event_cancelation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return convertView;
    }
}
