package com.example.FundigoApp.Producer.Artists;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.FundigoApp.Events.EventPageActivity;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.EventDataMethods;
import com.example.FundigoApp.StaticMethod.EventDataMethods.GetEventsDataCallback;

public class ProducerMainActivity extends android.support.v4.app.Fragment implements GetEventsDataCallback {

    ListView artistListView;
    public static ArtistAdapter artistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate (R.layout.activity_main_producer, container, false);
        artistListView = (ListView) rootView.findViewById (R.id.artist_list_view);
        artistAdapter = new ArtistAdapter (getActivity ().getApplicationContext (), GlobalVariables.artist_list);
        artistListView.setAdapter (artistAdapter);
        artistListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
         Intent intent = new Intent (this.getActivity (), EventPageActivity.class);
         EventDataMethods.downloadEventsData(this, GlobalVariables.PRODUCER_PARSE_OBJECT_ID, this.getContext(), intent);

         artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(getActivity(), ArtistStatsActivity.class);
                intent.putExtra("artist_name", GlobalVariables.artist_list.get(position).getName());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void eventDataCallback() {
        EventDataMethods.uploadArtistDataWithoutNoArtist();//13.10 assaf fixed
        artistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(GlobalVariables.refreshArtistsList){
             GlobalVariables.refreshArtistsList = false;
             Intent intent = new Intent (this.getActivity (), EventPageActivity.class);
             EventDataMethods.downloadEventsData (this, GlobalVariables.PRODUCER_PARSE_OBJECT_ID, this.getContext (), intent);
        }
    }

}