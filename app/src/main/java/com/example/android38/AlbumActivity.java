package com.example.android38;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android38.AlbumCollection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private ArrayAdapter<Photo> adapter;
    private Album selectedAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex = 0;
    private ActivityResultLauncher<Intent> mStartForResult;
    private static final int REQUEST_IMAGE_GET = 1;
    private AlbumCollection albumCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        String albumName = getIntent().getStringExtra("ALBUM_NAME");

        // Load or create the AlbumCollection
        albumCollection = loadAlbumCollection();

        // Find the selected album in the collection or create a new one
        selectedAlbum = albumCollection.findAlbumByName(albumName);
        if (selectedAlbum == null) {
            selectedAlbum = new Album(albumName != null ? albumName : "Default");
            albumCollection.addAlbum(selectedAlbum);
            saveAlbumCollection(albumCollection);
        }

        photos = selectedAlbum.getPhotos();

        // Set up the UI components
        setupUI();

        // Display the photos in the selected album
        displayPhotos();

        // Set up the back button
        Button backButton = findViewById(R.id.button2);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up the slideshow button
        Button slideshowButton = findViewById(R.id.button3);
        slideshowButton.setOnClickListener(v -> startSlideshow());

        // Set up the floating action button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> showPopupMenu(v));

        // Set up result launcher for photo picking
        mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fullPhotoUri = result.getData().getData();
                        addPhotoToAlbum(fullPhotoUri);
                    }
                });
    }

    private void setupUI() {
        // No ListView reference needed, as we're using ImageView directly
    }

    private void displayPhotos() {
        if (!photos.isEmpty()) {
            displaySelectedPhoto(currentPhotoIndex);
        }
    }

    private void displaySelectedPhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            Photo selectedPhoto = photos.get(position);
            String imagePath = selectedPhoto.getImagePath();

            // Assuming you have an ImageView with the id "photoListView" in your layout
            ImageView photoImageView = findViewById(R.id.photoListView);

            // Load and display the image using BitmapFactory
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            photoImageView.setImageBitmap(bitmap);
        }
    }

    private void startSlideshow() {
        if (!photos.isEmpty()) {
            currentPhotoIndex = (currentPhotoIndex + 1) % photos.size();
            displaySelectedPhoto(currentPhotoIndex);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.photo_actions, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_photo) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mStartForResult.launch(intent);
                return true;
            } else if (item.getItemId() == R.id.action_move_photo) {
                showMovePhotoDialog();
                return true;
            } else if (item.getItemId() == R.id.action_delete_photo) {
                showDeletePhotoDialog();
                return true;
            } else if (item.getItemId() == R.id.action_display_photo) {
                // Handle displaying photo details
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void addPhotoToAlbum(Uri photoUri) {
        Photo photo = new Photo();
        photo.setImagePath(photoUri.toString());
        selectedAlbum.addPhoto(photo);
        saveAlbumCollection(albumCollection);
        // No need to notify the adapter when using ImageView
    }

    private void showMovePhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Photo to Album");

        List<String> allAlbumNames = getAllAlbumNames();
        CharSequence[] albums = allAlbumNames.toArray(new CharSequence[0]);

        builder.setItems(albums, (dialog, which) -> movePhotoToAlbum(allAlbumNames.get(which)));

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeletePhotoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deletePhoto())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private List<String> getAllAlbumNames() {
        List<String> albumNames = new ArrayList<>();
        for (Album album : albumCollection.getAlbums()) {
            albumNames.add(album.getAlbumName());
        }
        return albumNames;
    }

    private void movePhotoToAlbum(String albumName) {
        // Implement the logic to move the current photo to the selected album
    }

    private void deletePhoto() {
        // Implement the logic to delete the current photo from the album
    }

    private AlbumCollection loadAlbumCollection() {
        File file = new File(getFilesDir(), "data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (AlbumCollection) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new AlbumCollection();
    }

    private void saveAlbumCollection(AlbumCollection albumCollection) {
        File file = new File(getFilesDir(), "data.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(albumCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
