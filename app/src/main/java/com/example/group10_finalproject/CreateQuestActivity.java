package com.example.group10_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group10_finalproject.models.Quest;
import com.example.group10_finalproject.models.QuestLocation;
import com.example.group10_finalproject.models.Status;
import com.example.group10_finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreateQuestActivity extends AppCompatActivity {

    private String userId;
    private ArrayList<QuestLocation> locations;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseDatabase db;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quest);

        locations = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        dbReference = db.getReference("quests");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");
        Log.d("DB", userId);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        locations = result.getData().getParcelableArrayListExtra("locations");
                    }
                }
        );

        Button addLocation = findViewById(R.id.create_quest_add_location);
        addLocation.setOnClickListener(v -> {
            Intent intent = new Intent(CreateQuestActivity.this, AddLocationActivity.class);
            intent.putParcelableArrayListExtra("locations", locations);
            activityResultLauncher.launch(intent);

        });

        Button publishQuest = findViewById(R.id.create_quest_publish);
        publishQuest.setOnClickListener(v -> {
            EditText title = findViewById(R.id.create_quest_title_input);
            EditText location = findViewById(R.id.create_quest_location_input);
            EditText description = findViewById(R.id.create_quest_description_input);

            String titleText = String.valueOf(title.getText());
            String locationText = String.valueOf(location.getText());
            String descriptionText = String.valueOf(description.getText());

            Quest newQuest = new Quest(descriptionText, locationText, titleText, userId, Status.PUBLISHED, locations);

            createQuest(newQuest);

        });

    }

    private void createQuest(Quest quest) {
        Query query = dbReference.orderByChild("questId").equalTo(quest.getQuestId());
        FirebaseDatabase userDB = FirebaseDatabase.getInstance();
        DatabaseReference userDBReference = userDB.getReference("users");

        Query userQuery = userDBReference.orderByChild("userId").equalTo(userId);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.addCreatedQuest(quest.getQuestId());
                            userSnapshot.getRef().setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("DB", "Quest added to user.");
                                    }).addOnFailureListener(e -> {
                                        Log.d("DB", "Quest adding failed.");
                                    });
                        }
                    }
                } else {
                    Log.d("DB", "User not found...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB", "Database error: " + error.getMessage());
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                dbReference.child(quest.getQuestId()).setValue(quest)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateQuestActivity.this,
                                    "Quest created successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(CreateQuestActivity.this,
                                    "Failed to create quest. Please try again.", Toast.LENGTH_LONG).show();
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB", "Database error: " + error.getMessage());
                Toast.makeText(CreateQuestActivity.this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}