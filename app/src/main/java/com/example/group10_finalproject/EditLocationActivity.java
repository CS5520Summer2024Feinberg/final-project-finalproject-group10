package com.example.group10_finalproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.QuestLocation;

import java.util.ArrayList;

public class EditLocationActivity extends AppCompatActivity {

    ArrayList<QuestLocation> locations;
    EditLocationAdapter adapter;
    public static final int REQUEST_CODE_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_location);

        locations = getIntent().getParcelableArrayListExtra("locations");

        RecyclerView recyclerView = findViewById(R.id.quest_location_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EditLocationAdapter(locations, this);
        recyclerView.setAdapter(adapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            QuestLocation updatedLocation = data.getParcelableExtra("updatedLocation");
            if (updatedLocation != null) {
                for (int i = 0; i < locations.size(); i++) {
                    if (locations.get(i).getLocationId().equals(updatedLocation.getLocationId())) {
                        locations.set(i, updatedLocation);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        }
    }

}