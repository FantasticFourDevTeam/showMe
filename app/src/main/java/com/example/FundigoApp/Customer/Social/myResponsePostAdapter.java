package com.example.FundigoApp.Customer.Social;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.example.FundigoApp.R;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin on 04/05/2016.
 */
public class myResponsePostAdapter extends BaseAdapter
{
    ResponsePost responsePost;
    Context context;
    List<ParseObject> PostObjectsList = new ArrayList<>();

    public myResponsePostAdapter(Context context, List<ParseObject> message,ResponsePost responsePost) {
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

        final String mes = PostObjectsList.get (i).getString ("Response");
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
                    responsePost.setMyPost(PostObjectsList.get(i).getString("MyPost"));
                    responsePost.setFatherPost(PostObjectsList.get(i).getObjectId());
                    try {
                        responsePost.save();
                        holder.responseText.setText("");
                        holder.send.setVisibility(View.INVISIBLE);
                        holder.responseText.setVisibility(View.INVISIBLE);

                        myResponsePostAdapter.this.notifyDataSetChanged();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

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

        /*row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, myResponsePost.class);
                intent.putExtra("numberPost", i*//*PostObjectsList.get(i).getNumber("NumberPost")*//*);
                context.startActivity(intent);
            }
        });*/

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
