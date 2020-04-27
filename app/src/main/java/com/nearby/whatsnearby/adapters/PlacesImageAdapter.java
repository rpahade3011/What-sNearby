package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.utilities.Utils;

/**
 * Created by rudhraksh.pahade on 2/7/2017.
 */

public class PlacesImageAdapter extends PagerAdapter {

    private Context context;
    private String imageId[];
    private LayoutInflater inflater;

    private ImageLoader imageLoader;
    private ImageView imageView;

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
        imageView = rootView.findViewById(R.id.banner_image);
        if (imageId.length > 0) {
            String imageURL = Utils.getInstance().getPlaceImagesUrl(context, imageId[position]);
            Log.i("PlacesImageAdapter", "Place image url --> " + imageURL);
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
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                } else {
                    imageView.setImageResource(R.mipmap.placeholder_image);
                }
            }
        });
    }
}