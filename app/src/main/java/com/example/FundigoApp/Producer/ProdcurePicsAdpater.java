package com.example.FundigoApp.Producer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethods;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProdcurePicsAdpater extends BaseAdapter {
    private Context mContext;
    private String[] mImages;
    private ImageLoader loader;

    public ProdcurePicsAdpater(Context c, String[] images) {
        mContext = c;
        this.mImages = images;
        loader = StaticMethods.getImageLoader(mContext);

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
                grid = inflater.inflate(R.layout.producer_pics_grid_view, null);
                ImageView imgView = (ImageView) grid.findViewById(R.id.grid_producers_images);
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
            Log.e("TAG" ,ex.getMessage());
        }
          return grid;
        }
    }

