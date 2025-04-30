package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class reportFragment extends Fragment {

    // Fragment initialization parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public reportFragment() {
        // Required empty public constructor
    }

    public static reportFragment newInstance(String param1, String param2) {
        reportFragment fragment = new reportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Set up the back button navigation (left3 in your XML)
        setNavigationClick(view, R.id.left3, new homeFragment());

        return view;
    }

    private void setNavigationClick(View parentView, int buttonId, Fragment destination) {
        parentView.findViewById(buttonId).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, destination)
                        .addToBackStack(null)
                        .commit()
        );
    }
}