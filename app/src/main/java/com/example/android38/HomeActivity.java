package com.example.android38;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

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
                showCreateAlbumDialog();
            }
        });
    }

    private void loadAlbumData() {
        File file = new File(getFilesDir(), "data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                albums = (List<Album>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            albums = new ArrayList<>();
        }
    }

    private void displayAlbumList() {
        ListView listView = findViewById(R.id.albumListView);

        // Create an ArrayAdapter for the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums);
        listView.setAdapter(adapter);

        // Enable multiple item selection
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set item click listener for album selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openAlbum(position);
            }
        });

        // Set up the Delete button
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to delete the selected album
                int selectedPosition = listView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    deleteAlbum(selectedPosition);
                } else {
                    Toast.makeText(HomeActivity.this, "Select an album to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up the Rename button
        Button renameButton = findViewById(R.id.renameButton);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to rename the selected album
                int selectedPosition = listView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    showRenameAlbumDialog(selectedPosition);
                } else {
                    Toast.makeText(HomeActivity.this, "Select an album to rename", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAlbum(int position) {
        if (position < albums.size()) {
            Album selectedAlbum = albums.get(position);
            Intent intent = new Intent(this, AlbumActivity.class);
            intent.putExtra("selectedAlbum", selectedAlbum);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Album does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Album");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newAlbumName = input.getText().toString();
                createAlbum(newAlbumName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }

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

    private void deleteAlbum(int position) {
        // Implement logic to delete the selected album
        Album deletedAlbum = albums.remove(position);
        Toast.makeText(this, "Deleted album: " + deletedAlbum.getAlbumName(), Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        saveAlbumData();
    }

    private void showRenameAlbumDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Album");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newAlbumName = input.getText().toString();
                renameAlbum(position, newAlbumName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }

    private void renameAlbum(int position, String newAlbumName) {
        // Implement logic to rename the selected album
        Album albumToRename = albums.get(position);
        albumToRename.setAlbumName(newAlbumName);
        Toast.makeText(this, "Renamed album to: " + newAlbumName, Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        saveAlbumData();
    }

    private void saveAlbumData() {
        File file = new File(getFilesDir(), "data.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(albums);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
