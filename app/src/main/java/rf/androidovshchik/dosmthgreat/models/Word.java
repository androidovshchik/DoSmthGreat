package rf.androidovshchik.dosmthgreat.models;

import android.content.ContentValues;
import android.database.Cursor;

public class Word extends Row {

    private static final String COLUMN_WORD = "word";
    private static final String COLUMN_BEST = "best";

    public String word;

    public boolean best;

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (rowId != NONE) {
            values.put(COLUMN_ROWID, rowId);
        }
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_BEST, best ? 1 : 0);
        return values;
    }

    @Override
    public void parseCursor(Cursor cursor) {
        rowId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROWID));
        word = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD));
        best = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEST)) == 1;
    }

    @Override
    public String getTable() {
        return "words";
    }
}
