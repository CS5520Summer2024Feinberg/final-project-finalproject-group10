package com.example.group10_finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Quest;
import com.example.group10_finalproject.models.QuestLocation;
import com.example.group10_finalproject.models.Review;
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

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    protected static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float MAX_QUEST_DISTANCE_MILES = 20;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<Quest> allQuests;
    private FirebaseDatabase db;
    private DatabaseReference questsReference;
    private SearchView searchView;
    private RecyclerView questList;
    private QuestAdapter questAdapter;
    private Location lastKnownLocation;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        db = FirebaseDatabase.getInstance();
        questsReference = db.getReference("quests");
        allQuests = new ArrayList<>();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (mMap != null) {
                        updateMapWithNearbyQuests(location);
                    }
                }
            }
        };

        searchView = findViewById(R.id.searchView);
        questList = findViewById(R.id.questList);

        userId = getIntent().getStringExtra("userId");

        questList.setLayoutManager(new LinearLayoutManager(this));
        questAdapter = new QuestAdapter(new ArrayList<>(), this::showQuestDialog);
        questList.setAdapter(questAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    questList.setVisibility(View.GONE);
                }
                return false;
            }
        });

        fetchQuestsFromFirebase();
    }

    private void fetchQuestsFromFirebase() {
        questsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allQuests.clear();
                for (DataSnapshot questSnapshot : dataSnapshot.getChildren()) {
                    Quest quest = questSnapshot.getValue(Quest.class);
                    if (quest != null) {
                        allQuests.add(quest);
                    }
                }
                // If we already have a location, update the map
                if (fusedLocationClient != null) {
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location -> {
                            if (location != null) {
                                updateMapWithNearbyQuests(location);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("MapsActivity", "loadQuests:onCancelled", databaseError.toException());
                Toast.makeText(MapsActivity.this, "Failed to load quests.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        List<Quest> searchResults = new ArrayList<>();
        for (Quest quest : allQuests) {
            if (quest.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    quest.getRoughLocation().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(quest);
            }
        }

        if (lastKnownLocation != null) {
            Collections.sort(searchResults, (q1, q2) -> {
                float[] results1 = new float[1];
                float[] results2 = new float[1];
                Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                        q1.getLocations().get(0).getLatitude(), q1.getLocations().get(0).getLongitude(), results1);
                Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                        q2.getLocations().get(0).getLatitude(), q2.getLocations().get(0).getLongitude(), results2);
                return Float.compare(results1[0], results2[0]);
            });
        }

        questAdapter.updateQuests(searchResults);
        questList.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Enable the my-location layer
        mMap.setMyLocationEnabled(true);

        // Set marker click listener
        mMap.setOnMarkerClickListener(this);

        // Request location updates
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setIntervalMillis(10000) // 10 seconds
                .setMinUpdateIntervalMillis(5000) // 5 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateMapWithNearbyQuests(Location playerLocation) {
        mMap.clear();
        LatLng playerLatLng = new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLatLng, 12));

        for (Quest quest : allQuests) {
            if (quest.getLocations() != null && !quest.getLocations().isEmpty()) {
                QuestLocation firstLocation = quest.getLocations().get(0);
                LatLng questLatLng = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());

                float[] distance = new float[1];
                Location.distanceBetween(playerLocation.getLatitude(), playerLocation.getLongitude(),
                        firstLocation.getLatitude(), firstLocation.getLongitude(), distance);

                if (distance[0] <= MAX_QUEST_DISTANCE_MILES * 1609.34) { // Convert miles to meters
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(questLatLng)
                            .title(quest.getTitle());
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(quest);
                }
            }
        }
        lastKnownLocation = playerLocation;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Quest quest = (Quest) marker.getTag();
        if (quest != null) {
            showQuestDialog(quest);
            return true;
        }
        return false;
    }

    private void showQuestDialog(Quest quest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quest_details, null);

        ImageView questImageView = dialogView.findViewById(R.id.questImageView);
        TextView questTitleTextView = dialogView.findViewById(R.id.questTitleTextView);
        TextView questDescriptionTextView = dialogView.findViewById(R.id.questDescriptionTextView);
        TextView questRatingTextView = dialogView.findViewById(R.id.questRatingTextView);

        questTitleTextView.setText(quest.getTitle());
        questDescriptionTextView.setText(quest.getDescription());

        // Load image from Firebase Storage
        if (quest.getImageId() != null && !quest.getImageId().isEmpty()) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference().child("images/" + quest.getImageId() + ".jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(questImageView);
            }).addOnFailureListener(e -> {
                Log.e("MapsActivity", "Error loading quest image", e);
                questImageView.setImageResource(R.drawable.error_image);
            });
        } else {
            questImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Fetch and display average rating
        fetchAverageRating(quest.getQuestId(), (averageRating, hasRatings) -> {
            String ratingText = hasRatings
                    ? String.format("Average Rating: %.2f / 5", averageRating)
                    : "Average Rating: NA";
            questRatingTextView.setText(ratingText);
        });

        builder.setView(dialogView)
                .setPositiveButton("Start Quest", (dialog, which) -> {
                    Intent intent = new Intent(MapsActivity.this, QuestGameplayActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("QUEST_ID", quest.getQuestId());
                    startActivity(intent);
                })
                .setNegativeButton("Back", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Reviews", (dialog, which) -> {
                    Intent intent = new Intent(MapsActivity.this, ReviewsActivity.class);
                    intent.putExtra("QUEST_ID", quest.getQuestId());
                    intent.putExtra("QUEST_TITLE", quest.getTitle());
                    startActivity(intent);
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#008B8B"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#008B8B"));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#008B8B"));
    }

    public void fetchAverageRating(String questId, OnRatingFetchedListener listener) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        reviewsRef.orderByChild("questId").equalTo(questId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalRating = 0;
                int count = 0;
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                        count++;
                    }
                }
                if (count > 0) {
                    float averageRating = (float) totalRating / count;
                    listener.onRatingFetched(averageRating, true);
                } else {
                    listener.onRatingFetched(0, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MapsActivity", "Error fetching reviews", databaseError.toException());
                listener.onRatingFetched(0, false);
            }
        });
    }

    public interface OnRatingFetchedListener {
        void onRatingFetched(float averageRating, boolean hasRatings);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            startLocationUpdates();
        }
    }
}