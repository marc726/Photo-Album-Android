package com.example.android38;

import android.nfc.Tag;

import java.io.Serializable;
import java.util.ArrayList;

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
}
