package com.example.FundigoApp.Customer.Social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class myResponsePost extends AppCompatActivity implements View.OnClickListener {

    List<ParseObject>response = new ArrayList<>();
    List<ParseObject>responseChildren = new ArrayList<>();
    String postId,message="1234";
    ListView listView;
    myResponsePostAdapter adapter;
    ResponsePost responsePost;
    Button writeResponse;
    TextView textMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_response_post);
        listView = (ListView) findViewById(R.id.listViewOfRseponseForum);
        textMessage = (TextView)findViewById(R.id.postMessageInResponseActivity);
        writeResponse =(Button)findViewById(R.id.newResponse_myResponse);
        responsePost = new ResponsePost();
        postId = getIntent().getStringExtra("id");
        Toast.makeText(this,postId,Toast.LENGTH_SHORT).show();
        message = getIntent().getStringExtra("message");
        textMessage.setText(message);
        getResponse();
        adapter = new myResponsePostAdapter(this,response,responsePost);
        listView.setAdapter(adapter);
        listView.setSelector (new ColorDrawable(Color.TRANSPARENT));
        writeResponse.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.newResponse_myResponse:
                Intent intent =new Intent(this,WritePost.class);
                intent.putExtra("comeFrom","Write Response");
                intent.putExtra("objectId",postId);
                startActivity(intent);
                break;
        }
    }

    public void getResponse()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery ("PostResponse");
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery ("PostResponse");
        //ParseQuery querySecond = new ParseQuery("ForumPost");
        //querySecond.whereEqualTo("objectId",postId);
        //query.whereMatchesQuery("postFather",querySecond);
        query1.whereNotEqualTo("FatherPost",postId);
        query1.whereEqualTo("MyPost",postId);
        query1.orderByAscending("updatedAt");
        query.whereEqualTo("MyPost",postId);
        query.whereEqualTo("FatherPost",postId);
        query.orderByDescending(  "updatedAt");

        try {
            response.clear();
            responseChildren.clear();
            response.addAll(query.find());
            responseChildren.addAll(query1.find());
            //Toast.makeText(this,""+response.size(),Toast.LENGTH_LONG).show();
            //ForumPost forumPost =new ForumPost();
            //forumPost.setPost(message);
            //response.add(0,forumPost);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        orderResponseMessage();
    }

    public void orderResponseMessage()
    {

        Boolean [] choose = new Boolean[responseChildren.size()];
        Arrays.fill(choose,Boolean.FALSE);
        int responseSize = response.size();
        int responseChildreanSize = responseChildren.size();
        List<ParseObject> responseSorted= new ArrayList<>();

        for (int i = 0; i < responseSize; i++)
        {
            responseSorted.add(response.get(i));
            String id =response.get(i).getObjectId();
            for (int j = responseChildreanSize-1; j >= 0  ; j--)
            {
                if(responseChildren.get(j).getString("FatherPost").equals(id))
                {
                    responseSorted.add(responseChildren.get(j));
                    choose[j] = true;
                }
            }
        }

        for(int i = 0; i < choose.length; i++)
        {
            if(!choose[i])
            {
                ParseObject add = responseChildren.get(i);
                String id =add.getString("FatherPost");
                for(int j = 0 ; j<responseSorted.size(); j++)
                {
                    if(responseSorted.get(j).getObjectId().equals(id))
                    {
                        responseSorted.add(j+1,add);
                        choose[i]=true;
                    }
                }
            }
        }
        response = responseSorted;
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        getResponse();
        adapter.notifyDataSetChanged();
    }
}