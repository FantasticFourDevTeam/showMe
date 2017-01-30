package com.example.FundigoApp.Events;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.R;

public class EventListHolder {

    ImageView image;
    TextView date;
    TextView name;
    TextView tags;
    TextView price;
    TextView place;
    TextView address;
    ImageView saveEvent;
    TextView expiredTag;
    TextView canceledTag;


    public EventListHolder(View v) {
        image = (ImageView) v.findViewById (R.id.imageView);
        date = (TextView) v.findViewById (R.id.event_date);
        name = (TextView) v.findViewById (R.id.event_name_tv);
        tags = (TextView) v.findViewById (R.id.tags);
        price = (TextView) v.findViewById (R.id.event_price);
        place = (TextView) v.findViewById (R.id.event_location);
        saveEvent = (ImageView) v.findViewById (R.id.imageView3);
        address = (TextView) v.findViewById (R.id.event_address);
        expiredTag = (TextView)v.findViewById(R.id.event_expired);
        canceledTag = (TextView)v.findViewById(R.id.event_canceled);
    }
}