package oleksandr.lohvinov.lab2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteContract {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "notes";

    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "oleksandr.lohvinov.lab2";

    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    public NoteContract() {

    }

    public static final class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "note";
        public static final String KEY_ID = BaseColumns._ID;
        public static final String TITLE = "title";
        public static final String ICON_SRC = "iconSrc";
        public static final String CREATION_TIME = "createTime";
        public static final String IMPORTANCE = "importance";
        public static final String DESCRIPTION = "description";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String CONTENT_MULTIPLE_ITEMS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_SINGLE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE_NAME;

        public enum ImportanceRate {NONE, LOW, MEDIUM, HIGH}
    }
}
