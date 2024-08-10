package com.example.group10_finalproject;

import static com.example.group10_finalproject.MapsActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.group10_finalproject.models.Image;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_PERMISSION = 100;
    private String currentPhotoPath;
    private Image currentImage;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String userId;

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
        userId = getCurrentUserId();

        db = FirebaseDatabase.getInstance();
        questsReference = db.getReference("quests");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadQuestData();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        checkPermissions();
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_details, null);

        ImageView locationImageView = dialogView.findViewById(R.id.locationImageView);
        TextView locationTitleTextView = dialogView.findViewById(R.id.locationTitleTextView);
        TextView locationDescriptionTextView = dialogView.findViewById(R.id.locationDescriptionTextView);
        Button takePictureButton = dialogView.findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(v -> selectImage());

        locationTitleTextView.setText(location.getName());
        locationDescriptionTextView.setText(location.getDescription());

        // Load image from Firebase Storage
        if (location.getImageId() != null && !location.getImageId().isEmpty()) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference().child("images/" + location.getImageId() + ".jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(locationImageView);
            }).addOnFailureListener(e -> {
                Log.e("QuestGameplayActivity", "Error loading location image", e);
                locationImageView.setImageResource(R.drawable.error_image);
            });
        } else {
            locationImageView.setImageResource(R.drawable.placeholder_image);
        }

        builder.setView(dialogView);

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

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(QuestGameplayActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                dispatchTakePictureIntent();
            } else if (options[item].equals("Choose from Gallery")) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.group10_finalproject.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = userId + "_" + questId + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File file = new File(currentPhotoPath);
                Uri uri = Uri.fromFile(file);
                currentImage = new Image(userId, currentPhotoPath, uri);
                uploadImageToFirebase(currentImage);
            } else if (requestCode == SELECT_FILE && data != null) {
                Uri selectedImageUri = data.getData();
                currentImage = new Image(userId, selectedImageUri.getPath(), selectedImageUri);
                uploadImageToFirebase(currentImage);
            }
        }
    }

    private void uploadImageToFirebase(Image image) {
        String imageId = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(imageId + ".jpg");

        imageRef.putFile(image.getFileUri())
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveImageUrlToDatabase(imageUrl, imageId);
                    updateUserMedia(imageId);
                    Toast.makeText(QuestGameplayActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(QuestGameplayActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveImageUrlToDatabase(String imageUrl, String imageId) {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("images");
        imagesRef.child(imageId).setValue(imageUrl);
    }

    private void updateUserMedia(String imageId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("media").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> mediaList = new ArrayList<>();
                for (DataSnapshot mediaSnapshot : dataSnapshot.getChildren()) {
                    String mediaItem = mediaSnapshot.getValue(String.class);
                    if (mediaItem != null) {
                        mediaList.add(mediaItem);
                    }
                }
                mediaList.add(imageId);
                userRef.child("media").setValue(mediaList)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("QuestGameplayActivity", "Media list updated successfully");
                            Toast.makeText(QuestGameplayActivity.this, "Image saved to your gallery", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("QuestGameplayActivity", "Failed to update media list", e);
                            Toast.makeText(QuestGameplayActivity.this, "Failed to save image to your gallery", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QuestGameplayActivity", "Error fetching media list", databaseError.toException());
                Toast.makeText(QuestGameplayActivity.this, "Failed to update gallery: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        final EditText commentInput = view.findViewById(R.id.reviewComment);
        final RatingBar ratingBar = view.findViewById(R.id.reviewRating);

        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);

        builder.setView(view)
                .setTitle("Rate and Review Quest")
                .setPositiveButton("Submit", (dialog, which) -> {
                    String comment = commentInput.getText().toString();
                    int rating = Math.round(ratingBar.getRating());
                    submitReview(comment, rating);
                })
                .setNegativeButton("Cancel", (dialog, which) -> returnToMapsActivity())
                .show();
    }

    private void submitReview(String content, int rating) {
        String userId = getCurrentUserId();
        Review newReview = new Review(content, rating, currentQuest.getQuestId(), userId);

        DatabaseReference reviewsReference = FirebaseDatabase.getInstance().getReference("reviews");
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        reviewsReference.child(newReview.getReviewId()).setValue(newReview)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DB", "Review added successfully.");

                    userReference.child("reviews").child(newReview.getReviewId()).setValue(true)
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("DB", "Review added to user's list.");
                                Toast.makeText(QuestGameplayActivity.this,
                                        "Review submitted successfully!", Toast.LENGTH_LONG).show();
                                returnToMapsActivity();
                            })
                            .addOnFailureListener(e -> {
                                Log.d("DB", "Failed to add review to user's list: " + e.getMessage());
                                Toast.makeText(QuestGameplayActivity.this,
                                        "Failed to update user data. Please try again.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d("DB", "Failed to add review: " + e.getMessage());
                    Toast.makeText(QuestGameplayActivity.this,
                            "Failed to submit review. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private String getCurrentUserId() {
       return getIntent().getStringExtra("userId");
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