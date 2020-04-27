package com.nearby.whatsnearby.adapters;

import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.utilities.Utils;
import com.zhpan.bannerview.holder.ViewHolder;
import com.zhpan.bannerview.provider.ViewStyleSetter;

public class PlacesImageAdapterHolder implements ViewHolder<String> {
    @Override
    public int getLayoutId() {
        return R.layout.gallery_image_item;
    }

    @Override
    public void onBind(View itemView, String data, int position, int size) {
        AppCompatImageView imageView = itemView.findViewById(R.id.banner_image);
        ViewStyleSetter v = new ViewStyleSetter(imageView);
        v.setRoundRect(imageView.getContext().getResources().getDimensionPixelOffset(R.dimen.dp_5));
        if (data != null) {
            String imageURL = Utils.getInstance()
                    .getPlaceImagesUrl(imageView.getContext(), data);
            Log.i("PlacesImageAdapter", "Place image url --> " + imageURL);
            downloadImages(imageURL, imageView);
        } else {
            imageView.setImageResource(R.mipmap.placeholder_image);
        }
    }

    private void downloadImages(String imageUrl, AppCompatImageView imageView) {
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                } else {
                    imageView.setImageResource(R.drawable.ic_image_black_24dp);
                }
            }
        });
    }
}