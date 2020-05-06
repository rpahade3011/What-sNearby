package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.PlaceBean;
import com.nearby.whatsnearby.services.GpsTracker;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {

    private Context context;
    private List<PlaceBean> list;
    private GpsTracker loc;

    public PlaceListAdapter(Context context, List<PlaceBean> list, GpsTracker loc) {
        this.context = context;
        this.list = list;
        this.loc = loc;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.places_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindDataToList(list.get(position), loc);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private RatingBar rating;
        private TextView address;
        private TextView isOpen;
        private TextView distance;
        private TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            rating = itemView.findViewById(R.id.rating);
            address = itemView.findViewById(R.id.address);
            isOpen = itemView.findViewById(R.id.isOpen);
            distance = itemView.findViewById(R.id.distance);
            time = itemView.findViewById(R.id.time);
        }
        void bindDataToList(PlaceBean bean, GpsTracker loc) {
            name.setText(bean.getName());
            rating.setRating(bean.getRating());
            if (bean.isOpen()) {
                isOpen.setTextColor(Color.parseColor("#2E7D32"));
                isOpen.setText("Currently Open");
            } else {
                isOpen.setTextColor(Color.parseColor("#D50000"));
                isOpen.setText("Currently Closed");
            }
            address.setText(bean.getVicinity());
            double d = countDistance(loc.latitude, loc.longitude, bean.getLatitude(),
                    bean.getLongitude(), "K");
            time.setText(String.format("%.2f", ((d / 5) * 60)) + " min");
            distance.setText((d < 1) ? String.format("%.0f", d * 1000) + " m"
                    : String.format("%.2f", d) + " km");
        }
    }

    private static double countDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}