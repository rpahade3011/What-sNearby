package com.nearby.whatsnearby.fragments.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.ReviewRecyclerAdapter;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.services.AppController;

public class ReviewFragment extends Fragment {
    private static final String NO_REVIEWS_TEXT = "Sorry, no reviews available for this place";

    private View reviewView;
    private PlaceDetailBean.Review[] mDataReviewsArray;

    public ReviewFragment() {}

    public static ReviewFragment getInstance() {
        return new ReviewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataReviewsArray = AppController.getInstance().getReview();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        initializeReviews();
    }

    private void init(View view) {
        reviewView = view.findViewById(R.id.review_frame);
    }

    private void initializeReviews() {
        RecyclerView reviews = reviewView.findViewById(R.id.review_list);
        reviews.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity()
                .getApplicationContext());
        reviews.setLayoutManager(layoutManager);

        PlaceDetailBean.Review[] reviewArray = mDataReviewsArray;

        if (reviewArray != null && reviewArray.length > 0) {
            ReviewRecyclerAdapter reviewsAdapter = new ReviewRecyclerAdapter(reviewArray,
                    getActivity().getApplicationContext());
            reviews.setAdapter(reviewsAdapter);
        } else {
            TextView no_Review = reviewView.findViewById(R.id.no_reviews);
            no_Review.setText(NO_REVIEWS_TEXT);
        }
    }
}
