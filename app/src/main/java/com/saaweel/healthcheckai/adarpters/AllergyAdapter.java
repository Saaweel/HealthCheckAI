package com.saaweel.healthcheckai.adarpters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.saaweel.healthcheckai.R;

import java.util.List;

public class AllergyAdapter extends RecyclerView.Adapter<Allergy> {
    private List<String> localDataSet;

    public AllergyAdapter(List<String> dataSet) {
        localDataSet = dataSet;
    }

    @Override
    public Allergy onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pd_allergy, viewGroup, false);
        return new Allergy(view);
    }
    @Override
    public void onBindViewHolder(Allergy allergy, final int position) {
        allergy.setData(localDataSet.get(position), this);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void removeData(String data) {
        localDataSet.remove(data);
        notifyDataSetChanged();
    }
}