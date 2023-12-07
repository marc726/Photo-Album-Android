package com.example.android38;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AlbumActivity extends AppCompatActivity {

    private Album selectedAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Retrieve the selected album from the intent
        selectedAlbum = (Album) getIntent().getSerializableExtra("selectedAlbum");

        // Display the photos in the selected album
        displayPhotos();

        // Set up the back button
        Button backButton = findViewById(R.id.button2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the back button click (e.g., navigate back)
                onBackPressed();
            }
        });
    }

    private void displayPhotos() {
        ListView listView = findViewById(R.id.photoListView);

        // Create an ArrayAdapter for the ListView
        ArrayAdapter<Photo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedAlbum.getPhotos());
        listView.setAdapter(adapter);
    }
}
