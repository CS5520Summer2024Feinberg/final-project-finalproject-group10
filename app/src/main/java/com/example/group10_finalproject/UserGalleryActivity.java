package com.example.group10_finalproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserGalleryActivity extends AppCompatActivity {


    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private List<String> userImageIds;
    private String userId;
    private View fullScreenContainer;
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Log.e("UserGalleryActivity", "No user ID provided");
            Toast.makeText(this, "Error: No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userImageIds = new ArrayList<>();

        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        galleryAdapter = new GalleryAdapter(this, userImageIds, this::showFullScreenImage);
        galleryRecyclerView.setAdapter(galleryAdapter);

        fullScreenContainer = findViewById(R.id.fullScreenContainer);
        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        fullScreenContainer.setOnClickListener(v -> hideFullScreenImage());

        fetchUserMedia();
    }

    private void fetchUserMedia() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("media").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userImageIds.clear();
                for (DataSnapshot imageIdSnapshot : dataSnapshot.getChildren()) {
                    String imageId = imageIdSnapshot.getValue(String.class);
                    if (imageId != null && !imageId.isEmpty()) {
                        userImageIds.add(imageId);
                    }
                }
                if (userImageIds.isEmpty()) {
                    Log.i("UserGalleryActivity", "No images found for user: " + userId);
                    Toast.makeText(UserGalleryActivity.this, "No images found", Toast.LENGTH_SHORT).show();
                }
                galleryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserGalleryActivity", "Error fetching user media", databaseError.toException());
                Toast.makeText(UserGalleryActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFullScreenImage(String imageId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child("images/" + imageId + ".jpg");

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.error_image)
                    .into(fullScreenImageView);

            fullScreenContainer.setVisibility(View.VISIBLE);
            hideSystemUI();
        }).addOnFailureListener(e -> {
            Log.e("UserGalleryActivity", "Error loading full screen image: " + imageId, e);
            Toast.makeText(this, "Failed to load full screen image", Toast.LENGTH_SHORT).show();
        });
    }

    private void hideFullScreenImage() {
        fullScreenContainer.setVisibility(View.GONE);
        showSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        if (fullScreenContainer.getVisibility() == View.VISIBLE) {
            hideFullScreenImage();
        } else {
            super.onBackPressed();
        }
    }

}