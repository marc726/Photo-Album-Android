package com.example.android38;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumCollection implements Serializable {
    private List<Album> albums = new ArrayList<>();

    public AlbumCollection() {
        this.albums = new ArrayList<>();
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public List<Album> getAlbums() {
        if (albums == null) {
            albums = new ArrayList<>();
        }
        return albums;
    }

    // Add other necessary methods as needed
}
