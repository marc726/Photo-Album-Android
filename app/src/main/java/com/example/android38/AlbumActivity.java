package com.example.android38;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

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
    }

    private void displayPhotos() {
        ListView listView = findViewById(R.id.photoListView);

        // Create an ArrayAdapter for the ListView
        ArrayAdapter<Photo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedAlbum.getPhotos());
        listView.setAdapter(adapter);
    }
}

