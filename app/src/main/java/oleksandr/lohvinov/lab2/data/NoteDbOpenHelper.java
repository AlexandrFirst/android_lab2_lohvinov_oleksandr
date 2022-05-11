package oleksandr.lohvinov.lab2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import oleksandr.lohvinov.lab2.data.NoteContract.NoteEntry;


public class NoteDbOpenHelper extends SQLiteOpenHelper {

    public NoteDbOpenHelper(Context context) {
        super(context, NoteContract.DATABASE_NAME, null, NoteContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTE_TABLE = "CREATE TABLE " + NoteEntry.TABLE_NAME + "("
                + NoteEntry.KEY_ID + " INTEGER PRIMARY KEY,"
                + NoteEntry.TITLE + " TEXT,"
                + NoteEntry.DESCRIPTION + " TEXT,"
                + NoteEntry.CREATION_TIME + " TEXT,"
                + NoteEntry.ICON_SRC + " TEXT,"
                + NoteEntry.IMPORTANCE + " INT)";
        db.execSQL(CREATE_NOTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.DATABASE_NAME);
        onCreate(db);
    }
}
