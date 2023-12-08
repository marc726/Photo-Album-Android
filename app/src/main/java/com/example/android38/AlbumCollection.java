package com.example.android38;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumCollection implements Serializable {
    private List<Album> albums;

    public AlbumCollection() {
        this.albums = new ArrayList<>();
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public Album findAlbumByName(String name) {
        for (Album album : albums) {
            if (album.getAlbumName().equals(name)) {
                return album;
            }
        }
        return null;
    }
}
