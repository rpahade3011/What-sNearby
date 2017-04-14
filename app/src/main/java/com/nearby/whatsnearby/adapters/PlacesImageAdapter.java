package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.services.AppController;

/**
 * Created by rudhraksh.pahade on 2/7/2017.
 */

public class PlacesImageAdapter extends PagerAdapter {

    String basePlaceUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
    String key = "AIzaSyBg-iwzAjavEUVV9hOQUr0JljZHL7XFRkQ";
    Context context;
    String imageId[];
    LayoutInflater inflater;

    ImageLoader imageLoader;
    NetworkImageView imageView;

    public PlacesImageAdapter(Context ctx, String imageId[]) {
        this.context = ctx;
        this.imageId = imageId;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (imageId == null) {
            return 0;
        } else {
            return imageId.length;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View rootView = inflater.inflate(R.layout.gallery_image_item, container, false);
        //ImageView imageView = (ImageView)rootView.findViewById(R.id.galleryItem);
        imageView = (NetworkImageView)rootView.findViewById(R.id.galleryItem);
        if (imageId.length > 0) {
            String imageURL = basePlaceUrl + imageId[position] + "&key=" + key;
            //Picasso.with(context).load(imageURL).into(imageView);
            downloadImages(imageURL);
        } else {
            imageView.setImageResource(R.mipmap.placeholder_image);
        }
        container.addView(rootView);

        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    private void downloadImages(String imageUrl) {
        imageLoader = AppController.getInstance().getImageLoader();
        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageView.setImageBitmap(response.getBitmap());
            }
        });

        imageView.setImageUrl(imageUrl, imageLoader);
    }
}
