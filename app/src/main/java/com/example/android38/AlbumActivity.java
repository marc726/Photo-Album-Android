package com.example.android38;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private Album selectedAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex = 0;
    private ActivityResultLauncher<Intent> mStartForResult;
    private static final int REQUEST_IMAGE_GET = 1;
    private GridLayout photoGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        String albumName = getIntent().getStringExtra("ALBUM_NAME");

        // Load or create the AlbumCollection
        AlbumCollection albumCollection = loadAlbumCollection();

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
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fullPhotoUri = result.getData().getData();
                        assert fullPhotoUri != null;
                        addPhotoToAlbum(fullPhotoUri);
                    }
                });
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
            params.height = imageWidth; // You might want to adjust this for your desired aspect ratio

            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(i % column);
            params.rowSpec = GridLayout.spec(i / column);
            imageView.setLayoutParams(params);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0); // Set padding to 0 for flush photos

            // Load and set the image for each photo
            Photo selectedPhoto = photos.get(i);
            Uri photoUri = Uri.parse(selectedPhoto.getImagePath());
            Bitmap bitmap = loadThumbnail(photoUri, imageWidth, imageWidth); // Ensure you pass the new dimensions to the loading method
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            imageView.setOnClickListener(v -> {
                // Handle displaying photo details
            });

            photoGridView.addView(imageView);
            photoGridView.requestLayout();
        }
    }




    private void startSlideshow() {
        if (!photos.isEmpty()) {
            currentPhotoIndex = (currentPhotoIndex + 1) % photos.size();
            displaySelectedPhoto(currentPhotoIndex);
        }
    }

    private void displaySelectedPhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            Photo selectedPhoto = photos.get(position);
            Uri photoUri = Uri.parse(selectedPhoto.getImagePath());

            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int imageWidth = screenWidth / 2; // To make it two images per row
            params.width = imageWidth;
            params.height = imageWidth; // This assumes a square aspect ratio

            params.columnSpec = GridLayout.spec(position % 2);
            params.rowSpec = GridLayout.spec(position / 2);
            imageView.setLayoutParams(params);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0); // Adjust padding as needed

            // Load and set the image for the selected photo
            Bitmap bitmap = loadThumbnail(photoUri, imageWidth, imageWidth); // Now passing width and height
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            // Add click listener to view photo details or implement your logic
            imageView.setOnClickListener(v -> {
                // Handle displaying photo details
            });

            photoGridView.addView(imageView);
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

        // Check if the Uri is a content Uri
        if ("content".equals(photoUri.getScheme())) {
            // If the Uri is a content Uri, create a copy of the image in the app's internal storage
            photoUri = saveImageToInternalStorage(photoUri);
        }

        photo.setImagePath(photoUri.toString());
        selectedAlbum.addPhoto(photo);
        saveAlbumCollection(loadAlbumCollection());
        // No need to notify the adapter when using ImageView
        displayPhotos();
    }

    private Uri saveImageToInternalStorage(Uri sourceUri) {
        // Create a new file in the internal storage
        String fileName = "image_" + System.currentTimeMillis() + ".png";
        File internalFile = new File(getFilesDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(internalFile)) {
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
        for (Album album : loadAlbumCollection().getAlbums()) {
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

    private void showPhotoActionDialog(boolean isDeleteAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isDeleteAction ? "Delete Photo" : "Move Photo");

        // Convert the photo file paths or names into a CharSequence array for the dialog
        CharSequence[] photoNames = new CharSequence[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            photoNames[i] = new File(photos.get(i).getImagePath()).getName();
        }

        builder.setItems(photoNames, (dialog, which) -> {
            if (isDeleteAction) {
                deletePhoto(which);
            } else {
                showMoveAlbumSelectionDialog(which);
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showMoveAlbumSelectionDialog(int photoIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Album to Move Photo To");

        // Assuming you have a method to get all album names
        List<String> albumNames = getAllAlbumNames();
        CharSequence[] albums = albumNames.toArray(new CharSequence[0]);

        builder.setItems(albums, (dialog, which) -> movePhotoToAlbum(photoIndex, albumNames.get(which)));

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void movePhotoToAlbum(int photoIndex, String targetAlbumName) {
        Photo photo = photos.get(photoIndex);
        Album targetAlbum = loadAlbumCollection().findAlbumByName(targetAlbumName);

        if (targetAlbum != null && !selectedAlbum.getAlbumName().equals(targetAlbumName)) {
            // Remove from current album and add to the target album
            selectedAlbum.getPhotos().remove(photoIndex);
            targetAlbum.getPhotos().add(photo);

            // Save the updated album collection
            saveAlbumCollection(loadAlbumCollection());

            // Update UI
            displayPhotos();
        }
    }

    private void deletePhoto(int photoIndex) {
        // Remove photo from the album
        photos.remove(photoIndex);

        // Save the updated album collection
        saveAlbumCollection(loadAlbumCollection());

        // Update UI
        displayPhotos();
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
