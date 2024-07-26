package com.example.group10_finalproject;

import android.content.Intent;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CreateUserActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseDatabase.getInstance();
        dbReference = db.getReference("users");

        Button createUserButton = findViewById(R.id.create_user_button);

        createUserButton.setOnClickListener(v -> {
            EditText username = findViewById(R.id.create_user_username_input);
            EditText email = findViewById(R.id.create_user_email_input);
            EditText password = findViewById(R.id.create_user_password_input);

            String usernameText = String.valueOf(username.getText());
            String emailText = String.valueOf(email.getText());
            String passwordText = String.valueOf(password.getText());

            User newUser = new User(emailText, usernameText, passwordText);
            createUser(newUser);
        });

    }

    private void createUser(User user) {
        Query query = dbReference.orderByChild("username").equalTo(user.getUsername());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(CreateUserActivity.this,
                            "Username already exists! Please chose another.", Toast.LENGTH_LONG).show();
                } else {
                    dbReference.child(user.getUserId()).setValue(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(CreateUserActivity.this,
                                        "Account created successfully!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
                                startActivity(intent);
                            }).addOnFailureListener(e -> {
                                Toast.makeText(CreateUserActivity.this,
                                        "Failed to create account. Please try again.", Toast.LENGTH_LONG).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB", "Database error: " + error.getMessage());
                Toast.makeText(CreateUserActivity.this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}