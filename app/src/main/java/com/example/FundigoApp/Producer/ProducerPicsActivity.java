package com.example.FundigoApp.Producer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;

import com.example.FundigoApp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class ProducerPicsActivity extends AppCompatActivity {
    private static GridView picsGridView;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prodcuer_pics_page);
        picsGridView = (GridView) findViewById(R.id.producerPicsGridView);
        Intent picViewIntent = getIntent();
        eventId = picViewIntent.getStringExtra("eventID");
        if (eventId != null)
            getEventPictures();

    }

    private void getEventPictures() {
        ParseQuery innerQuery = new ParseQuery("Event");
        innerQuery.whereEqualTo("objectId", eventId);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventsPictures");
        query.whereMatchesQuery("eventPointer", innerQuery);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> List, ParseException e) {
                int i = 0;
                String[] Images={"no images"};
                if (e == null) {
                    if (List.size()!=0) {
                        Images = new String[List.size()];
                        for (ParseObject obj : List) {
                            Images[i] = obj.getParseFile("eventPicture").getUrl();
                            i++;
                        }
                    }
                    Log.d("picture", "Retrieved " + List.size() + " picture");
                }
                else {
                    Log.d("pictures not retrieved", "Error: " + e.getMessage());
                }
                /** Create the Custom Grid View*/
                       ProdcurePicsAdpater adapter = new ProdcurePicsAdpater(ProducerPicsActivity.this, Images);
                       picsGridView.setAdapter(adapter);
            }
        });
    }
}