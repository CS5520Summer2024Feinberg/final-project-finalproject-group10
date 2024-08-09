package com.example.group10_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group10_finalproject.models.Quest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditQuestActivity extends AppCompatActivity {

    private String title;
    private String location;
    private String description;
    private String questId;
    private String userId;
    private Button saveButton;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_quest);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("quests");

        title = getIntent().getStringExtra("title");
        location = getIntent().getStringExtra("location");
        description = getIntent().getStringExtra("description");
        questId = getIntent().getStringExtra("questId");
        userId = getIntent().getStringExtra("userId");

        EditText titleInput = findViewById(R.id.edit_quest_name_input);
        EditText locationInput = findViewById(R.id.edit_quest_location_input);
        EditText descriptionInput = findViewById(R.id.edit_quest_description_input);
        saveButton = findViewById(R.id.edit_quest_save_button);

        saveButton.setOnClickListener(v -> {
            String updatedTitle = titleInput.getText().toString().trim();
            String updatedLocation = locationInput.getText().toString().trim();
            String updatedDescription = descriptionInput.getText().toString().trim();

            dbRef.child(questId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Quest quest = dataSnapshot.getValue(Quest.class);

                        if (quest != null) {
                            quest.setTitle(updatedTitle);
                            quest.setRoughLocation(updatedLocation);
                            quest.setDescription(updatedDescription);

                            dbRef.child(questId).setValue(quest).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditQuestActivity.this, "Quest updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(EditQuestActivity.this, UserHomeActivity.class);
                                    intent.putExtra("userId", userId);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(EditQuestActivity.this, "Failed to update quest.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(EditQuestActivity.this, "Quest not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("EditQuestActivity", "Database error", databaseError.toException());
                }
            });
        });

        titleInput.setText(title);
        locationInput.setText(location);
        descriptionInput.setText(description);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}