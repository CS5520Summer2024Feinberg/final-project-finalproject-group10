package com.example.group10_finalproject;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.QuestLocation;

import java.util.ArrayList;

public class EditLocationAdapter extends RecyclerView.Adapter<EditLocationAdapter.LocationViewHolder> {

    private ArrayList<QuestLocation> locations;
    private Activity activity;

    public EditLocationAdapter(ArrayList<QuestLocation> locations, Activity activity) {
        this.locations = locations;
        this.activity = activity;
    }

    public int getItemCount() { return locations.size(); }

    public EditLocationAdapter.LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_quest_card, viewGroup, false);

        return new EditLocationAdapter.LocationViewHolder(v);
    }

    public void onBindViewHolder(EditLocationAdapter.LocationViewHolder viewHolder, int i) {
        QuestLocation location = locations.get(i);

        String name = location.getName();
        String address = location.getAddress();

        viewHolder.name.setText(name);
        viewHolder.address.setText(address);
        viewHolder.editButton.setOnClickListener(v -> {
            Intent  intent = new Intent(activity, EditLocationFormActivity.class);
            intent.putExtra("location", location);
            activity.startActivityForResult(intent, 1001);
        });

    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        Button editButton;

        LocationViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.text_layout_1);
            this.address = itemView.findViewById(R.id.edit_quest_location);
            this.editButton = itemView.findViewById(R.id.edit_quest_edit_button);
        }
    }
}
