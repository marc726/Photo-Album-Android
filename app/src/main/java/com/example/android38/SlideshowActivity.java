package com.example.android38;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SlideshowActivity extends AppCompatActivity {

    private ArrayList<Photo> photos;
    private int currentPhotoIndex;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        imageView = findViewById(R.id.imageViewSlideshow);
        Button backButton = findViewById(R.id.backButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button prevButton = findViewById(R.id.prevButton);

        // Get the photos and current index from the intent
        photos = (ArrayList<Photo>) getIntent().getSerializableExtra("photos");
        currentPhotoIndex = getIntent().getIntExtra("currentPhotoIndex", 0);

        displayPhoto(currentPhotoIndex);

        backButton.setOnClickListener(v -> finish());
        nextButton.setOnClickListener(v -> displayNextPhoto());
        prevButton.setOnClickListener(v -> displayPreviousPhoto());
    }

    private void displayPhoto(int index) {
        Photo photo = photos.get(index);
        Uri photoUri = Uri.parse(photo.getImagePath());
        Bitmap bitmap = loadThumbnail(photoUri);
        imageView.setImageBitmap(bitmap);
    }

    private void displayNextPhoto() {
        currentPhotoIndex = (currentPhotoIndex + 1) % photos.size();
        displayPhoto(currentPhotoIndex);
    }

    private void displayPreviousPhoto() {
        currentPhotoIndex = (currentPhotoIndex - 1 + photos.size()) % photos.size();
        displayPhoto(currentPhotoIndex);
    }

    private Bitmap loadThumbnail(Uri uri) {
        Bitmap bitmap = null;
        try {
            // Get the content resolver
            ContentResolver contentResolver = getContentResolver();
            // Open an input stream with the URI
            InputStream inputStream = contentResolver.openInputStream(uri);
            // Decode the input stream into a bitmap
            bitmap = BitmapFactory.decodeStream(inputStream);
            // Make sure to close the input stream
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, maybe show a user-friendly message
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

}
