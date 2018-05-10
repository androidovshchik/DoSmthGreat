package rf.androidovshchik.dosmthgreat.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Random;

import rf.androidovshchik.dosmthgreat.BuildConfig;
import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.activities.MainActivity;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.models.Action;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;

public class ActionService extends ForegroundService {

    public static final int ID_ACTION = 100;

    private boolean firstTime = true;

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
            Random random = new Random();
            if (random.nextInt(4) > 0) {
                selectRandomAction();
            } else {
                selectRandomFile();
            }
        } else {
            String[] parts = data.split("/*/");
            if (parts.length > 1) {
                if (doesFileExist(parts[0])) {
                    showNotification(parts[1], parts[0]);
                } else if (doesFileExist(parts[1])) {
                    showNotification(parts[0], parts[1]);
                } else {
                    showNotification(data.replaceAll("/*/", " "), null);
                }
            } else {
                if (doesFileExist(data)) {
                    showNotification(null, data);
                } else {
                    showNotification(data, null);
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void selectRandomAction() {
        if (manager.isDbClosed()) {
            // stop is already called
            return;
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM actions ORDER BY RANDOM() LIMIT 1")
            .subscribe((Cursor cursor) -> {
                try {
                    if (cursor.moveToFirst()) {
                        Action action = new Action();
                        action.parseCursor(cursor);
                        if (!TextUtils.isEmpty(action.filename) && doesFileExist(action.filename)) {
                            showNotification(action.description, action.filename);
                        } else {
                            showNotification(action.description, null);
                        }
                    } else if (firstTime) {
                        firstTime = false;
                        selectRandomFile();
                    } else {
                        stopWork("No action files found and no actions in db", null);
                    }
                } finally {
                    cursor.close();
                }
            }));
    }

    private void selectRandomFile() {
        List<File> files = storage.getFiles(AppUtil.getPath(storage, getApplicationContext()),
            "^(?!_|data" + BuildConfig.DB_VERSION + ".db|notification.ogg).*");
        if (files.size() > 1) {
            showNotification(null, files.get(random.nextInt(files.size() - 1)).getName());
        } else if (files.size() == 1) {
            // exception otherwise with random
            showNotification(null, files.get(0).getName());
        } else if (firstTime) {
            // no action files found first time
            firstTime = false;
            selectRandomAction();
        } else {
            stopWork("No action files found and no actions in db", null);
        }
    }

    public boolean doesFileExist(String filename) {
        return storage.isFileExist(AppUtil.getFilePath(storage, getApplicationContext(), filename));
    }

    public void showNotification(@Nullable String text, @Nullable String filename) {
        if (filename == null && text == null) {
            stopWork("No file or description was specified", null);
            return;
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.KEY_TYPE, Task.TYPE_ACTION);
        if (filename != null) {
            intent.putExtra(MainActivity.KEY_FILENAME, filename);
        }
        NotificationCompat.Builder builder = getDefaultBuilder(true);
        if (text == null) {
            builder.setContentText(filename);
        } else {
            intent.putExtra(MainActivity.KEY_WORD, text);
            if (filename != null) {
                builder.setContentTitle(filename);
            }
            builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(text));
        }
        notificationManager.notify(ID_ACTION, builder.setSmallIcon(R.drawable.ic_directions_run_white_24dp)
            .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, 0))
            .setAutoCancel(true)
            .build());
        stopWork();
    }
}
