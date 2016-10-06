package com.example.FundigoApp.Filter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;

public class FilterImageAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mNames;
    private String[] mFilter;
    private Integer[] mImages;

    public FilterImageAdapter(Context c, String[] names, Integer[] images,String[] filter) {
        mContext = c;
        this.mImages = images;
        this.mNames = names;
        this.mFilter = filter;
    }

    @Override
    public int getCount() {
        return mImages.length;
    }

    @Override
    public Object getItem(int position) {
        //return null;
        return mNames[position];//Assaf updated.
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate (R.layout.grid_layout, null);

        } else {
            grid = (View) convertView;
        }
        //24.09 - Assaf updated
        TextView textView = (TextView) grid.findViewById (R.id.grid_text);
        ImageView imgView = (ImageView) grid.findViewById (R.id.grid_image);
        textView.setText (this.mNames[position]);
        imgView.setImageResource (this.mImages[position]);
        ////////
        if(mFilter!=null) {
               if (!GlobalVariables.CURRENT_FILTER_NAME.isEmpty() && this.mFilter[position].equals(GlobalVariables.CURRENT_FILTER_NAME)) {
                   grid.setBackgroundColor(Color.RED);
                 } else { //05.10 assaf - added to remove duplicate background Red when scroll the Grid
                    grid.setBackgroundColor(Color.WHITE);
                   }
               }
        return grid;
    }
}
