package com.example.android38;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
public class SearchActivity extends AppCompatActivity {
    private AlbumCollection albumCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); // Make sure to create a corresponding layout

        albumCollection = loadAlbumCollection(); // Load the album collection
        showSearchDialog();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle("Search by Tag");

        builder.setItems(new CharSequence[]{"Location", "Person"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showTagValueInput("Location");
                } else {
                    showTagValueInput("Person");
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTagValueInput(final String tagType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle("Enter " + tagType + " Tag Value");

        final EditText input = new EditText(SearchActivity.this);
        builder.setView(input);

        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tagValue = input.getText().toString();
                performSearch(tagType, tagValue);
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performSearch(String tagType, String tagValue) {
        ArrayList<Photo> matchingPhotos = new ArrayList<>();
        String searchTagType = tagType.toLowerCase();
        String searchTagValue = tagValue.toLowerCase();

        for (Album album : albumCollection.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    String tagTypeName = tag.getTagName().toLowerCase();
                    String tagVal = tag.getTagValue().toLowerCase();

                    if (tagTypeName.equals(searchTagType) && tagVal.startsWith(searchTagValue)) {
                        matchingPhotos.add(photo);
                    }
                }
            }
        }

        if (!matchingPhotos.isEmpty()) {
            Intent intent = new Intent(SearchActivity.this, PhotoDetailActivity.class);
            intent.putExtra("matchingPhotos", matchingPhotos);
            startActivity(intent);
        } else {
            Toast.makeText(SearchActivity.this, "No matching photos found", Toast.LENGTH_LONG).show();
        }
    }

    private AlbumCollection loadAlbumCollection() {
        File file = new File(getFilesDir(), "data.dat");
        AlbumCollection albumCollection = null;

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object data = ois.readObject();
                if (data instanceof AlbumCollection) {
                    albumCollection = (AlbumCollection) data;
                } else {
                    albumCollection = new AlbumCollection(); // Create a new collection if the data is not an AlbumCollection
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                albumCollection = new AlbumCollection(); // Create a new collection if an exception occurs
            }
        } else {
            albumCollection = new AlbumCollection(); // Create a new collection if the file does not exist
        }

        return albumCollection;
    }
}
