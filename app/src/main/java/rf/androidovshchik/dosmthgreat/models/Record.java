package rf.androidovshchik.dosmthgreat.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;

import io.reactivex.annotations.Nullable;

public class Record extends Row {

    @SuppressWarnings("all")
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_COMMENT = "comment";
    private static final String COLUMN_ACTIONS = "actions";

    @Nullable
    public String day;

    public String comment;

    public int actions;

    public Record() {}

    public Record(String day, String comment) {
        this.day = day;
        this.comment = comment;
        this.actions = 0;
    }

    public Record(String day, int actions) {
        this.day = day;
        this.actions = actions;
        this.comment = "";
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (rowId != NONE) {
            values.put(COLUMN_ROWID, rowId);
        }
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_COMMENT, comment);
        values.put(COLUMN_ACTIONS, actions);
        return values;
    }

    @Override
    public void parseCursor(Cursor cursor) {
        rowId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROWID));
        day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY));
        comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT));
        actions = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTIONS));
    }

    @Override
    public String getTable() {
        return "records";
    }
}
