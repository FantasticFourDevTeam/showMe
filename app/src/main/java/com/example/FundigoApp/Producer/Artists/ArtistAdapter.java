package com.example.FundigoApp.Producer.Artists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.FundigoApp.Events.EventInfo;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FilterMethods;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends BaseAdapter {

    List<Artist> artistList = new ArrayList<Artist>();
    Context context;
    LayoutInflater inflater;
    private Artist artist;

    public ArtistAdapter(Context c, List<Artist> artistList) {
        this.context = c;
        this.artistList = artistList;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
   public int getCount() {
       return artistList .size();
   }

    @Override
    public Object getItem(int position) {
        return artistList .get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artist_item, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        artist = (Artist) getItem(position);
        mViewHolder.tvTitle.setText(artist.getName());
        mViewHolder.numOfArtistEvents.setText(Integer.toString(getArtistEvents()));
        return convertView;
    }

    private class MyViewHolder {
        TextView tvTitle;
        TextView numOfArtistEvents;


        public MyViewHolder(View item) {
            tvTitle = (TextView) item.findViewById(R.id.artistName);
            numOfArtistEvents = (TextView)item.findViewById(R.id.numOfArtistEvents);
        }
    }

    private int getArtistEvents() {
        List<EventInfo> eventsListbyArtist = new ArrayList();
        try {
            FilterMethods.filterEventsByArtist(artist.getName(),
                    eventsListbyArtist);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return eventsListbyArtist.size();
    }
}
