package oleksandr.lohvinov.lab2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import oleksandr.lohvinov.lab2.data.NoteContract.NoteEntry;

public class EditNoteActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDIT_NOTE_LOADER = 12345;

    private String[] spinnerItemText;
    private Integer[] spinnerItemImages = {

            R.drawable.important_icon_low,
            R.drawable.important_icon_middle,
            R.drawable.important_icon_high};

    private Spinner spinner;
    private Button addImageButton;
    private Button deleteImageButton;

    private EditText noteTitleEditText;
    private EditText noteDescriptionEditText;

    private ImageView noteIconImageView;
    private Uri noteIconUri;

    private Uri currentNoteUri;


    ActivityResultLauncher<String[]> mGetContent = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    noteIconUri = uri;
                    noteIconImageView.setImageURI(Uri.parse(uri.toString()));
                    deleteImageButton.setVisibility(View.VISIBLE);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        spinnerItemText = new String[]{getString(R.string.status_imp_low), getString(R.string.status_imp_mid), getString(R.string.status_imp_high)};


        Intent intent = getIntent();
        currentNoteUri = intent.getData();
        if (currentNoteUri == null) {
            setTitle(getString(R.string.edit_page_label));
        } else {
            setTitle(getString(R.string.edit_page_label));
            LoaderManager.getInstance(this).initLoader(EDIT_NOTE_LOADER, null, this);
        }

        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        noteIconImageView = findViewById(R.id.noteIconImageViewId);

        spinner = findViewById(R.id.selectImportanceRateNote);

        addImageButton = findViewById(R.id.addIconNoteBtn);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch(new String[]{"image/*"});
            }
        });

        deleteImageButton = findViewById(R.id.deleteIconNoteBtn);
        deleteImageButton.setVisibility(View.GONE);
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteIconUri != null) {
                    noteIconUri = null;
                    noteIconImageView.setImageResource(R.drawable.ic_baseline_image_24);
                    deleteImageButton.setVisibility(View.GONE);
                }
            }
        });

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(this, R.layout.custom_spinner,
                spinnerItemText, spinnerItemImages);
        spinner.setAdapter(spinnerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem saveButton = menu.findItem(R.id.saveEditActivityButton);
        MenuItem cancelButton = menu.findItem(R.id.cancelEditActivityButton);

        saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(EditNoteActivity.this, "Saving info...", Toast.LENGTH_SHORT).show();
                saveMember();
                return true;
            }
        });
        cancelButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return true;
            }
        });

        return true;
    }


    private void saveMember() {
        String title = noteTitleEditText.getText().toString().trim();
        String description = noteDescriptionEditText.getText().toString().trim();
        int importanceRate = spinner.getSelectedItemPosition();
        String imageUri = null;
        if (noteIconUri != null) {
            imageUri = noteIconUri.toString();
        }
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String date = currentDate.format(dtf);

        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteEntry.TITLE, title);
        contentValues.put(NoteEntry.DESCRIPTION, description);
        contentValues.put(NoteEntry.ICON_SRC, imageUri);
        contentValues.put(NoteEntry.IMPORTANCE, importanceRate);
        contentValues.put(NoteEntry.CREATION_TIME, date);

        if (currentNoteUri == null) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = contentResolver.insert(NoteEntry.CONTENT_URI, contentValues);
            if (uri == null) {
                Toast.makeText(this, "Insertion of data in the table failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsChanged = getContentResolver().update(currentNoteUri, contentValues, null, null);
            if (rowsChanged == 0) {
                Toast.makeText(this, "Saving of data in the table failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Member updated", Toast.LENGTH_LONG).show();
            }
        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                NoteEntry.KEY_ID,
                NoteEntry.TITLE,
                NoteEntry.ICON_SRC,
                NoteEntry.DESCRIPTION,
                NoteEntry.CREATION_TIME
        };

        CursorLoader cursorLoader = new CursorLoader(this,
                currentNoteUri,
                projection,
                null,
                null,
                null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int iconImageIndex = data.getColumnIndex(NoteEntry.ICON_SRC);
            int titleNoteIndex = data.getColumnIndex(NoteEntry.TITLE);
            int descriptionNoteIndex = data.getColumnIndex(NoteEntry.DESCRIPTION);
            int importanceNoteIndex = data.getColumnIndex(NoteEntry.IMPORTANCE);

            String iconUrl = data.getString(iconImageIndex);
            String title = data.getString(titleNoteIndex);
            String description = data.getString(descriptionNoteIndex);

            if (iconUrl != null && !iconUrl.equals("")) {
                noteIconUri = Uri.parse(iconUrl);
                noteIconImageView.setImageURI(noteIconUri);
                deleteImageButton.setVisibility(View.VISIBLE);
            }
            if (title != null) {
                noteTitleEditText.setText(title);
            }
            if (description != null) {
                noteDescriptionEditText.setText(description);
            }
            spinner.setSelection(importanceNoteIndex);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}