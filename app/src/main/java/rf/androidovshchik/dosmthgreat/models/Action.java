package rf.androidovshchik.dosmthgreat.models;

import android.content.ContentValues;
import android.database.Cursor;

public class Action extends Row {

    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_FILENAME = "filename";

    public String description;

    public String filename;

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (rowId != NONE) {
            values.put(COLUMN_ROWID, rowId);
        }
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_FILENAME, filename);
        return values;
    }

    @Override
    public void parseCursor(Cursor cursor) {
        rowId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROWID));
        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        filename = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILENAME));
    }

    @Override
    public String getTable() {
        return "actions";
    }
}
