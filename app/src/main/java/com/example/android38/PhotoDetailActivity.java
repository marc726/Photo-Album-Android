package com.example.android38;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PhotoDetailActivity extends Activity {

    private ImageView photoImageView;
    private TextView tagTextView;
    private Photo selectedPhoto;
    private AlbumCollection albumCollection;
    private String photoUriString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // Load album collection here or in onResume
        albumCollection = loadAlbumCollection();

        // Initialize views
        photoImageView = findViewById(R.id.photoImageView);
        tagTextView = findViewById(R.id.tagTextView);
        Button backButton = findViewById(R.id.backButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button moveButton = findViewById(R.id.moveButton);
        Button addTagButton = findViewById(R.id.addTagButton);
        Button removeTagButton = findViewById(R.id.removeTagButton);

        // Load the photo URI from the intent
        photoUriString = getIntent().getStringExtra("photoUri");

        // Initialize selectedPhoto based on the URI
        selectedPhoto = findPhotoByUri(photoUriString);

        if (selectedPhoto == null) {
            Toast.makeText(this, "Photo data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if photo data is not available
            return;
        }

        // Load and display the photo
        Uri photoUri = Uri.parse(selectedPhoto.getImagePath());
        Bitmap bitmap = loadScaledBitmap(photoUri);
        if (bitmap != null) {
            photoImageView.setImageBitmap(bitmap);
        }

        // Load and display tags
        updateTagDisplay();

        // Set button functionality
        backButton.setOnClickListener(v -> finish());
        deleteButton.setOnClickListener(v -> deletePhoto());
        moveButton.setOnClickListener(v -> movePhoto());
        addTagButton.setOnClickListener(v -> addTag());
        removeTagButton.setOnClickListener(v -> removeTag());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload album collection to get updated data
        albumCollection = loadAlbumCollection();
        selectedPhoto = findPhotoByUri(photoUriString);

        if (selectedPhoto == null) {
            Toast.makeText(this, "Photo data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if photo data is not available
            return;
        }

        // Refresh UI with updated photo data
        refreshUI();
    }

    private void refreshUI() {
        // Load and display the photo and tags
        Uri photoUri = Uri.parse(selectedPhoto.getImagePath());
        Bitmap bitmap = loadScaledBitmap(photoUri);
        if (bitmap != null) {
            photoImageView.setImageBitmap(bitmap);
        }
        updateTagDisplay();
    }

    private Photo findPhotoByUri(String photoUriString) {
        AlbumCollection albumCollection = loadAlbumCollection();

        for (Album album : albumCollection.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                if (photo.getImagePath().equals(photoUriString)) {
                    return photo;
                }
            }
        }

        return null; // Return null if no matching photo is found
    }


    private void deletePhoto() {
        Album album = findAlbumContainingPhoto(selectedPhoto);
        if (album != null) {
            album.getPhotos().remove(selectedPhoto);
            saveAlbumCollection(albumCollection); // Save changes
            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Set result for the calling activity
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Error: Photo not found in any album", Toast.LENGTH_SHORT).show();
        }
    }


    private Album findAlbumContainingPhoto(Photo photo) {
        for (Album album : albumCollection.getAlbums()) {
            if (album.getPhotos().contains(photo)) {
                return album;
            }
        }
        return null;
    }


    private void movePhoto() {
        if (selectedPhoto != null) {
            showMovePhotoDialog();
        } else {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMovePhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Album to Move Photo To");

        List<String> albumNames = getAlbumNames();
        CharSequence[] albums = albumNames.toArray(new CharSequence[0]);

        builder.setItems(albums, (dialog, which) -> movePhotoToAlbum(albumNames.get(which)));

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> getAlbumNames() {
        List<String> names = new ArrayList<>();
        for (Album album : albumCollection.getAlbums()) {
            names.add(album.getAlbumName());
        }
        return names;
    }

    private void movePhotoToAlbum(String targetAlbumName) {
        Album currentAlbum = findAlbumContainingPhoto(selectedPhoto);
        Album targetAlbum = albumCollection.findAlbumByName(targetAlbumName);

        if (currentAlbum != null && targetAlbum != null && !currentAlbum.equals(targetAlbum)) {
            currentAlbum.getPhotos().remove(selectedPhoto);
            targetAlbum.addPhoto(selectedPhoto);
            saveAlbumCollection(albumCollection); // Save changes to the album collection
            Toast.makeText(this, "Photo moved to " + targetAlbumName, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Set result for the calling activity
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Error: Unable to move photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTag() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_tag, null);

        final Spinner spinnerTagType = dialogView.findViewById(R.id.spinnerTagType);
        final EditText editTextTagValue = dialogView.findViewById(R.id.editTextTagValue);
        final Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        final Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        editTextTagValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonConfirm.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(dialogView)
                .create();

        buttonConfirm.setOnClickListener(v -> {
            String tagType = spinnerTagType.getSelectedItem().toString();
            String tagValue = editTextTagValue.getText().toString();

            if (isTagTypeAlreadyPresent(tagType)) {
                Toast.makeText(PhotoDetailActivity.this, "Error: Tag type \"" + tagType + "\" already exists for this photo.", Toast.LENGTH_LONG).show();
                return;
            }

            selectedPhoto.addTag(new Tag(tagType, tagValue));
            updateSelectedPhotoInAlbumCollection(selectedPhoto); // Ensure the updated photo is in the album collection
            updateTagDisplay();
            saveAlbumCollection(albumCollection); // Save changes
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }


    private void removeTag() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_remove_tag, null);

        final Spinner spinnerTag = dialogView.findViewById(R.id.spinnerTag);
        final Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        final Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Populate the spinner with existing tags
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selectedPhoto.getTags());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTag.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Remove Tag")
                .setView(dialogView)
                .create();

        buttonConfirm.setOnClickListener(v -> {
            Tag selectedTag = (Tag) spinnerTag.getSelectedItem();
            selectedPhoto.removeTag(selectedTag); // Remove the selected tag
            updateSelectedPhotoInAlbumCollection(selectedPhoto); // Ensure the updated photo is in the album collection
            updateTagDisplay();
            saveAlbumCollection(albumCollection); // Save changes
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void updateTagDisplay() {
        StringBuilder tagBuilder = new StringBuilder();
        for (Tag tag : selectedPhoto.getTags()) {
            tagBuilder.append(tag.toString()).append(", ");
        }
        tagTextView.setText("Tags: " + tagBuilder.toString());
    }

    private void updateSelectedPhotoInAlbumCollection(Photo updatedPhoto) {
        for (Album album : albumCollection.getAlbums()) {
            for (int i = 0; i < album.getPhotos().size(); i++) {
                if (album.getPhotos().get(i).getImagePath().equals(updatedPhoto.getImagePath())) {
                    album.getPhotos().set(i, updatedPhoto); // Update the photo in the album
                    break;
                }
            }
        }
    }

    private boolean isTagTypeAlreadyPresent(String tagType) {
        for (Tag tag : selectedPhoto.getTags()) {
            if (tag.getTagName().equals(tagType)) {
                return true;
            }
        }
        return false;
    }

    private Bitmap loadScaledBitmap(Uri uri) {
        // Desired dimensions for Nexus 4
        final int desiredWidth = 384;  // Half of 768px
        final int desiredHeight = 640; // Half of 1280px

        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            // Get the dimensions of the original bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate the inSampleSize value

            // Decode the image file into a smaller image to save memory
            options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
            options.inJustDecodeBounds = false;
            inputStream = contentResolver.openInputStream(uri);
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return scaledBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
