package com.example.group10_finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group10_finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserAccountActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseDatabase db;
    private DatabaseReference dbReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.edit_user_username_input);
        emailInput = findViewById(R.id.edit_user_email_input);
        passwordInput = findViewById(R.id.edit_user_password_input);
        Button saveChangesButton = findViewById(R.id.save_changes_button);

        db = FirebaseDatabase.getInstance();
        dbReference = db.getReference("users");

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadUserData();

        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        dbReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    usernameInput.setText(user.getUsername());
                    emailInput.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserAccountActivity", "Error loading user data", databaseError.toException());
                Toast.makeText(UserAccountActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        String newUsername = usernameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();
        String newPassword = passwordInput.getText().toString().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Username and email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        dbReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    user.setUsername(newUsername);
                    user.setEmail(newEmail);
                    if (!newPassword.isEmpty()) {
                        user.setPassword(newPassword);
                    }

                    dbReference.child(userId).setValue(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UserAccountActivity.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UserAccountActivity.this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                                Log.e("UserAccountActivity", "Error saving user data", e);
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserAccountActivity", "Error updating user data", databaseError.toException());
                Toast.makeText(UserAccountActivity.this, "Failed to update user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}