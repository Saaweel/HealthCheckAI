package com.saaweel.healthcheckai.adarpters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.saaweel.healthcheckai.R;

public class Allergy extends RecyclerView.ViewHolder {
    TextView name;
    TextView remove;
    AllergyAdapter allergyAdapter;

    public Allergy(View view) {
        super(view);

        this.name = view.findViewById(R.id.name);

        this.remove = view.findViewById(R.id.remove);

        this.remove.setOnClickListener(v -> {
            allergyAdapter.removeData(name.getText().toString());
        });
    }

    public void setData(String name, AllergyAdapter allergyAdapter) {
        this.name.setText(name);
        this.allergyAdapter = allergyAdapter;
    }
}
