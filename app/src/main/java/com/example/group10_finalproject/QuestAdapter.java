package com.example.group10_finalproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Quest;
import com.example.group10_finalproject.models.Review;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {
    private List<Quest> quests;
    private final OnQuestClickListener listener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = quests.get(position);
        holder.questTitleTextView.setText(quest.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onQuestClick(quest));

        // Fetch and display average rating
        ((MapsActivity) holder.itemView.getContext()).fetchAverageRating(quest.getQuestId(), (averageRating, hasRatings) -> {
            String ratingText = hasRatings ? String.format("%.2f", averageRating) : "NA";
            holder.questRatingTextView.setText(ratingText);
        });
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
        TextView questTitleTextView;
        TextView questRatingTextView;

        QuestViewHolder(View itemView) {
            super(itemView);
            questTitleTextView = itemView.findViewById(R.id.questTitleTextView);
            questRatingTextView = itemView.findViewById(R.id.questRatingTextView);
        }
    }

    private void fetchAverageRating(String questId, OnRatingFetchedListener listener) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        reviewsRef.orderByChild("questId").equalTo(questId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalRating = 0;
                int count = 0;
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                        count++;
                    }
                }
                float averageRating = count > 0 ? (float) totalRating / count : 0;
                listener.onRatingFetched(averageRating);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QuestAdapter", "Error fetching reviews", databaseError.toException());
                listener.onRatingFetched(0);
            }
        });
    }

    interface OnRatingFetchedListener {
        void onRatingFetched(float averageRating);
    }
}