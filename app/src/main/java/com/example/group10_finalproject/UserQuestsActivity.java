package com.example.group10_finalproject;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group10_finalproject.models.Quest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserQuestsActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private ArrayList<Quest> quests;
    private String userId;
    private EditQuestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_quests);

        quests = new ArrayList<>();
        userId = getIntent().getStringExtra("userId");
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("quests");

        RecyclerView recyclerView = findViewById(R.id.edit_quest_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EditQuestAdapter(quests, this);
        recyclerView.setAdapter(adapter);

        dbRef.orderByChild("creatorId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                quests.clear();

                for (DataSnapshot questSnapshot : dataSnapshot.getChildren()) {
                    Quest quest = questSnapshot.getValue(Quest.class);
                    quests.add(quest);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserQuestsActivity", "Database query failed", databaseError.toException());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}