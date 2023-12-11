package com.example.android38;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;


public class Photo implements Serializable {
    static final long serialVersionUID = 1L;
    private ArrayList<Tag> tags = new ArrayList<Tag>();
    private String name;
    private String imagePath;

    public Photo(String name) {
        this.name = name;
        this.tags = new ArrayList<Tag>();
    }

    public Photo() {
        this.name = "";
        this.tags = new ArrayList<Tag>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(imagePath, photo.imagePath); // or any unique identifier
    }


    public String getName() {
        return name;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }


}
