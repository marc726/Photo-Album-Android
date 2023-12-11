package com.example.android38;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

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

        // Desired dimensions for Nexus 4
        final int desiredWidth = 384;
        final int desiredHeight = 640;


        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            // Get the dimensions of the original bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate the inSampleSize value (how much to scale down the image)
            int inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);

            // Decode the image file into a smaller image to save memory
            options.inSampleSize = inSampleSize;
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


}
