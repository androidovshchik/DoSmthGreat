package rf.androidovshchik.dosmthgreat.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.annotations.Nullable;
import rf.androidovshchik.dosmthgreat.data.Preferences;
import timber.log.Timber;

public class Task extends Row implements Parcelable {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("EEE HH:mm", Locale.ENGLISH);

    public static final long MINUTE = 60 * 1000L;
    public static final long HOURS_24 = 24 * 60 * MINUTE;

    public static final String TYPE_WORD = "word";
    public static final String TYPE_VOICE = "voice";
    public static final String TYPE_SOUND = "sound";
    public static final String TYPE_ACTION = "action";
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_RESULT = "result";

    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_TASK = "task";
    private static final String COLUMN_DATA = "data";

    @Nullable
    @SerializedName("day")
    public String day = null;

    @Nullable
    @SerializedName("time")
    public String time;

    @SerializedName("task")
    public String task;

    @SerializedName("data")
    public String data;

    public Task() {}

    public Task(Parcel parcel) {
        this.rowId = parcel.readLong();
        this.day = parcel.readString();
        this.time = parcel.readString();
        this.task = parcel.readString();
        this.data = parcel.readString();
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (rowId != NONE) {
            values.put(COLUMN_ROWID, rowId);
        }
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_TASK, task);
        values.put(COLUMN_DATA, data);
        return values;
    }

    @Override
    public void parseCursor(Cursor cursor) {
        rowId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROWID));
        day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY));
        if (day != null && !Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").contains(day)) {
            day = null;
        }
        time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
        if (time != null && !time.matches("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
            time = null;
        }
        task = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
        data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA));
    }

    @Override
    public String getTable() {
        return "timeline";
    }

    @Override
    public String toString() {
        return "Task{" +
            "day='" + day + '\'' +
            ", time='" + time + '\'' +
            ", task='" + task + '\'' +
            ", data='" + data + '\'' +
            ", rowId=" + rowId +
            '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(rowId);
        dest.writeValue(day);
        dest.writeValue(time);
        dest.writeValue(task);
        dest.writeValue(data);
    }

    @SuppressWarnings("all")
    public static long nextDelayFromNow(Preferences preferences, ArrayList<Task> tasks) {
        Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();
        long minDelay = HOURS_24, delay;
        Calendar calendar = Calendar.getInstance();
        String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
            Locale.ENGLISH);
        try {
            Date now = FORMAT.parse(String.format("%s %02d:%02d", dayName,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            for (Task task: tasks) {
                if (task.time == null) {
                    continue;
                }
                delay = HOURS_24;
                Date time = FORMAT.parse(String.format("%s %s", task.day == null ?
                    dayName : task.day , task.time));
                if (time.after(now)) {
                    delay = time.getTime() - now.getTime();
                } else if (time.before(now)) {
                    delay = HOURS_24 - now.getTime() + time.getTime();
                }
                if (delay < minDelay) {
                    minDelay = delay;
                    preferences.putString(Preferences.EXECUTE_TASK, gson.toJson(task));
                }
            }
        } catch (ParseException e) {
            Timber.e(e);
            return HOURS_24;
        }
        return minDelay;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {

        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
