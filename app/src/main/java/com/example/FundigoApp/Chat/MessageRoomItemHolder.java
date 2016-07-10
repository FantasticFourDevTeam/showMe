package com.example.FundigoApp.Chat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.R;

public class MessageRoomItemHolder {
    ImageView customerOrEventImage;
    TextView messageBody;
    TextView customerOrEventName;

    public MessageRoomItemHolder(View v) {
        customerOrEventImage = (ImageView) v.findViewById (R.id.message_itemIV);
        customerOrEventName = (TextView) v.findViewById (R.id.message_item_nameTV);
        messageBody = (TextView) v.findViewById (R.id.message_item_bodyTV);
    }
}
