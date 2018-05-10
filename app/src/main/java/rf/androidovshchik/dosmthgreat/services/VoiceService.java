package rf.androidovshchik.dosmthgreat.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.R;
import timber.log.Timber;

public class VoiceService extends SoundService {

    public static final int ID_VOICE = 103;

    private static final String STOP_VOICE = "DO_STOP_VOICE";

    private TextToSpeech textToSpeech;

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Cancel broadcast received");
            stopWork();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(STOP_VOICE);
        registerReceiver(cancelReceiver, filter);
        // important expression
        playOneTime = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra(ForegroundService.EXTRA_DATA);
        if (TextUtils.isEmpty(data)) {
            stopWork("Empty text to speech", null);
            return START_NOT_STICKY;
        }
        textToSpeech = new TextToSpeech(getApplicationContext(), (int status) -> {
            // UI thread here
            Timber.d("TextToSpeech init status is %d", status);
            if (status < TextToSpeech.SUCCESS) {
                stopWork("Failed to init text to speech", null);
                return;
            }
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onDone(String utteranceId) {
                    Timber.d("onDone %s", utteranceId);
                    onDoneSynthesis();
                }

                @Override
                public void onError(String utteranceId) {
                    Timber.e("onError %s", utteranceId);
                    String message = "Cannot convert text to speech";
                    stopWork(message, new Exception(message));
                }

                @Override
                public void onStart(String utteranceId) {}
            });
            disposable.add(Observable.fromCallable(() -> {
                afterInitTTS(data);
                return true;
            }).subscribeOn(Schedulers.io())
                .subscribe());
        });
        return START_NOT_STICKY;
    }

    @WorkerThread
    @SuppressWarnings("all")
    public void afterInitTTS(String data) {
        deleteTempFile();
        int result = synthesizeToTempFile(data);
        Timber.d("Synthesizing to file result is %d", result);
        if (result < TextToSpeech.SUCCESS) {
            stopWork("Failed to synthesize text to file", null);
        }
    }

    @WorkerThread
    public void onDoneSynthesis() {
        showStopNotification();
        disposable.add(Observable.interval(0, 1, TimeUnit.MINUTES)
            .subscribe((Long value) -> {
                if (!exoPlayer.getPlayWhenReady()) {
                    startPlayFile(getTempFilePath());
                }
            }));
    }

    public int synthesizeToTempFile(String text) {
        HashMap<String, String> render = new HashMap<>();
        render.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getString(R.string.slogan));
        return textToSpeech.synthesizeToFile(text, render, getTempFilePath());
    }

    public void deleteTempFile() {
        String tempPath = getTempFilePath();
        if (storage.isFileExist(tempPath)) {
            storage.deleteFile(tempPath);
        }
    }

    public String getTempFilePath() {
        return storage.getInternalFilesDirectory() + File.separator + "temp.wav";
    }

    public void showStopNotification() {
        notificationManager.notify(ID_VOICE, getDefaultBuilder(false)
            .setSmallIcon(R.drawable.ic_hearing_white_24dp)
            .setContentText(getString(R.string.tap_to_stop))
            .setContentIntent(PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent(STOP_VOICE), 0))
            .setOngoing(true)
            .build());
    }

    @Override
    public void stopWork(@Nullable String message, @Nullable Exception e) {
        notificationManager.cancel(ID_VOICE);
        super.stopWork(message, e);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cancelReceiver);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
