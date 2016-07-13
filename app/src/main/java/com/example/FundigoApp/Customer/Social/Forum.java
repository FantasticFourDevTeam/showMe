package com.example.FundigoApp.Customer.Social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.FundigoApp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class Forum extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button newPost;
    private ListView postList;
    ResponsePost responsePost;
    myPostAdapter adapter;
    static ParseObject select;
    List<ParseObject> postObjectsList = new ArrayList<ParseObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        newPost = (Button) findViewById(R.id.newPost_Forum);
        postList = (ListView) findViewById(R.id.listViewOfForum);
        responsePost = new ResponsePost();
        newPost.setOnClickListener(this);

        adapter = new myPostAdapter(this, postObjectsList, responsePost);
        postList.setAdapter(adapter);
        postList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        postList.setOnItemClickListener(this);
        postList.setItemsCanFocus(true);
    }

    public void getPost() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ForumPost");
        query.orderByDescending("updatedAt");
        try
        {
            postObjectsList.clear();
            postObjectsList.addAll(query.find());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newPost_Forum:
                Intent writePost = new Intent(this, WritePost.class);
                writePost.putExtra("comeFrom", "Write Post");
                startActivity(writePost);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> av, View view, int i, long l) {
        Intent intent = new Intent(Forum.this, myResponsePost.class);
        startActivity(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getPost();
        Log.e("onResume", "12345678901234567890");
        Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();

    }

}
