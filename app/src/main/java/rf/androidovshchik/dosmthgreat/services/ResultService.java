package rf.androidovshchik.dosmthgreat.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.activities.MainActivity;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.models.Record;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;

public class ResultService extends ForegroundService {

    public static final int ID_RESULT = 102;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            stopWork("Cannot open base db", null);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (manager.isDbClosed()) {
            // stop is already called
            return START_NOT_STICKY;
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM records")
            .subscribe((Cursor cursor) -> {
                int countActions = 0, countActionDays = 0, allDays = (int) TimeUnit.MILLISECONDS
                    .toDays(Calendar.getInstance().getTimeInMillis() -
                        AppUtil.getDateX(getApplicationContext()).getTime());
                try {
                    while (cursor.moveToNext()) {
                        Record record = new Record();
                        record.parseCursor(cursor);
                        if (record.actions > 0) {
                            countActions += record.actions;
                            countActionDays++;
                        }
                    }
                } finally {
                    cursor.close();
                }
                Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentMain.putExtra(MainActivity.KEY_TYPE, Task.TYPE_RESULT);
                notificationManager.notify(ID_RESULT, getDefaultBuilder(true)
                    .setSmallIcon(R.drawable.ic_event_note_white_24dp)
                    .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(getString(R.string.result_action_days, countActionDays))
                        .addLine(getString(R.string.result_all_days, allDays))
                        .addLine(getString(R.string.result_actions, countActions))
                        .addLine(getString(R.string.result_conclusion)))
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        0, intentMain, 0))
                    .setAutoCancel(true)
                    .build());
                stopWork();
            }));
        return START_NOT_STICKY;
    }
}

