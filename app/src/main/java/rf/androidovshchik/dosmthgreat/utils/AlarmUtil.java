package rf.androidovshchik.dosmthgreat.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.sqlbrite3.BriteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.data.DbManager;
import rf.androidovshchik.dosmthgreat.data.Preferences;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.triggers.ServiceTrigger;
import timber.log.Timber;

public class AlarmUtil {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

    public static final long MINUTE = 60 * 1000L;

    public static final long HOURS_24 = 24 * 60 * MINUTE;

    public static final long MAX_DELAY = 7 * HOURS_24;

    public static Observable<Boolean> setup(Context context, Class<?> clss) {
        return Observable.fromCallable(() -> {
            DbManager manager = new DbManager();
            if (!manager.openDb(context, DbCallback.BASE_NAME)) {
                Timber.w("Cannot handle open db");
                return false;
            }
            ArrayList<Task> tasks = new ArrayList<>();
            BriteDatabase.Transaction transaction = manager.db.newTransaction();
            try {
                tasks.addAll(Task.getRows(manager.db.query("SELECT rowid, * FROM timeline"),
                    Task.class));
                transaction.markSuccessful();
            } catch (Exception e) {
                Timber.e(e);
                return false;
            } finally {
                transaction.end();
            }
            manager.closeDb();
            next(context, tasks, clss);
            return true;
        }).subscribeOn(Schedulers.io());
    }

    @SuppressWarnings("all")
    public static void next(Context context, ArrayList<Task> tasks, Class<?> clss) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
            new Intent(context, ServiceTrigger.class), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        long interval = delay(new Preferences(context), tasks, Calendar.getInstance());
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval, pendingIntent);
        Timber.d("New alarm with delay in " + interval + " milliseconds from class " +
            clss.getSimpleName());
    }

    public static long delay(@Nullable Preferences preferences, ArrayList<Task> tasks, Calendar calendar) {
        Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();
        long minDelay = HOURS_24, delay;
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
        Date now = fixedDate(String.format(Locale.ENGLISH, "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)), dayOfWeek);
        if (now == null) {
            return minDelay;
        }
        for (Task task: tasks) {
            Date time = fixedDate(task.time, task.day != null ? task.day : dayOfWeek);
            if (time == null) {
                continue;
            }
            delay = MAX_DELAY;
            if (time.after(now)) {
                delay = time.getTime() - now.getTime();
            } else if (time.before(now)) {
                if (task.day != null) {
                    delay = MAX_DELAY - now.getTime() + time.getTime();
                } else {
                    // now and time with equals offset => offset will be zero
                    delay = HOURS_24 - now.getTime() + time.getTime();
                }
            }
            if (delay < minDelay) {
                minDelay = delay;
                // only for test purpose null case
                if (preferences != null) {
                    preferences.putString(Preferences.EXECUTE_TASK, gson.toJson(task));
                }
            }
        }
        return minDelay;
    }

    @Nullable
    public static Date fixedDate(@Nullable String time, @NonNull String dayOfWeek) {
        if (time == null) {
            return null;
        }
        try {
            Date date = AlarmUtil.FORMAT.parse(time);
            date.setTime(date.getTime() + 3 * 60 * AlarmUtil.MINUTE + offset(dayOfWeek));
            return date;
        } catch (ParseException e) {
            Timber.e(e);
        }
        return null;
    }

    private static long offset(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Mon":
                return 0L;
            case "Tue":
                return HOURS_24;
            case "Wed":
                return 2 * HOURS_24;
            case "Thu":
                return 3 * HOURS_24;
            case "Fri":
                return 4 * HOURS_24;
            case "Sat":
                return 5 * HOURS_24;
            case "Sun":
                return 6 * HOURS_24;
        }
        return 0L;
    }
}
