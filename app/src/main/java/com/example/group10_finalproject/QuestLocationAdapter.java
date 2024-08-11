package com.example.group10_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.QuestLocation;

import java.util.ArrayList;
import java.util.List;

public class QuestLocationAdapter extends RecyclerView.Adapter<QuestLocationAdapter.LocationViewHolder>{

    private ArrayList<QuestLocation> locations;

    public QuestLocationAdapter(ArrayList<QuestLocation> locations) {
        this.locations = locations;
    }

    public int getItemCount() { return locations.size(); }

    public QuestLocationAdapter.LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.quest_location_item, viewGroup, false);

        return new QuestLocationAdapter.LocationViewHolder(v);
    }

    public void onBindViewHolder(QuestLocationAdapter.LocationViewHolder viewHolder, int i) {
        QuestLocation location = locations.get(i);

        String name = location.getName();

        viewHolder.name.setText(name);
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView name;

        LocationViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.quest_location_name);
        }
    }
}
