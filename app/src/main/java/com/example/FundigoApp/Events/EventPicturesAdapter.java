package com.example.FundigoApp.Events;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EventPicturesAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mImages;
    private ImageLoader loader;

    public EventPicturesAdapter(Context c, String[] images) {
        mContext = c;
        this.mImages = images;
        loader = FileAndImageMethods.getImageLoader(mContext);
    }

    @Override
    public int getCount() {
        return mImages.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid=null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        try {
            if (convertView == null) {
                //01.01 - Assaf updated
                grid = inflater.inflate(R.layout.event_pictures_grid_view_adapter,parent,false);
                ImageView imgView = (ImageView) grid.findViewById(R.id.grid_producers_images);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgView.setPadding(8,8,8,8);
                //01.01 - until here//
                if (getCount() != 0 && mImages[0] != "no images" ) {
                    loader.displayImage(this.mImages[position], imgView);
                }
                if (mImages[0] == "no images" && getCount()==1) {
                    imgView.setImageResource(R.drawable.no_images);
                }
            } else {
                grid = convertView;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return grid;
    }

}

