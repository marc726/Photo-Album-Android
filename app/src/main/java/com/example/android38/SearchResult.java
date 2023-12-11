package com.example.android38;

import java.io.Serializable;



public class SearchResult implements Serializable{
    private static final long serialVersionUID = 1L;
    private String albumName;
    private Photo photo;

    public SearchResult(String albumName, Photo photo) {
        this.albumName = albumName;
        this.photo = photo;
    }

    // Getters
    public String getAlbumName() { return albumName; }
    public Photo getPhoto() { return photo; }

    public String getFormattedTags() {
        StringBuilder tagsBuilder = new StringBuilder();
        for (Tag tag : photo.getTags()) {
            tagsBuilder.append(tag.toString()).append(", ");
        }
        if (tagsBuilder.length() > 0) {
            tagsBuilder.setLength(tagsBuilder.length() - 2); // Remove trailing comma and space
        }
        return tagsBuilder.toString();
    }

}
