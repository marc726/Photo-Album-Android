package com.example.android38;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private List<Album> albums;
    private ArrayAdapter<Album> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Load album and photo data
        loadAlbumData();

        // Display list of albums
        displayAlbumList();

        // Set up the Create button
        Button createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to create a new album
                // You might want to replace "New Album" with the actual name of the album
                createAlbum("New Album");
            }
        });
    }

    private void loadAlbumData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir() + "/data.dat"))) {
            albums = (List<Album>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (albums == null) {
            albums = new ArrayList<>();
        }
    }

    private void displayAlbumList() {
        ListView listView = findViewById(R.id.albumListView);

        // Create an ArrayAdapter for the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums);
        listView.setAdapter(adapter);

        // Set item click listener for album selection
        //listView.setOnItemClickListener((parent, view, position, id) -> openAlbum(position));
    }

    /*private void openAlbum(int position) {
        // Implement logic to open the selected album
        Album selectedAlbum = albums.get(position);
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("selectedAlbum", selectedAlbum);
        startActivity(intent);
    } */

    // Add methods for creating, deleting, and renaming albums as needed

    private void createAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getAlbumName().equalsIgnoreCase(albumName)) {
                Toast.makeText(this, "Album with the same name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Album newAlbum = new Album(albumName);
        albums.add(newAlbum);
        adapter.notifyDataSetChanged();
        saveAlbumData();
    }
    // Method to save album data using serialization
    private void saveAlbumData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFilesDir() + "/data.dat"))) {
            oos.writeObject(albums);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}