package rf.androidovshchik.dosmthgreat.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;

import com.snatik.storage.Storage;

import java.util.Random;

import io.reactivex.disposables.CompositeDisposable;
import rf.androidovshchik.dosmthgreat.BuildConfig;
import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.data.DbManager;
import rf.androidovshchik.dosmthgreat.triggers.ToastTrigger;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;
import timber.log.Timber;

public abstract class ForegroundService extends Service {

    public static final String EXTRA_DATA = "data";

    private static final String ID_NOISY_CHANNEL = "do_noisy";
    private static final String ID_QUITE_CHANNEL = "do_quite";

    private static final int ID_FOREGROUND_DEFAULT = 1;
    private static final int ID_FOREGROUND_ACTION = 10;
    private static final int ID_FOREGROUND_COMMENT = 11;
    private static final int ID_FOREGROUND_RESULT = 12;
    private static final int ID_FOREGROUND_SOUND = 13;
    private static final int ID_FOREGROUND_UPGRADE = 14;
    private static final int ID_FOREGROUND_VOICE = 15;
    private static final int ID_FOREGROUND_WORD = 16;

    private PowerManager.WakeLock wakeLock;

    public NotificationManager notificationManager;

    public CompositeDisposable disposable = new CompositeDisposable();

    public DbManager manager = new DbManager();

    public Random random = new Random();

    public Storage storage;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("all")
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
            AppUtil.getName(getApplicationContext()));
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(ID_QUITE_CHANNEL,
                AppUtil.getName(getApplicationContext()), NotificationManager.IMPORTANCE_LOW));
            NotificationChannel channel = new NotificationChannel(ID_NOISY_CHANNEL,
                getString(R.string.slogan), NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.GREEN);
            channel.setVibrationPattern(new long[] {1000, 1000});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        startServiceForeground();
        storage = new Storage(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @SuppressWarnings("all")
    private void startServiceForeground() {
        int id = ID_FOREGROUND_DEFAULT;
        int icon = R.drawable.ic_today_white_24dp;
        String title = AppUtil.getName(getApplicationContext());
        if (getClass().equals(ActionService.class)) {
            id = ID_FOREGROUND_ACTION;
            title = getString(R.string.task_action);
        } else if (getClass().equals(CommentService.class)) {
            id = ID_FOREGROUND_COMMENT;
            title = getString(R.string.task_comment);
        }  else if (getClass().equals(ResultService.class)) {
            id = ID_FOREGROUND_RESULT;
            title = getString(R.string.task_result);
        } else if (getClass().equals(SoundService.class)) {
            id = ID_FOREGROUND_SOUND;
            icon = R.drawable.ic_music_note_white_24dp;
            title = getString(R.string.task_sound);
        } else if (getClass().equals(UpgradeService.class)) {
            id = ID_FOREGROUND_UPGRADE;
            icon = R.drawable.ic_system_update_white_24dp;
            title = getString(R.string.task_upgrade);
        } else if (getClass().equals(VoiceService.class)) {
            id = ID_FOREGROUND_VOICE;
            icon = R.drawable.ic_mic_white_24dp;
            title = getString(R.string.task_voice);
        } else if (getClass().equals(WordService.class)) {
            id = ID_FOREGROUND_WORD;
            title = getString(R.string.task_word);
        }
        startForeground(id, new NotificationCompat.Builder(getApplicationContext(), ID_QUITE_CHANNEL)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(getString(R.string.slogan))
            .setSound(null)
            .build());
    }

    public NotificationCompat.Builder getDefaultBuilder(boolean isNoise) {
        NotificationCompat.Builder builder =  new NotificationCompat.Builder(getApplicationContext(),
            isNoise ? ID_NOISY_CHANNEL: ID_QUITE_CHANNEL);
        if (isNoise) {
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setLights(Color.GREEN, 1000, 1000);
            builder.setVibrate(new long[] {1000, 1000});
            String path = AppUtil.getFilePath(storage, getApplicationContext(), "notification.ogg");
            if (storage.isFileExist(path)) {
                Uri sound = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID +
                    ".provider", storage.getFile(path));
                // on nexus is working :)
                grantUriPermission("com.android.systemui", sound, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                builder.setSound(sound);
            } else {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        } else {
            builder.setSound(null);
        }
        String title = AppUtil.getName(getApplicationContext());
        if (getClass().equals(ActionService.class)) {
            title = getString(R.string.task_action);
        } else if (getClass().equals(CommentService.class)) {
            title = getString(R.string.task_comment);
        }  else if (getClass().equals(ResultService.class)) {
            title = getString(R.string.task_result);
        } else if (getClass().equals(SoundService.class)) {
            title = getString(R.string.task_sound);
        } else if (getClass().equals(UpgradeService.class)) {
            title = getString(R.string.task_upgrade);
        } else if (getClass().equals(VoiceService.class)) {
            title = getString(R.string.task_voice);
        } else if (getClass().equals(WordService.class)) {
            title = getString(R.string.task_word);
        }
        builder.setContentTitle(title);
        return builder;
    }

    public void showMessage(@NonNull String message) {
        Intent intent = new Intent();
        intent.setAction(ToastTrigger.ACTION);
        intent.putExtra(ToastTrigger.EXTRA_MESSAGE, message);
        sendBroadcast(intent);
    }

    public void printMessages(@Nullable String message, @Nullable Exception e) {
        if (e != null) {
            Timber.e(e);
        }
        if (message != null) {
            if (e != null) {
                Timber.e(message);
            } else {
                Timber.d(message);
            }
            showMessage(message);
        }
    }

    public void stopWork() {
        stopWork(null, null);
    }

    public void stopWork(@Nullable String message, @Nullable Exception e) {
        printMessages(message, e);
        disposable.clear();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        manager.closeDb();
        stopForeground(true);
    }
}