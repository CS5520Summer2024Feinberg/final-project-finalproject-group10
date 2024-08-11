package com.example.group10_finalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group10_finalproject.models.Image;
import com.example.group10_finalproject.models.QuestLocation;
import com.example.group10_finalproject.models.User;
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
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity {

    private String userId;
    private ArrayList<QuestLocation> locations;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String currentPhotoPath;
    private Image currentImage;
    private AlertDialog progressDialog;
    private ImageView iconImage;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);

        locations = getIntent().getParcelableArrayListExtra("locations");
        userId = getIntent().getStringExtra("userId");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        iconImage = findViewById(R.id.create_quest_picture_icon);

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

            onSubmit(nameText, addressText, descriptionText);
        });

        Button imageButton = findViewById(R.id.add_location_image);
        imageButton.setOnClickListener(view -> selectImage());

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddLocationActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File file = new File(currentPhotoPath);
                Uri uri = Uri.fromFile(file);
                currentImage = new Image(userId, currentPhotoPath, uri);
                iconImage.setVisibility(ImageView.VISIBLE);
                Toast.makeText(AddLocationActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
            } else if (requestCode == SELECT_FILE && data != null) {
                Uri selectedImageUri = data.getData();
                currentImage = new Image(userId, selectedImageUri.getPath(), selectedImageUri);
                iconImage.setVisibility(ImageView.VISIBLE);
                Toast.makeText(AddLocationActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = userId + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(R.layout.progress_dialog);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("images");
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        List<String> media = user.getMedia();
                        media.add(currentImage.getImageId());

                        userReference.child("media").setValue(media)
                                .addOnSuccessListener(aVoid -> Log.d("DB", "media updated"))
                                .addOnFailureListener(e -> Log.d("DB", "Image adding failure"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB", "Database error: " + error.getMessage());
            }
        });

        String uploadId = databaseReference.push().getKey();
        if (uploadId != null) {
            databaseReference.child(uploadId).setValue(imageUrl);
        }
    }

    private void onSubmit(String name, String address, String description) {

        if (name.trim().isEmpty()) {
            Toast.makeText(this, "Location name cannot be empty..", Toast.LENGTH_LONG).show();
            return;
        }

        if (address.trim().isEmpty()) {
            Toast.makeText(this, "Address cannot be empty..", Toast.LENGTH_LONG).show();
            return;
        }

        if (description.trim().isEmpty()) {
            Toast.makeText(this, "Description cannot be empty..", Toast.LENGTH_LONG).show();
            return;

        }

        showProgressDialog();

        if (currentImage != null) {
            Log.d("DB", "We get here");
            String imageId = currentImage.getImageId();
            StorageReference storageReference = firebaseStorage.getReference().child("images/" + imageId + ".jpg");
            storageReference.putFile(currentImage.getFileUri())
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToDatabase(imageUrl);
                        addLocation(name, address, description, imageId);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(AddLocationActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            addLocation(name, address, description, "");
        }
    }

    public void addLocation(String name, String address, String description, String imageId) {
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
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoder failure...", Toast.LENGTH_LONG).show();
        }

        QuestLocation newLocation = new QuestLocation(address, longitude, latitude, name, description, "", imageId);
        locations.add(newLocation);

        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("locations", locations);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Location added!", Toast.LENGTH_LONG).show();
        hideProgressDialog();
        finish();
    }
}