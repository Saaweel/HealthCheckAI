package com.saaweel.healthcheckai.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.activities.MainActivity;

public class MainFragment extends Fragment {
    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        MainActivity mainActivity = (MainActivity) requireActivity();

        view.findViewById(R.id.app1).setOnClickListener(v -> mainActivity.changeFragment(new ChatFragment()));
        view.findViewById(R.id.app2).setOnClickListener(v -> mainActivity.changeFragment(new ToolsFragment()));
        view.findViewById(R.id.app3).setOnClickListener(v -> mainActivity.changeFragment(new PersonalDataFragment()));
        view.findViewById(R.id.app4).setOnClickListener(v -> mainActivity.changeFragment(new SettingsFragment()));
        view.findViewById(R.id.app5).setOnClickListener(v -> mainActivity.changeFragment(new AboutFragment()));

        return view;
    }
}