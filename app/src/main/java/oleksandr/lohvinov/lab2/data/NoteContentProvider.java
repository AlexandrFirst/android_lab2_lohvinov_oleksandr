package oleksandr.lohvinov.lab2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Date;

import oleksandr.lohvinov.lab2.data.NoteContract.NoteEntry;

public class NoteContentProvider extends ContentProvider {

    NoteDbOpenHelper dbOpenHelper;

    private static final int NOTES = 111;
    private static final int NOTE_ID = 222;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteEntry.TABLE_NAME, NOTES);
        uriMatcher.addURI(NoteContract.AUTHORITY, NoteEntry.TABLE_NAME + "/#", NOTE_ID);
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new NoteDbOpenHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match) {
            case NOTES:
                cursor = db.query(NoteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTE_ID:
                selection = NoteEntry.KEY_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(NoteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can't query incorrect URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteEntry.CONTENT_MULTIPLE_ITEMS;
            case NOTE_ID:
                return NoteEntry.CONTENT_SINGLE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        switch (match) {
            case NOTES:
                long id = db.insert(NoteEntry.TABLE_NAME, null, values);
                if (id == -1) {
                    Log.e("insertMethod", "Insertion of data in the table failed for " + uri);
                    return null;
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                Toast.makeText(getContext(), "Incorrect URI", Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Insertion of data in the table failed for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        int rowsDeleted;

        int match = uriMatcher.match(uri);
        switch (match) {
            case NOTES:
                rowsDeleted = db.delete(NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTE_ID:
                selection = NoteEntry.KEY_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Can't delete incorrect URI " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case NOTES:
                rowsUpdated = db.update(NoteEntry.TABLE_NAME, values, selection, selectionArgs);

                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsUpdated;
            case NOTE_ID:
                selection = NoteEntry.KEY_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsUpdated = db.update(NoteEntry.TABLE_NAME, values, selection, selectionArgs);

                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Can't update incorrect URI " + uri);
        }
    }
}
