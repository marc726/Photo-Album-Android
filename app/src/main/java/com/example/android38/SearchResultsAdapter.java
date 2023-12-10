package com.example.android38;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SearchResultsAdapter extends ArrayAdapter<SearchResult> {
    public SearchResultsAdapter(Context context, List<SearchResult> searchResults) {
        super(context, 0, searchResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchResult searchResult = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result_item, parent, false);
        }

        TextView albumNameTextView = convertView.findViewById(R.id.albumNameTextView);
        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);

        albumNameTextView.setText("Album: " + searchResult.getAlbumName() + "\n" + searchResult.getFormattedTags());
        Uri photoUri = Uri.parse(searchResult.getPhoto().getImagePath());
        photoImageView.setImageURI(photoUri); // Load the image or use a thumbnail loader

        return convertView;
    }
}
