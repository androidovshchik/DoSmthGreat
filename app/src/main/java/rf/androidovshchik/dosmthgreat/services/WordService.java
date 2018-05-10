package rf.androidovshchik.dosmthgreat.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.activities.MainActivity;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.models.Word;

public class WordService extends ForegroundService {

    public static final int ID_WORD = 104;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            stopWork("Cannot open base db", null);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra(ForegroundService.EXTRA_DATA);
        if (TextUtils.isEmpty(data)) {
            if (manager.isDbClosed()) {
                // stop is already called
                return START_NOT_STICKY;
            }
            disposable.add(manager.onSelectTable("SELECT rowid, * FROM words ORDER BY RANDOM() LIMIT 1")
                .subscribe((Cursor cursor) -> {
                    try {
                        if (cursor.moveToFirst()) {
                            Word word = new Word();
                            word.parseCursor(cursor);
                            showNotification(word.word);
                        }
                    } finally {
                        cursor.close();
                    }
                    stopWork();
                }));
        } else {
            showNotification(data);
            stopWork();
        }
        return START_NOT_STICKY;
    }

    private void showNotification(String word) {
        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentMain.putExtra(MainActivity.KEY_TYPE, Task.TYPE_WORD);
        intentMain.putExtra(MainActivity.KEY_WORD, word);
        notificationManager.notify(ID_WORD, getDefaultBuilder(true)
            .setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(word))
            .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                0, intentMain, 0))
            .setAutoCancel(true)
            .build());
    }
}

