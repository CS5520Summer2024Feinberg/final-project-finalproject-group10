package com.example.group10_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Review;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    private List<Review> reviews;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.contentTextView.setText(review.getContent());
        holder.ratingTextView.setText("Rating: " + review.getRating() + "/5");

        try {
            Date date = inputFormat.parse(review.getDateCreated());
            holder.dateTextView.setText(outputFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.dateTextView.setText(review.getDateCreated());  // Fallback to displaying the raw string
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;
        TextView ratingTextView;
        TextView dateTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.reviewContentTextView);
            ratingTextView = itemView.findViewById(R.id.reviewRatingTextView);
            dateTextView = itemView.findViewById(R.id.reviewDateTextView);
        }
    }

}
