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
    private AlbumCollection albumCollection;


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

        // Set up the Delete button
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlbumDialog();
            }
        });

        // Set up the Rename button
        Button renameButton = findViewById(R.id.renameButton);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameAlbumDialog();
            }
        });

        // Set up the Open button
        Button openButton = findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenAlbumDialog();
            }
        });
    }

    private void loadAlbumData() {
        File file = new File(getFilesDir(), "data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                albumCollection = (AlbumCollection) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (albumCollection == null) {
            albumCollection = new AlbumCollection();
        }
    }


    private void displayAlbumList() {
        ListView listView = findViewById(R.id.albumListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumCollection.getAlbums());
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }


    private void showCreateAlbumDialog() {
        AlbumCollection albumCollection = loadAlbumCollection(); // Load the album collection
        List<Album> albumList = albumCollection.getAlbums();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Album or Create New Album");

        ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumList);
        ListView listView = new ListView(this);
        listView.setAdapter(albumAdapter);

        // Set up the dialog layout
        builder.setView(listView);

        // Set up the buttons
        builder.setPositiveButton("Create New Album", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showCreateNewAlbumDialog();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Create the dialog
        final AlertDialog dialog = builder.create();

        // Set up the item click listener for album selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Close the dialog
                dialog.dismiss();

                // Perform the create action on the selected album
                Album selectedAlbum = albums.get(position);
                createAlbum(selectedAlbum.getAlbumName());
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void showCreateNewAlbumDialog() {
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
        for (Album album : albumCollection.getAlbums()) {
            if (album.getAlbumName().equalsIgnoreCase(albumName)) {
                Toast.makeText(this, "Album with the same name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Album newAlbum = new Album(albumName);
        albumCollection.addAlbum(newAlbum);
        adapter.notifyDataSetChanged();
        saveAlbumData(albumCollection);
    }


    private void showDeleteAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Album to Delete");

        // Load the album collection
        AlbumCollection albumCollection = loadAlbumCollection();
        List<Album> albums = albumCollection.getAlbums(); // Get the list of albums

        // Check if the albums list is not null
        if (albums == null) {
            Toast.makeText(this, "No albums to delete", Toast.LENGTH_SHORT).show();
            return; // Exit the method if there are no albums
        }

        ListView listView = new ListView(this);
        ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums);
        listView.setAdapter(albumAdapter);

        // Set up the dialog layout
        builder.setView(listView);

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = listView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    deleteAlbum(selectedPosition);
                } else {
                    Toast.makeText(HomeActivity.this, "Select an album to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Create the dialog
        final AlertDialog dialog = builder.create();

        // Set up the item click listener for album selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Close the dialog
                dialog.dismiss();

                // Perform the delete action on the selected album
                deleteAlbum(position);
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void deleteAlbum(int position) {
        AlbumCollection albumCollection = loadAlbumCollection(); // Load the album collection
        List<Album> albums = albumCollection.getAlbums();

        if (position >= 0 && position < albums.size()) {
            Album deletedAlbum = albums.remove(position);
            Toast.makeText(this, "Deleted album: " + deletedAlbum.getAlbumName(), Toast.LENGTH_SHORT).show();

            // Save the updated AlbumCollection
            saveAlbumData(albumCollection);

            // Update the adapter with the new albums list
            adapter.clear();
            adapter.addAll(albums);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Invalid album selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRenameAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Album to Rename");

        AlbumCollection albumCollection = loadAlbumCollection(); // Load the album collection
        List<Album> albumList = albumCollection.getAlbums();

        ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumList);
        ListView listView = new ListView(this);
        listView.setAdapter(albumAdapter);

        // Set up the dialog layout
        builder.setView(listView);

        // Set up the buttons
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = listView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION && selectedPosition < albumList.size()) {
                    showRenameDialog(selectedPosition);
                } else {
                    Toast.makeText(HomeActivity.this, "Select an album to rename", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Create the dialog
        final AlertDialog dialog = builder.create();

        // Set up the item click listener for album selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Close the dialog
                dialog.dismiss();

                // Perform the rename action on the selected album
                showRenameDialog(position);
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void showRenameDialog(final int position) {
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
        AlbumCollection albumCollection = loadAlbumCollection(); // Load the album collection
        List<Album> albumList = albumCollection.getAlbums();

        if (position >= 0 && position < albumList.size()) {
            Album albumToRename = albumList.get(position);
            albumToRename.setAlbumName(newAlbumName);


            adapter.clear();
            adapter.clear();
            adapter.addAll(albumList);
            adapter.notifyDataSetChanged();

            saveAlbumData(albumCollection); // Save the updated collection

            Toast.makeText(this, "Renamed album to: " + newAlbumName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid album selection", Toast.LENGTH_SHORT).show();
        }
    }


    private void showOpenAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Album to Open");

        AlbumCollection albumCollection = loadAlbumCollection(); // Load the album collection
        List<Album> albumList = albumCollection.getAlbums();

        ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumList);
        ListView listView = new ListView(this);
        listView.setAdapter(albumAdapter);

        // Set up the dialog layout
        builder.setView(listView);

        // Set up the buttons
        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = listView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    openAlbum(selectedPosition);
                } else {
                    Toast.makeText(HomeActivity.this, "Select an album to open", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Create the dialog
        final AlertDialog dialog = builder.create();

        // Set up the item click listener for album selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Close the dialog
                dialog.dismiss();

                // Perform the open action on the selected album
                openAlbum(position);
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void openAlbum(int position) {
        if (position < albumCollection.getAlbums().size()) {
            Album selectedAlbum = albumCollection.getAlbums().get(position);
            Intent intent = new Intent(this, AlbumActivity.class);
            // Pass the album name as a string
            intent.putExtra("selectedAlbum", selectedAlbum.getAlbumName());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Album does not exist", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveAlbumData(AlbumCollection albumCollection) {
        File file = new File(getFilesDir(), "data.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(albumCollection);
        } catch (IOException e) {
            e.printStackTrace();
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
