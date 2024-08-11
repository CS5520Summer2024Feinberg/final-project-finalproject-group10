package com.example.group10_finalproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Image;
import com.example.group10_finalproject.models.Quest;
import com.example.group10_finalproject.models.QuestLocation;
import com.example.group10_finalproject.models.Status;
import com.example.group10_finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateQuestActivity extends AppCompatActivity {

    private String userId;
    private ArrayList<QuestLocation> locations;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseDatabase db;
    private DatabaseReference dbReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ImageView imageIcon;
    private String currentPhotoPath;
    private Image currentImage;
    private AlertDialog progressDialog;
    private QuestLocationAdapter adapter;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quest);

        locations = new ArrayList<>();

        imageIcon = findViewById(R.id.create_quest_picture_icon);

        if (savedInstanceState != null) {
            locations = savedInstanceState.getParcelableArrayList("locations");
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");
        db = FirebaseDatabase.getInstance();
        dbReference = db.getReference("quests");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");

        RecyclerView recyclerView = findViewById(R.id.locations_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestLocationAdapter(locations);
        recyclerView.setAdapter(adapter);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        locations.clear();
                        locations.addAll(result.getData().getParcelableArrayListExtra("locations"));
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        Button addLocation = findViewById(R.id.create_quest_add_location);
        addLocation.setOnClickListener(v -> {
            Intent intent = new Intent(CreateQuestActivity.this, AddLocationActivity.class);
            intent.putParcelableArrayListExtra("locations", locations);
            intent.putExtra("userId", userId);
            activityResultLauncher.launch(intent);

        });

        Button publishQuest = findViewById(R.id.create_quest_publish);
        publishQuest.setOnClickListener(v -> {
            EditText title = findViewById(R.id.create_quest_title_input);
            EditText location = findViewById(R.id.create_quest_location_input);
            EditText description = findViewById(R.id.create_quest_description_input);

            String titleText = String.valueOf(title.getText());
            String locationText = String.valueOf(location.getText());
            String descriptionText = String.valueOf(description.getText());

            if (titleText.trim().isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }

            if (locationText.trim().isEmpty()) {
                Toast.makeText(this, "Location cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }

            if (descriptionText.trim().isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }

            if (locations.size() == 0) {
                Toast.makeText(this, "Quests must have at least 1 location", Toast.LENGTH_LONG).show();
                return;
            }

            Quest newQuest = new Quest(descriptionText, locationText, titleText, userId, Status.PUBLISHED, locations);

            createQuest(newQuest);

        });

        Button imageButton = findViewById(R.id.create_quest_image);
        imageButton.setOnClickListener(v -> selectImage());

        checkPermissions();

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("locations", locations);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            locations = savedInstanceState.getParcelableArrayList("locations");
            adapter.notifyDataSetChanged();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateQuestActivity.this);
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
                imageIcon.setVisibility(ImageView.VISIBLE);
                Toast.makeText(CreateQuestActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
            } else if (requestCode == SELECT_FILE && data != null) {
                Uri selectedImageUri = data.getData();
                currentImage = new Image(userId, selectedImageUri.getPath(), selectedImageUri);
                imageIcon.setVisibility(ImageView.VISIBLE);
                Toast.makeText(CreateQuestActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
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

    private void createQuest(Quest quest) {

        showProgressDialog();

        if (currentImage != null) {
            String imageId = currentImage.getImageId();
            StorageReference storageReference = firebaseStorage.getReference().child("images/" + imageId + ".jpg");
            storageReference.putFile(currentImage.getFileUri())
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToDatabase(imageUrl);
                        saveQuestToDatabase(quest, currentImage.getImageId());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(CreateQuestActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            saveQuestToDatabase(quest, "");
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("images");
        String uploadId = databaseReference.push().getKey();
        if (uploadId != null) {
            databaseReference.child(uploadId).setValue(imageUrl);
        }
    }

    private void saveQuestToDatabase(Quest quest, String imageId) {
        quest.setImageId(imageId);
        Query query = dbReference.orderByChild("questId").equalTo(quest.getQuestId());
        FirebaseDatabase userDB = FirebaseDatabase.getInstance();
        DatabaseReference userDBReference = userDB.getReference("users");

        Query userQuery = userDBReference.orderByChild("userId").equalTo(userId);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.addCreatedQuest(quest.getQuestId());
                            user.addMedia(imageId);
                            userSnapshot.getRef().setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("DB", "Quest added to user.");
                                    }).addOnFailureListener(e -> {
                                        Log.d("DB", "Quest adding failed.");
                                    });
                        }
                    }
                } else {
                    Log.d("DB", "User not found...");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB", "Database error: " + error.getMessage());
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                dbReference.child(quest.getQuestId()).setValue(quest)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateQuestActivity.this,
                                    "Quest created successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(CreateQuestActivity.this,
                                    "Failed to create quest. Please try again.", Toast.LENGTH_LONG).show();
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB", "Database error: " + error.getMessage());
                Toast.makeText(CreateQuestActivity.this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
        hideProgressDialog();
        finish();
    }
}