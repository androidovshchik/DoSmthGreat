package rf.androidovshchik.dosmthgreat.services;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;
import timber.log.Timber;

public class SoundService extends ForegroundService {

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private static boolean audioFocusRequested = false;

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = (int focusChange) -> {
        Timber.d("OnAudioFocusChange %d", focusChange);
        if (focusChange < AudioManager.AUDIOFOCUS_NONE) {
            stopPlay("Lost audio focus", null);
        }
    };

    public SimpleExoPlayer exoPlayer;
    private FileDataSource fileDataSource;

    public boolean playOneTime = true;

    private Player.EventListener exoPlayerListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {}

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Timber.d("onLoadingChanged %b", isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                    Timber.d("onPlayerStateChanged %b STATE_IDLE", playWhenReady);
                    break;
                case Player.STATE_BUFFERING:
                    Timber.d("onPlayerStateChanged %b STATE_BUFFERING", playWhenReady);
                    break;
                case Player.STATE_READY:
                    Timber.d("onPlayerStateChanged %b STATE_READY", playWhenReady);
                    break;
                case Player.STATE_ENDED:
                    Timber.d("onPlayerStateChanged %b STATE_ENDED", playWhenReady);
                    if (playWhenReady) {
                        stopPlay();
                    }
                    break;
                default:
                    Timber.d("onPlayerStateChanged %b %d", playWhenReady, playbackState);
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {}

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            stopPlay("Play error was occurred", e);
        }

        @Override
        public void onPositionDiscontinuity() {
            Timber.d("onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .build();
        }
        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getApplicationContext()),
            new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(exoPlayerListener);
        fileDataSource = new FileDataSource();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra(ForegroundService.EXTRA_DATA);
        disposable.add(Observable.fromCallable(() -> {
            if (TextUtils.isEmpty(data)) {
                List<File> files = storage.getFiles(AppUtil.getPath(storage, getApplicationContext()),
                    "^(?!_).*.mp3$");
                if (files.size() > 1) {
                    startPlayFile(files.get(random.nextInt(files.size() - 1)).getPath());
                } else if (files.size() == 1) {
                    // exception otherwise with random
                    startPlayFile(files.get(0).getPath());
                } else {
                    stopWork("No mp3 files found", null);
                }
            } else {
                String filePath = AppUtil.getFilePath(storage, getApplicationContext(), data);
                if (storage.isFileExist(filePath)) {
                    startPlayFile(filePath);
                } else {
                    stopWork("Audio file not found", null);
                }
            }
            return true;
        }).subscribeOn(Schedulers.io())
            .subscribe());
        return START_NOT_STICKY;
    }

    public void startPlayFile(String path) {
        try {
            fileDataSource.open(new DataSpec(Uri.fromFile(storage.getFile(path))));
        } catch (FileDataSource.FileDataSourceException e) {
            stopWork("Failed to open audio file", e);
            return;
        }
        exoPlayer.prepare(new ExtractorMediaSource(fileDataSource.getUri(), () -> fileDataSource,
            new DefaultExtractorsFactory(), null, null));
        if (!audioFocusRequested) {
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            }
            audioFocusRequested = true;
            Timber.d("Focus request result is %d", result);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                stopPlay("Focus request is failed", null);
                return;
            }
        }
        exoPlayer.setPlayWhenReady(true);
    }

    private void stopPlay() {
        stopPlay(null, null);
    }

    private void stopPlay(@Nullable String message, @Nullable Exception e) {
        if (audioFocusRequested) {
            audioFocusRequested = false;
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                result = audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
            Timber.d("Abandon request result is %d", result);
        }
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.stop();
        if (playOneTime) {
            stopWork(message, e);
        } else {
            printMessages(message, e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exoPlayer.removeListener(exoPlayerListener);
        exoPlayer.release();
    }
}
