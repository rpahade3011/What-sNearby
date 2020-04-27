package com.nearby.whatsnearby.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.constants.PlacesConstants;


/**
 * Created by rudhraksh.pahade on 11-07-2016.
 */

public class TilesFormatter extends BaseAdapter {

    private Context mContext;
    LayoutInflater inflater;

    PlacesConstants places = new PlacesConstants();
    String colors[] = {"#ffb300", "#2196f3", "#0277bd", "#e65100", "#3f51b5", "#004d40", "#4caf50",
            "#ffc107", "#607d8b", "#e91e63", "#3f51b5", "#9c27b0", "#673ab7"};

    public TilesFormatter(Context c) {
        this.mContext = c;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return places.places_list.length;
    }

    @Override
    public Object getItem(int i) {
        return places.places_list[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String place_id = places.places_list[position];
        String icon_id = places.places.get(place_id);

        convertView = inflater.inflate(R.layout.places_grid_item, parent, false);
        if(convertView == null){

        }else {
            try {
                ImageView place_img = convertView.findViewById(R.id.place_img);
                TextView place_text = convertView.findViewById(R.id.place_text);
                place_img.setBackgroundColor(Color.parseColor(colors[position % 13]));
                if (icon_id != null) {
                    Drawable drawable = mContext.getResources().getDrawable(getDrawable(mContext, icon_id));
                    place_img.setImageDrawable(drawable);
                }
                if(place_id.equalsIgnoreCase("local_government_office")){
                    place_id = "government_office";
                }
                if(place_id.equalsIgnoreCase("grocery_or_supermarket")){
                    place_id = "supermarket";
                }
                place_text.setText(place_id.toUpperCase().replace("_", " "));
            } catch (Exception e) {
                Log.e("Places", place_id);
            }
        }
        // Animating each child of grid adapter
        if (convertView != null) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.error_x_in);
            convertView.setAnimation(animation);
        }
        return convertView;
    }

    private static int getDrawable(Context context, String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}