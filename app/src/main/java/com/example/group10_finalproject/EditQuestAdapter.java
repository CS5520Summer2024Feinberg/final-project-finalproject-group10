package com.example.group10_finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Quest;

import java.util.ArrayList;

public class EditQuestAdapter extends RecyclerView.Adapter<EditQuestAdapter.QuestViewHolder> {
    private ArrayList<Quest> quests;
    private Context context;

    public EditQuestAdapter(ArrayList<Quest> quests, Context context) {
        this.quests = quests;
        this.context = context;
    }

    public int getItemCount() { return quests.size(); }

    public EditQuestAdapter.QuestViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_quest_card, viewGroup, false);

        return new EditQuestAdapter.QuestViewHolder(v);
    }


    public void onBindViewHolder(EditQuestAdapter.QuestViewHolder viewHolder, int i) {
        Quest quest = quests.get(i);

        String name = quest.getTitle();
        String location = quest.getRoughLocation();

        viewHolder.name.setText(name);
        viewHolder.location.setText(location);
        viewHolder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditQuestActivity.class);
            intent.putExtra("title", quest.getTitle());
            intent.putExtra("location", quest.getRoughLocation());
            intent.putExtra("description", quest.getDescription());
            intent.putExtra("questId", quest.getQuestId());
            intent.putExtra("userId", quest.getCreatorId());
            context.startActivity(intent);
        });
    }

    class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView location;
        Button editButton;

        QuestViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.text_layout_1);
            this.location = itemView.findViewById(R.id.edit_quest_location);
            this.editButton = itemView.findViewById(R.id.edit_quest_edit_button);
        }
    }
}
