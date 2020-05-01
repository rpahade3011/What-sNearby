package com.nearby.whatsnearby.fragments.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PhotosOverviewGridAdapter;
import com.nearby.whatsnearby.services.AppController;

public class PhotosFragment extends Fragment {
    private static final String NO_PHOTOS_TEXT = "Sorry, no photos available for this place";
    private String[] mDataPhotosArray;

    private RecyclerView mPhotosGrid;

    public PhotosFragment() {}

    public static PhotosFragment getInstance() {
        return new PhotosFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataPhotosArray = AppController.getInstance().getPlacePhotos();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeView(view);
    }

    private void initializeView(View view) {
        mPhotosGrid = view.findViewById(R.id.photosGrid);
        if (mDataPhotosArray != null && mDataPhotosArray.length > 0) {
            GridLayoutManager layoutManager =
                    new GridLayoutManager(getActivity().getApplicationContext(),
                            2, LinearLayoutManager.VERTICAL, false);
            mPhotosGrid.setLayoutManager(layoutManager);
            mPhotosGrid.setAdapter(new PhotosOverviewGridAdapter(getActivity()
                    .getApplicationContext(), mDataPhotosArray));
        } else {
            TextView no_Photos= view.findViewById(R.id.no_photos);
            no_Photos.setText(NO_PHOTOS_TEXT);
        }
    }
}