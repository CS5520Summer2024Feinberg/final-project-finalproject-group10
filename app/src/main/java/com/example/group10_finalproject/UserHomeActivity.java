package com.example.group10_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserHomeActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");
        Log.d("DB", userId);

        Button exploreButton = findViewById(R.id.home_explore_button);
        Button questsButton = findViewById(R.id.home_quests_buton);
        Button createButton = findViewById(R.id.home_create_button);
        Button galleryButton = findViewById(R.id.home_gallery_button);
        Button accountButton = findViewById(R.id.home_account_button);

        exploreButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, MapsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, CreateQuestActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        questsButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, UserQuestsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, UserGalleryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        accountButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, UserAccountActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }
}