package oleksandr.lohvinov.lab2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import oleksandr.lohvinov.lab2.data.NoteContract.NoteEntry;

public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TITLE_SEARCH_KEY = "title";
    private static final String IMPRT_FILTER_KEY = "impRate";

    private String[] spinnerItemText = {"All", "Low", "Middle", "High"};
    private Integer[] spinnerItemImages = {
            R.drawable.important_icon_all,
            R.drawable.important_icon_low,
            R.drawable.important_icon_middle,
            R.drawable.important_icon_high};

    private static final int MEMBER_LOADER = 123;

    FloatingActionButton addButton;
    ListView notesList;
    NoteCursorAdapter noteCursorAdapter;

    private String searchText = "";
    private int importanceLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButtonId);
        notesList = findViewById(R.id.notesListId);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                startActivity(intent);
            }
        });

        noteCursorAdapter = new NoteCursorAdapter(this, null, false);
        notesList.setAdapter(noteCursorAdapter);

        registerForContextMenu(notesList);

        LoaderManager.getInstance(this).initLoader(MEMBER_LOADER, null, this);

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                Uri currentMemberUri = ContentUris.withAppendedId(NoteEntry.CONTENT_URI, id);
                intent.setData(currentMemberUri);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) searchItem.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                search();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                search();
                return true;
            }
        });

        MenuItem spinnerItem = menu.findItem(R.id.filterSpinner);
        AppCompatSpinner spinnerView = (AppCompatSpinner) spinnerItem.getActionView();

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(this, R.layout.custom_spinner,
                spinnerItemText, spinnerItemImages);
        spinnerView.setAdapter(spinnerAdapter);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                importanceLevel = position;
                search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                importanceLevel = 0;
                search();
            }
        });

        return super.onCreateOptionsMenu(menu);

    }


    private void search() {
        Bundle bundle = new Bundle();
        if (!searchText.equals("") && searchText != null) {
            bundle.putString(TITLE_SEARCH_KEY, searchText);
        }
        if (importanceLevel > 0) {
            bundle.putInt(IMPRT_FILTER_KEY, importanceLevel - 1);
        }

        LoaderManager.getInstance(this).restartLoader(MEMBER_LOADER, bundle, this);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Do you want delete member?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri deleteNoteUri = ContentUris.withAppendedId(NoteEntry.CONTENT_URI, info.id);
                        int rowsDeleted = getContentResolver().delete(deleteNoteUri, null, null);

                        if (rowsDeleted == 0) {
                            Toast.makeText(MainActivity.this, "Deleting of data in the table failed", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Member is deleted", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selectionTitle = NoteEntry.TITLE + " LIKE '%' || ? || '%' AND ";
        String selectionImportance = NoteEntry.IMPORTANCE + " = ? AND ";

        String finalSelectionString = "";
        ArrayList<String> selectionArgs = new ArrayList<>();
        String[] selectionArgsArr = null;

        if (args != null) {
            if (args.containsKey(TITLE_SEARCH_KEY)) {
                finalSelectionString = finalSelectionString.concat(selectionTitle);
                selectionArgs.add(args.getString(TITLE_SEARCH_KEY));
            }

            if (args.containsKey(IMPRT_FILTER_KEY)) {
                finalSelectionString = finalSelectionString.concat(selectionImportance);
                selectionArgs.add(String.valueOf(args.getInt(IMPRT_FILTER_KEY)));
            }
        }
        if (!finalSelectionString.equals("")) {
            finalSelectionString = finalSelectionString.substring(0, finalSelectionString.length() - 5);
            selectionArgsArr = selectionArgs.toArray(new String[selectionArgs.size()]);
        } else {
            finalSelectionString = null;
            selectionArgs = null;
        }


        String[] projection = {
                NoteEntry.KEY_ID,
                NoteEntry.TITLE,
                NoteEntry.ICON_SRC,
                NoteEntry.IMPORTANCE,
                NoteEntry.CREATION_TIME
        };

        CursorLoader cursorLoader = new CursorLoader(this,
                NoteEntry.CONTENT_URI,
                projection,
                finalSelectionString,
                selectionArgsArr,
                null);
        return cursorLoader;

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        noteCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        noteCursorAdapter.swapCursor(null);
    }
}