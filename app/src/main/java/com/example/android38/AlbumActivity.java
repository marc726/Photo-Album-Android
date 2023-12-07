package com.example.android38;

import android.app.Activity;
import android.os.Bundle;
import com.example.android38.R;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {



    private Album selectedAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex = 0;

    private ImageView photoImageView;
    private Button slideshowButton;

    private ActivityResultLauncher<Intent> mStartForResult;

    private static final int REQUEST_IMAGE_GET = 1;
    private ImageView imageView; // Assuming you have an ImageView to display the photo
    private Album currentAlbum;  // Assuming you have an Album object to add the photo to
    private Uri currentPhotoUri;


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

        mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri fullPhotoUri = result.getData().getData();
                // Handle the returned URI (e.g., display it or add it to an album)
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

                if (item.getItemId() == R.id.action_add_photo) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mStartForResult.launch(intent);  // Ensure mStartForResult is initialized
                    return true;
                }
                // Handle item clicks here
                int itemId = item.getItemId();
                if (itemId == R.id.action_add_photo) {
                    // Trigger an intent to open the gallery
                    // When you want to start the activity for result
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mStartForResult.launch(intent);
                    return true;
                } else if (itemId == R.id.action_move_photo) {
                    // Show a dialog to select an album to move the photo to
                    showMovePhotoDialog();
                    return true;
                } else if (itemId == R.id.action_delete_photo) {
                    // Confirm and delete the photo
                    showDeletePhotoDialog();
                    return true;
                } else if (itemId == R.id.action_display_photo) {
                    // Show the photo in full screen or with more details
                    if (currentPhotoUri != null) {
                        displayPhoto(currentPhotoUri);
                    } else {
                        // Handle the case where no photo is selected or currentPhotoUri is not set
                        Toast.makeText(AlbumActivity.this, "No photo selected", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            }
        });

        // Show the PopupMenu
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();

            // Display the photo in an ImageView (imageView)
            displayPhoto(fullPhotoUri);

            // Add the photo to the current album
            addPhotoToAlbum(fullPhotoUri);
        }
    }

    private void showMovePhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Photo to Album");

        List<String> allAlbumNames = getAllAlbumNames();
        CharSequence[] albums = allAlbumNames.toArray(new CharSequence[0]);

        builder.setItems(albums, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                movePhotoToAlbum(allAlbumNames.get(which));
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeletePhotoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deletePhoto();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
    private void displayPhoto(Uri photoUri) {
        if (imageView != null) {
            imageView.setImageURI(photoUri); // Directly set the Uri of the image to the ImageView
        }
    }

    private List<String> getAllAlbumNames() {
        // TODO: Return a list of album names
        List<String> albumNames = new ArrayList<>();
        // Add logic to populate albumNames based on your data
        return albumNames;
    }

    private void addPhotoToAlbum(Uri photoUri) {
        if (currentAlbum != null) {
            // Create a new Photo object
            Photo photo = new Photo();
            photo.setImagePath(photoUri.toString()); // Convert Uri to String and set it

            // Add the photo to the album
            currentAlbum.addPhoto(photo);

            // TODO: Update UI if necessary and save changes to persistent storage
        }
    }

    private void movePhotoToAlbum(String albumName) {
        // TODO: Implement the logic to move the current photo to the selected album
        // This involves finding the current photo and album, then moving the photo
    }

    private void deletePhoto() {
        // TODO: Implement the logic to delete the current photo from the album
        // This involves identifying the current photo and removing it
    }


}
