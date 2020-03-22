package com.nearby.whatsnearby.fragments.error;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nearby.whatsnearby.R;


public class ErrorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String msg = getArguments().getString("msg");
        View view = inflater.inflate(R.layout.error_fragment, container, false);
        TextView errorMsg = view.findViewById(R.id.error);
        errorMsg.setText(msg);
        return view;
    }
}