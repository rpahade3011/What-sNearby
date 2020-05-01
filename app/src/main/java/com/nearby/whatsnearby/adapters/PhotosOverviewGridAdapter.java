package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.utilities.Utils;

public class PhotosOverviewGridAdapter extends RecyclerView.Adapter<PhotosOverviewGridAdapter
        .PhotosViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;

    private String[] mPhotosArray;

    public PhotosOverviewGridAdapter(Context c, String[] array) {
        this.mContext = c;
        this.mPhotosArray = array;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.gallery_image_item, parent ,false);
        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        String photoUrl = mPhotosArray[position];
        if (photoUrl != null) {
            String imageURL = Utils.getInstance()
                    .getPlaceImagesUrl(mContext, photoUrl);
            Log.i("PlacesImageAdapter", "Place image url --> " + imageURL);
            downloadImages(imageURL, holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.no_image_96dp);
        }
    }

    @Override
    public int getItemCount() {
        return mPhotosArray.length;
    }

    private void downloadImages(String imageUrl, NetworkImageView imageView) {
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PlacesImageAdapter", "Error loading images:: "
                        + error.getLocalizedMessage());
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                } else {
                    imageView.setImageResource(R.drawable.no_image_96dp);
                }
            }
        });
        imageView.setImageUrl(imageUrl, imageLoader);
    }

    static class PhotosViewHolder extends RecyclerView.ViewHolder {
        private NetworkImageView imageView;

        public PhotosViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.banner_image);
        }
    }
}
