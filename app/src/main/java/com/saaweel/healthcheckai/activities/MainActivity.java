package com.saaweel.healthcheckai.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.fragments.ChatFragment;
import com.saaweel.healthcheckai.fragments.MainFragment;
import com.saaweel.healthcheckai.fragments.PersonalDataFragment;
import com.saaweel.healthcheckai.fragments.SettingsFragment;
import com.saaweel.healthcheckai.fragments.ToolsFragment;

import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

/**
 *
 */
public class MainActivity extends AppCompatActivity {
    private TextView quickApp1, quickApp2, quickApp3, quickApp4;
    private TextView app_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen.installSplashScreen(this);

        setContentView(R.layout.activity_main);

        changeFragment(new MainFragment());

        quickApp1 = findViewById(R.id.quickApp1);
        quickApp2 = findViewById(R.id.quickApp2);
        quickApp3 = findViewById(R.id.quickApp3);
        quickApp4 = findViewById(R.id.quickApp4);
        app_title = findViewById(R.id.app_title);

        quickApp1.setOnClickListener(v -> {
            setTabs("app1");
            changeFragment(new ChatFragment());
        });

        quickApp2.setOnClickListener(v -> {
            setTabs("app2");
            changeFragment(new ToolsFragment());
        });

        quickApp3.setOnClickListener(v -> {
            setTabs("app3");
            changeFragment(new PersonalDataFragment());
        });

        quickApp4.setOnClickListener(v -> {
            setTabs("app4");
            changeFragment(new SettingsFragment());
        });
    }

    public void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_view, fragment).commit();
        findViewById(R.id.fragment_view).setVisibility(View.VISIBLE);
    }

    public void setTabs(String selected) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (selected.equals("home")) {
            app_title.setVisibility(View.VISIBLE);
            quickApp1.setVisibility(View.GONE);
            quickApp2.setVisibility(View.GONE);
            quickApp3.setVisibility(View.GONE);
            quickApp4.setVisibility(View.GONE);
        } else {
            quickApp1.setTextSize(30);
            quickApp2.setTextSize(30);
            quickApp3.setTextSize(30);
            quickApp4.setTextSize(30);

            switch (selected) {
                case "app1":
                    quickApp1.setTextSize(50);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    break;
                case "app2":
                    quickApp2.setTextSize(50);
                    break;
                case "app3":
                    quickApp3.setTextSize(50);
                    break;
                case "app4":
                    quickApp4.setTextSize(50);
                    break;
            }

            app_title.setVisibility(View.GONE);
            quickApp1.setVisibility(View.VISIBLE);
            quickApp2.setVisibility(View.VISIBLE);
            quickApp3.setVisibility(View.VISIBLE);
            quickApp4.setVisibility(View.VISIBLE);
        }
    }
}