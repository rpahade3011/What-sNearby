package com.nearby.whatsnearby.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.ReviewAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;


public class ReviewFragment extends Fragment {

    private static final String NO_REVIEWS_TEXT = "Sorry, no reviews available for this place";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.review_fragment, container, false);
        ListView reviews = (ListView) view.findViewById(R.id.review_list);
        PlaceDetailBean.Review[] reviewsArray = (PlaceDetailBean.Review[]) getArguments().getSerializable("reviews");
        if (reviewsArray != null && reviewsArray.length > 0) {
            ReviewAdapter reviewsAdapter = new ReviewAdapter(reviewsArray, getActivity());
            reviews.setAdapter(reviewsAdapter);
        } else {
            TextView no_Review = (TextView) view.findViewById(R.id.no_reviews);
            no_Review.setText(NO_REVIEWS_TEXT);
        }
        return view;
    }
}
