package com.example.FundigoApp.Customer.Social;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.FundigoApp.R;

/**
 * Created by Benjamin on 01/05/2016.
 */
public class ForumHolder
{
    TextView message;
    Button response,send;
    EditText responseText;
    public ForumHolder(View view) {
        message = (TextView) view.findViewById (R.id.MaessagePostList);
        response =(Button)view.findViewById(R.id.responsePost);
        send=(Button)view.findViewById(R.id.sendMessagePost);
        responseText=(EditText)view.findViewById(R.id.responseMessagePost);
    }
}
