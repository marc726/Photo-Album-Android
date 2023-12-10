package com.example.android38;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SearchResultsAdapter extends ArrayAdapter<SearchResult> {
    private Context context;

    public SearchResultsAdapter(Context context, List<SearchResult> searchResults) {
        super(context, 0, searchResults);
        this.context = context;
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

        // Load the scaled bitmap
        Bitmap scaledBitmap = loadScaledBitmap(photoUri, 200, 200); // Example dimensions
        if (scaledBitmap != null) {
            photoImageView.setImageBitmap(scaledBitmap);
        }

        return convertView;
    }

    private Bitmap loadScaledBitmap(Uri uri, int desiredWidth, int desiredHeight) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            assert inputStream != null;
            inputStream.close();

            options.inSampleSize = calculateInSampleSize(options, desiredWidth, desiredHeight);
            options.inJustDecodeBounds = false;
            inputStream = contentResolver.openInputStream(uri);
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            assert inputStream != null;
            inputStream.close();

            return scaledBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
