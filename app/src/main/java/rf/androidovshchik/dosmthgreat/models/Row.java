package rf.androidovshchik.dosmthgreat.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public abstract class Row {

	public static final String COLUMN_ROWID = "rowid";

	public static final long NONE = 0L;

	@SerializedName("rowId")
	public long rowId = NONE;

	public abstract ContentValues toContentValues();

	public abstract void parseCursor(Cursor cursor);

	public abstract String getTable();

	public static <T extends Row> ArrayList<T> getRows(Cursor cursor, Class<T> rowClass) throws Exception {
		ArrayList<T> rows = new ArrayList<>();
		if (cursor == null) {
			return rows;
		}
		try {
			while (cursor.moveToNext()) {
				T row = rowClass.newInstance();
				row.parseCursor(cursor);
				rows.add(row);
			}
		} finally {
			cursor.close();
		}
		return rows;
	}
}
