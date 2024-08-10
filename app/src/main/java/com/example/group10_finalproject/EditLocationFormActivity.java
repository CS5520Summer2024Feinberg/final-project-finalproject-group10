package com.example.group10_finalproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.group10_finalproject.models.QuestLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditLocationFormActivity extends AppCompatActivity {

    private QuestLocation location;
    private EditText name;
    private EditText address;
    private EditText description;
    private Button saveButton;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_location_form);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("quests");

        location = getIntent().getParcelableExtra("location");
        name = findViewById(R.id.edit_location_name_input);
        address = findViewById(R.id.edit_location_address);
        description = findViewById(R.id.edit_location_description);
        saveButton = findViewById(R.id.edit_location_save_button);

        name.setText(location.getName());
        address.setText(location.getAddress());
        description.setText(location.getDescription());

        saveButton.setOnClickListener(v -> {
            if (name.getText().toString().trim().isEmpty()
                    || address.getText().toString().trim().isEmpty()
                    || description.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Form fields cannot be empty.", Toast.LENGTH_LONG).show();
                return;
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            double latitude = 0;
            double longitude = 0;

            try {
                addresses = geocoder.getFromLocationName(address.getText().toString(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    Toast.makeText(this, "Unable to find location...", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            location.setName(name.getText().toString());
            location.setAddress(address.getText().toString());
            location.setDescription(description.getText().toString());
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean locationFound = false;

                    for (DataSnapshot questSnapshot : dataSnapshot.getChildren()) {
                        Quest quest = questSnapshot.getValue(Quest.class);

                        if (quest != null) {
                            List<QuestLocation> locations = quest.getLocations();

                            for (int i = 0; i < locations.size(); i++) {
                                QuestLocation questLocation = locations.get(i);

                                if (questLocation.getLocationId().equals(location.getLocationId())) {
                                    locations.set(i, location);
                                    locationFound = true;

                                    dbRef.child(quest.getQuestId()).setValue(quest)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Intent resultIntent = new Intent();
                                                    resultIntent.putExtra("updatedLocation", location);
                                                    setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                    Toast.makeText(EditLocationFormActivity.this,
                                                            "Location updated successfully!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(EditLocationFormActivity.this,
                                                            "Failed to update location.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    break;
                                }
                            }

                            if (locationFound) {
                                break;
                            }
                        }
                    }

                    if (!locationFound) {
                        Toast.makeText(EditLocationFormActivity.this, "Location not found in any quest.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error querying database", databaseError.toException());
                }
            });
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}