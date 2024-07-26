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

import com.example.group10_finalproject.models.QuestLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity {

    private ArrayList<QuestLocation> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);

        locations = getIntent().getParcelableArrayListExtra("locations");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button addLocationButton = findViewById(R.id.add_location_submit);
        addLocationButton.setOnClickListener(v -> {
            EditText name = findViewById(R.id.add_location_name_input);
            EditText address = findViewById(R.id.add_location_address_input);
            EditText description = findViewById(R.id.add_location_description_input);

            String nameText = String.valueOf(name.getText());
            String addressText = String.valueOf(address.getText());
            String descriptionText = String.valueOf(description.getText());

            addLocation(nameText, addressText, descriptionText);
        });
    }

    public void addLocation(String name, String address, String description) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        double latitude = 0;
        double longitude = 0;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("DB", "Latitude: " + latitude);
                Log.d("DB", "Longitude: " + longitude);
            } else {
                Toast.makeText(this, "Unable to find location...", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoder failure...", Toast.LENGTH_LONG).show();
        }

        QuestLocation newLocation = new QuestLocation(address, longitude, latitude, name, description, "");
        locations.add(newLocation);

        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("locations", locations);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Location added!", Toast.LENGTH_LONG).show();
        finish();
    }
}