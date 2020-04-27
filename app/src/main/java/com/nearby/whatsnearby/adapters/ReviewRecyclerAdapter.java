package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.services.AppController;
import com.nearby.whatsnearby.utilities.Utils;

public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.ReviewHolder> {
    private PlaceDetailBean.Review[] reviews;
    private Context context;
    private String colors = "#30d1d5";
    private ImageLoader imageLoader;

    public ReviewRecyclerAdapter(PlaceDetailBean.Review[] reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.review_fragment_item, parent, false);
        return new ReviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
        bindData(holder, position);
    }

    @Override
    public int getItemCount() {
        return reviews.length;
    }

    private void bindData(ReviewHolder holder, int position) {
        holder.author_name.setText(reviews[position].getAuthor_name());
        holder.author_text.setText(reviews[position].getAuthor_text());
        holder.relative_time_description.setText(reviews[position].getRelative_time_description());
        Drawable ratingStars = holder.author_rating.getProgressDrawable();
        DrawableCompat.setTint(ratingStars, Color.parseColor("#30d1d5"));
        holder.author_rating.setRating(reviews[position].getRating());
        holder.icon.setScaleType(ImageView.ScaleType.CENTER);
        GradientDrawable gd = (GradientDrawable) holder.icon.getBackground().getCurrent();
        gd.setColor(Color.parseColor(colors));

        if (reviews[position].getAuthor_url() != null && reviews[position].getAuthor_url().length() > 24) {
            String author_url = Utils.getInstance().getAuthorReviewsImageUrl(context, reviews[position].getAuthor_url().substring(24));
            Log.i("ReviewRecyclerAdapter", "Author Reviews Image Url --> " + author_url);
            try {
                downloadAuthorImages(author_url, holder.icon);
            } catch (Exception ex) {
                Log.e("ReviewAdapter", ex.getMessage());
            }
        }
    }

    private void downloadAuthorImages(String imageUrl, ImageView imageView) {
        imageLoader = AppController.getInstance().getImageLoader();
        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                }
            }
        });
    }

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView author_name;
        private TextView author_text;
        private TextView relative_time_description;
        private RatingBar author_rating;

        public ReviewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.author_icon);
            author_name = itemView.findViewById(R.id.author_name);
            author_text = itemView.findViewById(R.id.author_text);
            relative_time_description = itemView.findViewById(R.id.relative_time_description);
            author_rating = itemView.findViewById(R.id.author_rating);
        }
    }
}