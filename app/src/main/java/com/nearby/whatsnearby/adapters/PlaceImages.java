package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nearby.whatsnearby.R;
import com.squareup.picasso.Picasso;

public class PlaceImages extends BaseAdapter {

    String baseurl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
    String key = "AIzaSyBg-iwzAjavEUVV9hOQUr0JljZHL7XFRkQ";
    Context context;
    String imageId[];
    LayoutInflater inflater;

    public PlaceImages(Context context, String imageId[]){
        this.context = context;
        this.imageId = imageId;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return imageId.length;
    }

    @Override
    public Object getItem(int position) {
        return imageId[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.gallery_image_item, parent, false);
        }
        ImageView imageView = (ImageView)convertView.findViewById(R.id.galleryItem);
        String imageURL = baseurl + imageId[position] + "&key=" + key;
        Picasso.with(context).load(imageURL).into(imageView);
        return convertView;
    }
}
