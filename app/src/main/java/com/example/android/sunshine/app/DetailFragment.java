package com.example.android.sunshine.app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Get text view
        TextView detailTextView = (TextView) rootView.findViewById(R.id.detail_forecast_text);

        // Get intent and forecast data string
        Intent detailIntent = getActivity().getIntent();
        String detail = detailIntent.getStringExtra(Intent.EXTRA_TEXT);

        // Set text view to detail forecast string
        detailTextView.setText(detail);

        return rootView;
    }

}
