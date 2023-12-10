package com.example.android38;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteSearchText1, autoCompleteSearchText2;
    private Spinner searchTypeSpinner1, searchTypeSpinner2;
    private ListView searchResultsListView;
    private CheckBox checkBoxConjunction, checkBoxDisjunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize UI components
        autoCompleteSearchText1 = findViewById(R.id.autoCompleteSearchText1);
        autoCompleteSearchText2 = findViewById(R.id.autoCompleteSearchText2);
        searchTypeSpinner1 = findViewById(R.id.tagTypeSpinner1);
        searchTypeSpinner2 = findViewById(R.id.spinnerTagType2);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        checkBoxConjunction = findViewById(R.id.checkBoxConjunction);
        checkBoxDisjunction = findViewById(R.id.checkBoxDisjunction);

        setupUI();
    }

    private void setupUI() {
        // Setup listeners for the spinners and AutoCompleteTextViews
        setupSpinners();
        setupAutoCompleteTextViews();
        setupCheckBoxes();

        TextView backButton = findViewById(R.id.backButton);
        TextView clearSearchButton = findViewById(R.id.clearSearchButton);

        backButton.setOnClickListener(v -> onBackPressed());
        clearSearchButton.setOnClickListener(v -> resetSearch());
    }



    private void setupSpinners() {
        // First Spinner setup
        searchTypeSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAutoCompleteSuggestions(autoCompleteSearchText1, parent.getItemAtPosition(position).toString());
                uncheckBothCheckBoxes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Second Spinner setup
        searchTypeSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAutoCompleteSuggestions(autoCompleteSearchText2, parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupAutoCompleteTextViews() {
        // TextChangedListeners for the AutoCompleteTextViews
        autoCompleteSearchText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldPerformSearch()) {
                    performSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoCompleteSearchText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldPerformSearch()) {
                    performSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void setupCheckBoxes() {
        // Listener for the conjunction checkbox
        checkBoxConjunction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxDisjunction.setChecked(false);
                searchTypeSpinner2.setVisibility(View.VISIBLE);
                autoCompleteSearchText2.setVisibility(View.VISIBLE);
            } else if (!checkBoxDisjunction.isChecked()) {
                resetSecondSearchCriteria();
            }
            performSearchIfNeeded();
        });

        // Listener for the disjunction checkbox
        checkBoxDisjunction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBoxConjunction.setChecked(false);
                searchTypeSpinner2.setVisibility(View.VISIBLE);
                autoCompleteSearchText2.setVisibility(View.VISIBLE);
            } else if (!checkBoxConjunction.isChecked()) {
                resetSecondSearchCriteria();
            }
            performSearchIfNeeded();
        });
    }

    private void uncheckBothCheckBoxes() {
        checkBoxConjunction.setChecked(false);
        checkBoxDisjunction.setChecked(false);
        resetSecondSearchCriteria(); // Hide and reset the second search criteria UI
        performSearchIfNeeded(); // Perform search again if needed
    }

    private boolean shouldPerformSearch() {
        if (checkBoxConjunction.isChecked() || checkBoxDisjunction.isChecked()) {
            return !autoCompleteSearchText1.getText().toString().isEmpty() &&
                    !autoCompleteSearchText2.getText().toString().isEmpty();
        }
        return !autoCompleteSearchText1.getText().toString().isEmpty();
    }

    private void resetSecondSearchCriteria() {
        searchTypeSpinner2.setVisibility(View.GONE);
        autoCompleteSearchText2.setText("");
        autoCompleteSearchText2.setVisibility(View.GONE);
    }

    private void performSearchIfNeeded() {
        if (shouldPerformSearch()) {
            performSearch();
        }
    }


    private void performSearch() {
        String query1 = autoCompleteSearchText1.getText().toString().trim();
        String searchType1 = searchTypeSpinner1.getSelectedItem() != null ? searchTypeSpinner1.getSelectedItem().toString() : "";

        boolean isConjunction = checkBoxConjunction.isChecked();
        boolean isDisjunction = checkBoxDisjunction.isChecked();

        List<SearchResult> searchResults = new ArrayList<>();

        AlbumCollection albumCollection = loadAlbumCollection(); // Load your albums

        if (isConjunction || isDisjunction) {
            // Perform search with both criteria
            String query2 = autoCompleteSearchText2.getText().toString().trim();
            String searchType2 = searchTypeSpinner2.getSelectedItem() != null ? searchTypeSpinner2.getSelectedItem().toString() : "";

            for (Album album : albumCollection.getAlbums()) {
                for (Photo photo : album.getPhotos()) {
                    boolean matchesFirstCriteria = matchesSearchCriteria(photo, query1, searchType1);
                    boolean matchesSecondCriteria = matchesSearchCriteria(photo, query2, searchType2);

                    if ((isConjunction && matchesFirstCriteria && matchesSecondCriteria) ||
                            (isDisjunction && (matchesFirstCriteria || matchesSecondCriteria))) {
                        searchResults.add(new SearchResult(album.getAlbumName(), photo));
                    }
                }
            }
        } else {
            // Perform search with single criteria
            for (Album album : albumCollection.getAlbums()) {
                for (Photo photo : album.getPhotos()) {
                    if (matchesSearchCriteria(photo, query1, searchType1)) {
                        searchResults.add(new SearchResult(album.getAlbumName(), photo));
                    }
                }
            }
        }

        SearchResultsAdapter adapter = new SearchResultsAdapter(this, searchResults);
        searchResultsListView.setAdapter(adapter);
    }


    private boolean matchesSearchCriteria(Photo photo, String query, String searchType) {
        if (query.isEmpty()) return false; // Skip empty queries
        for (Tag tag : photo.getTags()) {
            if (tag.getTagName().equalsIgnoreCase(searchType) && tag.getTagValue().toLowerCase().contains(query.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    private void resetSearch() {
        // Reset the first autocomplete text field
        AutoCompleteTextView autoCompleteSearchText1 = findViewById(R.id.autoCompleteSearchText1);
        autoCompleteSearchText1.setText("");

        // Reset the second autocomplete text field and hide it
        AutoCompleteTextView autoCompleteSearchText2 = findViewById(R.id.autoCompleteSearchText2);
        autoCompleteSearchText2.setText("");
        autoCompleteSearchText2.setVisibility(View.GONE);

        // Hide the second spinner
        Spinner searchTypeSpinner2 = findViewById(R.id.spinnerTagType2);
        searchTypeSpinner2.setVisibility(View.GONE);

        // Reset and hide the checkboxes for conjunction and disjunction
        CheckBox checkBoxConjunction = findViewById(R.id.checkBoxConjunction);
        checkBoxConjunction.setChecked(false);

        CheckBox checkBoxDisjunction = findViewById(R.id.checkBoxDisjunction);
        checkBoxDisjunction.setChecked(false);

        // Clear the search results
        ListView searchResultsListView = findViewById(R.id.searchResultsListView);
        searchResultsListView.setAdapter(null);

        // Reinitialize the autocomplete suggestions for the first AutoCompleteTextView
        String selectedTagType = searchTypeSpinner1.getSelectedItem().toString();
        updateAutoCompleteSuggestions(autoCompleteSearchText1, selectedTagType);
    }



    private void updateAutoCompleteSuggestions(AutoCompleteTextView autoCompleteTextView, String tagType) {
        List<String> suggestions = getAllTagValuesForType(tagType);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        autoCompleteTextView.setAdapter(adapter);
    }



    private List<String> getAllTagValuesForType(String tagType) {
        Set<String> uniqueTagValues = new HashSet<>();
        AlbumCollection albumCollection = loadAlbumCollection(); // Load your albums

        for (Album album : albumCollection.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase(tagType)) {
                        uniqueTagValues.add(tag.getTagValue());
                    }
                }
            }
        }

        return new ArrayList<>(uniqueTagValues);
    }


    private AlbumCollection loadAlbumCollection() {
        File file = new File(getFilesDir(), "data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                return (AlbumCollection) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new AlbumCollection();
    }
}
