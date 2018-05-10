package rf.androidovshchik.dosmthgreat.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;

import java.util.Date;

import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.activities.MainActivity;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.models.Record;
import rf.androidovshchik.dosmthgreat.models.Task;

public class CommentService extends ForegroundService {

    public static final int ID_COMMENT = 101;

    private String day;

    @Override
    public void onCreate() {
        super.onCreate();
        day = Record.FORMAT.format(new Date());
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
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM records WHERE day = '" +
            day + "' LIMIT 1")
            .subscribe((Cursor cursor) -> {
                try {
                    if (cursor.getCount() <= 0) {
                        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentMain.putExtra(MainActivity.KEY_TYPE, Task.TYPE_COMMENT);
                        intentMain.putExtra(MainActivity.KEY_DAY, day);
                        notificationManager.notify(ID_COMMENT, getDefaultBuilder(true)
                            .setSmallIcon(R.drawable.ic_comment_white_24dp)
                            .setContentText(getString(R.string.comment))
                            .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                                0, intentMain, 0))
                            .setAutoCancel(true)
                            .build());
                    }
                } finally {
                    cursor.close();
                }
                stopWork();
            }));
        return START_NOT_STICKY;
    }
}
