package com.example.FundigoApp.Customer.Social;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Benjamin on 01/05/2016.
 */
public class myPostAdapter extends BaseAdapter
{
    ResponsePost responsePost;
    Context context;
    List<ParseObject> PostObjectsList = new ArrayList<>();

    public myPostAdapter(Context context, List<ParseObject> message,ResponsePost responsePost) {
        this.context = context;
        this.PostObjectsList = message;
        this.responsePost=responsePost;

    }
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View row = view;
        final ForumHolder holder;
        if (row == null) {
            LayoutInflater inflator = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
            row = inflator.inflate (R.layout.list_of_post_forum, viewGroup, false);
            holder = new ForumHolder (row);
            row.setTag (holder);
        } else {
            holder = (ForumHolder) row.getTag ();
        }

        final String mes = PostObjectsList.get (i).getString ("Post");
        holder.message.setText (mes);

        holder.response.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.send.setVisibility(View.VISIBLE);
                holder.responseText.setVisibility(View.VISIBLE);

            }
        });

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.responseText.getText().length()>0)
                {
                    responsePost.setResponsePost(holder.responseText.getText().toString());
                    responsePost.setMyPost(PostObjectsList.get(i).getObjectId());
                    responsePost.setFatherPost(PostObjectsList.get(i).getObjectId());
                    ParseQuery<ParseObject> query =new ParseQuery<ParseObject>("ForumPost");
                    query.whereEqualTo("objectId",PostObjectsList.get(i).getObjectId());

                    try {
                        List<ParseObject> object=query.find();
                        object.get(0).put("updatedAt",new Date());
                        object.get(0).save();
                        responsePost.save();
                        holder.responseText.setText("");
                        holder.send.setVisibility(View.INVISIBLE);
                        holder.responseText.setVisibility(View.INVISIBLE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        // Add listener for edit text
        holder.responseText
                .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus)
                    {
                        /*
                         * When focus is lost save the entered value for
                         * later use
                         */
                        if (!hasFocus)
                        {
                            int itemIndex = v.getId();
                            String enteredPrice = ((EditText) v).getText()
                                    .toString();
                        }
                    }
                });

    row.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, myResponsePost.class);
            intent.putExtra("id", PostObjectsList.get(i).getObjectId());
            intent.putExtra("message",PostObjectsList.get(i).getString("Post"));
            context.startActivity(intent);
        }
    });
        return row;
    }
    @Override
    public int getCount() {
        return PostObjectsList.size ();
    }

    @Override
    public Object getItem(int i) {
        return PostObjectsList.get (i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
