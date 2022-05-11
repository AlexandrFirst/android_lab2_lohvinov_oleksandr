package oleksandr.lohvinov.lab2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import oleksandr.lohvinov.lab2.data.NoteContract.NoteEntry;

public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MEMBER_LOADER = 123;

    FloatingActionButton addButton;
    ListView notesList;
    NoteCursorAdapter noteCursorAdapter;

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
                null,
                null,
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