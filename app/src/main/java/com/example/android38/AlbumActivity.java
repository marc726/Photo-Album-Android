package com.example.android38;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private Album selectedAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex = 0;

    private ImageView photoImageView;
    private Button slideshowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Retrieve the selected album from the intent
        selectedAlbum = (Album) getIntent().getSerializableExtra("selectedAlbum");
        photos = selectedAlbum.getPhotos();

        // Set up the UI components
        setupUI();

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

        // Set up the slideshow button
        slideshowButton = findViewById(R.id.button3);
        slideshowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSlideshow();
            }
        });

        // Set up the floating action button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
    }

    private void setupUI() {
        ListView listView = findViewById(R.id.photoListView);
        photoImageView = findViewById(R.id.photoImageView);

        // Create an ArrayAdapter for the ListView
        ArrayAdapter<Photo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, photos);
        listView.setAdapter(adapter);

        // Set item click listener for photo selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displaySelectedPhoto(position);
            }
        });
    }

    private void displayPhotos() {
        if (!photos.isEmpty()) {
            // Display the first photo by default
            displaySelectedPhoto(currentPhotoIndex);
        }
    }

    private void displaySelectedPhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            Photo selectedPhoto = photos.get(position);
            // Update the ImageView with the selected photo
            // You need to implement the logic to load and display the photo using its path or other attributes
            // For simplicity, I assume the Photo class has a method getImagePath()
            String imagePath = selectedPhoto.getImagePath();
            // Load and display the image using an image loading library like Picasso or Glide
            // Example: Picasso.get().load(new File(imagePath)).into(photoImageView);
        }
    }

    private void startSlideshow() {
        if (!photos.isEmpty()) {
            // Start the slideshow by incrementing the currentPhotoIndex
            currentPhotoIndex = (currentPhotoIndex + 1) % photos.size();
            // Display the next photo
            displaySelectedPhoto(currentPhotoIndex);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.photo_actions, popupMenu.getMenu());

        // Set up item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle item clicks here
                switch (item.getItemId()) {
                    case R.id.action_add_photo:
                        // Handle add photo action
                        return true;
                    case R.id.action_move_photo:
                        // Handle move photo action
                        return true;
                    case R.id.action_delete_photo:
                        // Handle delete photo action
                        return true;
                    case R.id.action_display_photo:
                        // Handle display photo action
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Show the PopupMenu
        popupMenu.show();
    }
}
