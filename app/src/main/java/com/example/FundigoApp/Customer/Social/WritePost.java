package com.example.FundigoApp.Customer.Social;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.FundigoApp.R;
import com.parse.ParseException;

public class WritePost extends AppCompatActivity implements View.OnClickListener
{

    Button finish;
    EditText text;
    ForumPost forumPost;
    ResponsePost responsePost;
    String from,postObjectId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        finish = (Button)findViewById(R.id.finishWriteNewPost);
        text = (EditText)findViewById(R.id.editTextNewPost);
        from = getIntent().getStringExtra("comeFrom");
        text.setHint(from);
        if(from.equals("Write Response"))
        {
            responsePost = new ResponsePost();
            postObjectId = getIntent().getStringExtra("objectId");
        }
        else
        {
            forumPost = new ForumPost();
        }

        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.finishWriteNewPost:
                if(text.getText().length()>0)
                {
                    if(forumPost!=null)
                    {
                        forumPost.setPost(text.getText().toString());
                        try {
                            forumPost.save();
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        responsePost.setResponsePost(text.getText().toString());
                        responsePost.setMyPost(postObjectId);
                        responsePost.setFatherPost(postObjectId);
                        try {
                            responsePost.save();
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (ParseException e){e.printStackTrace();}

                    }
                }
                else
                {
                    Toast.makeText(this,"Need Write SomeThing",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
