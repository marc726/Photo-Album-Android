package com.example.android38;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private Album selectedAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex = 0;
    private static final int PHOTO_DETAIL_REQUEST = 1; // You can choose any integer
    private ActivityResultLauncher<Intent> mStartForResult;
    private GridLayout photoGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        String albumName = getIntent().getStringExtra("selectedAlbum");

        // Load or create the AlbumCollection
        AlbumCollection albumCollection = loadAlbumCollection();

        // Find the selected album in the collection
        selectedAlbum = albumCollection.findAlbumByName(albumName);

        if (selectedAlbum == null) {
            if (albumName != null && !albumName.trim().isEmpty()) {
                // Handle the case where the album does not exist but the name is provided
                selectedAlbum = new Album(albumName);
                albumCollection.addAlbum(selectedAlbum);
                saveAlbumCollection(albumCollection);
            } else {
                // Handle the error appropriately, for example:
                Toast.makeText(this, "Error: Album name is missing or invalid", Toast.LENGTH_LONG).show();
                // Optionally redirect the user or close the activity
                finish(); // Closes the current activity and returns to the previous one
            }
        } else {
            photos = selectedAlbum.getPhotos();
            // Continue with your existing logic for when the album is found
        }

        photos = selectedAlbum.getPhotos();

        // Set up the UI components
        setupUI();

        // Display the photos in the selected album
        displayPhotos();

        // Set up the back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up the slideshow button
        Button slideshowButton = findViewById(R.id.slideshowButton);
        slideshowButton.setOnClickListener(v -> startSlideshow());

        // Set up the floating action button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this::showPopupMenu);

        // Set up result launcher for photo picking
        mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fullPhotoUri = result.getData().getData();
                        assert fullPhotoUri != null;
                        addPhotoToAlbum(fullPhotoUri);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_DETAIL_REQUEST && resultCode == RESULT_OK) {
            // Refresh album data
            AlbumCollection albumCollection = loadAlbumCollection();
            selectedAlbum = albumCollection.findAlbumByName(selectedAlbum.getAlbumName());
            if (selectedAlbum != null) {
                photos = selectedAlbum.getPhotos();
            }
            displayPhotos();
        }
    }


    private void setupUI() {
        photoGridView = findViewById(R.id.photoGridView);
    }

    private void displayPhotos() {
        photoGridView.removeAllViews();
        int column = 2;  // Set the number of columns to 2
        int total = photos.size();
        int rows = (int) Math.ceil((double) total / column);
        photoGridView.setRowCount(rows);

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int imageWidth = screenWidth / column; // Calculate the width for each image

        for (int i = 0; i < total; i++) {
            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageWidth;
            params.height = imageWidth; // Adjust this for your desired aspect ratio

            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(i % column);
            params.rowSpec = GridLayout.spec(i / column);
            imageView.setLayoutParams(params);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0); // Set padding to 0 for flush photos

            // Load and set the image for each photo
            Photo selectedPhoto = photos.get(i);
            Uri photoUri = Uri.parse(selectedPhoto.getImagePath());
            Bitmap bitmap = loadThumbnail(photoUri, imageWidth, imageWidth);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            final int photoIndex = i; // Capture the index for use in the click listener
            imageView.setOnClickListener(v -> onPhotoSelected(photoIndex)); // Set click listener

            photoGridView.addView(imageView); // Add imageView to photoGridView
        }
    }

    private void startSlideshow() {
        if (!photos.isEmpty()) {
            Intent slideshowIntent = new Intent(this, SlideshowActivity.class);
            slideshowIntent.putExtra("photos", new ArrayList<>(photos));
            slideshowIntent.putExtra("currentPhotoIndex", currentPhotoIndex);
            startActivity(slideshowIntent);
        }
    }


    private void onPhotoSelected(int photoIndex) {
        Photo selectedPhoto = photos.get(photoIndex);
        Intent intent = new Intent(this, PhotoDetailActivity.class);
        intent.putExtra("photoUri", selectedPhoto.getImagePath());
        startActivityForResult(intent, PHOTO_DETAIL_REQUEST); // Define PHOTO_DETAIL_REQUEST as a constant
    }



    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.photo_actions, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_photo) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mStartForResult.launch(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void addPhotoToAlbum(Uri photoUri) {
        Photo photo = new Photo();
        AlbumCollection albumCollection = loadAlbumCollection();

        // Check if the Uri is a content Uri
        if ("content".equals(photoUri.getScheme())) {
            // If the Uri is a content Uri, create a copy of the image in the app's internal storage
            photoUri = saveImageToInternalStorage(photoUri);
        }

        photo.setImagePath(photoUri.toString());

        // Find the corresponding album in the loaded collection
        Album albumToUpdate = albumCollection.findAlbumByName(selectedAlbum.getAlbumName());
        if (albumToUpdate != null) {
            albumToUpdate.addPhoto(photo);
            photos = albumToUpdate.getPhotos(); // Update the photos list
        } else {
            Toast.makeText(this, "Error: Album not found in the collection", Toast.LENGTH_LONG).show();
            return;
        }

        // Save the album collection with the newly added photo
        saveAlbumCollection(albumCollection);

        // Update UI on the main thread
        runOnUiThread(this::displayPhotos);
    }


    private Uri saveImageToInternalStorage(Uri sourceUri) {
        // Create a new file in the internal storage
        String fileName = "image_" + System.currentTimeMillis() + ".png";
        File internalFile = new File(getFilesDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = Files.newOutputStream(internalFile.toPath())) {
            if (inputStream != null) {
                // Copy the image data from the source Uri to the internal file
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Return the Uri of the internal file
                return Uri.fromFile(internalFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the original Uri if an error occurred
        return sourceUri;
    }


    private Bitmap loadThumbnail(Uri uri, int targetW, int targetH) {
        try {
            ContentResolver resolver = getContentResolver();
            ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

                // Set inSampleSize to scale down the image
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
                parcelFileDescriptor.close();
                return Bitmap.createScaledBitmap(image, targetW, targetH, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // _________________________________________________________________
//                              FILE IO

    private void saveAlbumCollection(AlbumCollection albumCollection) {
        File file = new File(getFilesDir(), "data.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {
            oos.writeObject(albumCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AlbumCollection loadAlbumCollection() {
        File file = new File(getFilesDir(), "data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                return (AlbumCollection) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new AlbumCollection();
    }
}
