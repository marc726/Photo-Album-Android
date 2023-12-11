package com.example.android38;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tagName;
    private String tagValue;

    public Tag(String tagName, String tagValue) {
        this.tagName = tagName;
        this.tagValue = tagValue;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    @Override
    public String toString() {
        return tagName + "=" + tagValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass()!= obj.getClass()) {
            return false;
        }
        Tag tag = (Tag) obj;
        return Objects.equals(tagName, tag.tagName) &&
                Objects.equals(tagValue, tag.tagValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, tagValue);
    }
}
