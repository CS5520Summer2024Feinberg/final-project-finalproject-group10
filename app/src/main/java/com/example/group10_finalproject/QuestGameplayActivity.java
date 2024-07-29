package com.example.group10_finalproject;

import static com.example.group10_finalproject.MapsActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group10_finalproject.models.Quest;
import com.example.group10_finalproject.models.QuestLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuestGameplayActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private String questId;
    private Quest currentQuest;
    private List<QuestLocation> questLocations;
    private int currentLocationIndex = 0;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private TextView questTitleTextView;
    private TextView currentLocationTextView;

    private static final float LOCATION_PROXIMITY_THRESHOLD = 50; // meters

    private FirebaseDatabase db;
    private DatabaseReference questsReference;
    private boolean dialogShownForCurrentLocation = false;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quest_gameplay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        questTitleTextView = findViewById(R.id.questTitleTextView);
        currentLocationTextView = findViewById(R.id.currentLocationTextView);

        questId = getIntent().getStringExtra("QUEST_ID");

        db = FirebaseDatabase.getInstance();
        questsReference = db.getReference("quests");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadQuestData();
    }

    private void loadQuestData() {
        questsReference.child(questId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentQuest = dataSnapshot.getValue(Quest.class);
                if (currentQuest != null) {
                    questLocations = currentQuest.getLocations();
                    updateQuestInfo();
                    if (mMap != null) {
                        updateMapWithQuestLocation();
                    }
                } else {
                    Toast.makeText(QuestGameplayActivity.this, "Failed to load quest data", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestGameplayActivity.this, "Failed to load quest data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateQuestInfo() {
        questTitleTextView.setText(currentQuest.getTitle());
        if (currentLocationIndex < questLocations.size()) {
            currentLocationTextView.setText(questLocations.get(currentLocationIndex).getName());
        } else {
            currentLocationTextView.setText("Quest Completed!");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);

        startLocationUpdates();
        if (currentQuest != null) {
            updateMapWithQuestLocation();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setIntervalMillis(10000) // 10 seconds
                .setMinUpdateIntervalMillis(5000) // 5 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    lastKnownLocation = location;
                    updatePlayerLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updatePlayerLocation(Location location) {
        if (mMap == null) return;

        LatLng playerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLatLng, 15));
        updateMapWithQuestLocation();
        checkProximityToLocation(location);
    }

    private void updateMapWithQuestLocation() {
        mMap.clear();
        if (currentLocationIndex < questLocations.size()) {
            QuestLocation location = questLocations.get(currentLocationIndex);
            LatLng questLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(questLatLng).title(location.getName()));
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (currentLocationIndex < questLocations.size()) {
            QuestLocation location = questLocations.get(currentLocationIndex);
            if (marker.getPosition().latitude == location.getLatitude() &&
                    marker.getPosition().longitude == location.getLongitude()) {

                // Check if the player is within 50 meters
                float[] results = new float[1];
                Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                        location.getLatitude(), location.getLongitude(), results);

                boolean isWithinRange = results[0] <= LOCATION_PROXIMITY_THRESHOLD;

                showLocationDialog(location, isWithinRange);
            }
        }
        return true;
    }

    private void checkProximityToLocation(Location playerLocation) {
        if (currentLocationIndex < questLocations.size()) {
            QuestLocation questLocation = questLocations.get(currentLocationIndex);
            Location locationTarget = new Location("");
            locationTarget.setLatitude(questLocation.getLatitude());
            locationTarget.setLongitude(questLocation.getLongitude());

            float distance = playerLocation.distanceTo(locationTarget);
            if (distance <= LOCATION_PROXIMITY_THRESHOLD && !dialogShownForCurrentLocation) {
                showLocationDialog(questLocation, true);
                dialogShownForCurrentLocation = true;
            }
        }
    }

    private void showLocationDialog(QuestLocation location, boolean allowProgress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(location.getName())
                .setMessage(location.getDescription());

        if (allowProgress) {
            String buttonText = (currentLocationIndex == questLocations.size() - 1) ? "Finish" : "Next Location";
            builder.setPositiveButton(buttonText, (dialog, which) -> {
                if (currentLocationIndex == questLocations.size() - 1) {
                    showReviewDialog();
                } else {
                    moveToNextLocation();
                }
            });
            builder.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());
        }

        builder.create().show();
    }

    private void moveToNextLocation() {
        currentLocationIndex++;
        dialogShownForCurrentLocation = false;
        updateQuestInfo();
        updateMapWithQuestLocation();
    }

    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quest Completed!")
                .setMessage("Would you like to leave a review?")
                .setPositiveButton("Leave Review", (dialog, which) -> showReviewForm())
                .setNegativeButton("No, thank you", (dialog, which) -> returnToMapsActivity())
                .setCancelable(false)
                .show();
    }

    private void showReviewForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Enter your review here");
        builder.setView(input)
                .setTitle("Write Your Review")
                .setPositiveButton("Submit", (dialog, which) -> {
                    String review = input.getText().toString();
                    submitReview(review);
                })
                .setNegativeButton("Cancel", (dialog, which) -> returnToMapsActivity())
                .show();
    }

    private void submitReview(String review) {
        // TODO: Implement the logic to submit the review in backend
        Toast.makeText(this, "Review submitted: " + review, Toast.LENGTH_SHORT).show();
        returnToMapsActivity();
    }

    private void returnToMapsActivity() {
        Intent intent = new Intent(QuestGameplayActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    onMapReady(mMap);
                }
            } else {
                Toast.makeText(this, "Location permission is required to play quests", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            startLocationUpdates();
        }
    }
}