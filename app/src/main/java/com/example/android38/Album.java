package com.example.android38;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {

    private static final long serialVersionUID = 1L;
    private String albumName;
    private List<Photo> photos;

    // Constructors, getters, and setters
    public Album(String albumName) {
        this.albumName = albumName;
        this.photos = new ArrayList<Photo>();
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void addPhoto(Photo photo) {
        this.photos.add(photo);
    }

    public void removePhoto(Photo photo) {
        this.photos.remove(photo);
    }

    public void removeAllPhotos() {
        getPhotos().clear();
    }

    public String toString() {
        if (photos.isEmpty()) {
            return "Album Name: " + albumName + "\n" + "Number of Photos: 0";
        } else {
            return "Album Name: " + albumName + "\n" + "Number of Photos: " + photos.size();
        }
    }
}
