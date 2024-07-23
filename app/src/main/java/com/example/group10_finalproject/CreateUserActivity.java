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

import com.example.group10_finalproject.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

            Toast.makeText(CreateUserActivity.this, "Account created!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }

    private void createUser(User user) {
        dbReference.child(user.getUserId()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DB", "User created!");
                }).addOnFailureListener(e -> {
                    Log.d("DB", "User creation failed!");
                });
    }
}