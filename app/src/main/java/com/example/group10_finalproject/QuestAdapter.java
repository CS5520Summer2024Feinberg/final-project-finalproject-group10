package com.example.group10_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Quest;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {
    private List<Quest> quests;
    private OnQuestClickListener listener;

    public interface OnQuestClickListener {
        void onQuestClick(Quest quest);
    }

    public QuestAdapter(List<Quest> quests, OnQuestClickListener listener) {
        this.quests = quests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = quests.get(position);
        holder.textView.setText(quest.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onQuestClick(quest));
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void updateQuests(List<Quest> newQuests) {
        quests = newQuests;
        notifyDataSetChanged();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        QuestViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}