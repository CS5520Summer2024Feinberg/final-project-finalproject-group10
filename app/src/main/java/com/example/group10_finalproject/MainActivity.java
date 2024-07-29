package com.example.group10_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseDatabase.getInstance();
        dbReference = db.getReference("users");

        Button btnOpenMap = findViewById(R.id.map);
        Button createButton = findViewById(R.id.create_button);
        Button loginButton = findViewById(R.id.login_button);

        // TODO: Delete this once done testing
        btnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateUserActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            EditText username = findViewById(R.id.username_input_text);
            EditText password = findViewById(R.id.password_input_text);

            String usernameText = String.valueOf(username.getText());
            String passwordText = String.valueOf(password.getText());

            loginUser(usernameText, passwordText);
        });

    }

    public void loginUser(String username, String password) {
        Query query = dbReference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            if (Objects.equals(username, user.getUsername())
                                    && Objects.equals(password, user.getPassword())) {

                                Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
                                intent.putExtra("userId", user.getUserId());
                                startActivity(intent);

                            } else {
                                Toast.makeText(MainActivity.this, "Incorrect password..", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User not found..", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DB", "Error: " + databaseError.getMessage());
            }
        });
    }
}