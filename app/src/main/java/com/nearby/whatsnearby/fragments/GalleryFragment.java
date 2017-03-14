package com.nearby.whatsnearby.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.adapters.PlaceImages;


public class GalleryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_fragment, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.placesImage);
        String[] photosArray = getArguments().getStringArray("photos");
        if (photosArray != null && photosArray.length > 0) {
            gridView.setAdapter(new PlaceImages(getActivity(), photosArray));
        } else {
            TextView no_Image = (TextView) view.findViewById(R.id.no_images);
            no_Image.setText("Sorry, no images available for this place");
        }
        return view;
    }
}
