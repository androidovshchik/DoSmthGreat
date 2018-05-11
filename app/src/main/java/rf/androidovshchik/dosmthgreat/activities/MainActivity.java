package rf.androidovshchik.dosmthgreat.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import rf.androidovshchik.dosmthgreat.BuildConfig;
import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.adapters.PagerAdapter;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.data.DbManager;
import rf.androidovshchik.dosmthgreat.data.Preferences;
import rf.androidovshchik.dosmthgreat.models.Record;
import rf.androidovshchik.dosmthgreat.models.Row;
import rf.androidovshchik.dosmthgreat.models.Word;
import rf.androidovshchik.dosmthgreat.services.UpgradeService;
import rf.androidovshchik.dosmthgreat.utils.AlarmUtil;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;
import rf.androidovshchik.dosmthgreat.utils.ServiceUtil;
import rf.androidovshchik.dosmthgreat.utils.ViewUtil;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_TYPE = "type";

    public static final String KEY_WORD = "word";
    public static final String KEY_DAY = "day";
    public static final String KEY_FILENAME = "filename";

    private static final int REQUEST_TTS = 1;

    @BindView(R.id.actionDays)
    CountAnimationTextView actionDays;
    @BindView(R.id.allDays)
    CountAnimationTextView allDays;
    @BindView(R.id.actions)
    CountAnimationTextView actions;

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.actionsBar)
    HorizontalScrollView actionsBar;
    @BindView(R.id.buttonsContainer)
    LinearLayout buttonsContainer;
    @BindView(R.id.zero)
    AppCompatButton zero;

    @BindView(R.id.words)
    TextView words;

    private CompositeDisposable disposable = new CompositeDisposable();

    private PagerAdapter adapter;

    private DbManager manager = new DbManager();

    private Preferences preferences;

    private Storage storage;

    private boolean hasSetupNextAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle(AppUtil.getName(getApplicationContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0f);
        }
        preferences = new Preferences(getApplicationContext());
        storage = new Storage(getApplicationContext());
        adapter = new PagerAdapter(getApplicationContext(), getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                onSelectedPage(true, false, false);
            }
        });
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_DAY)) {
            writeComment(intent.getStringExtra(KEY_DAY));
        } if (intent.hasExtra(KEY_FILENAME)) {
            String path = AppUtil.getFilePath(storage, getApplicationContext(),
                intent.getStringExtra(KEY_FILENAME));
            String extension = path.substring(path.lastIndexOf(".") + 1);
            if (TextUtils.isEmpty(extension)) {
                Timber.w("Invalid file extension");
                return;
            }
            MimeTypeMap mimeType = MimeTypeMap.getSingleton();
            Intent fileIntent = new Intent(Intent.ACTION_VIEW);
            fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fileIntent.setDataAndType(Uri.fromFile(storage.getFile(path)),
                mimeType.getMimeTypeFromExtension(extension));
            startActivity(Intent.createChooser(fileIntent, getString(R.string.app_name)));
        } else {
            // check for TTS data
            Intent intentTTS = new Intent();
            intentTTS.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(intentTTS, REQUEST_TTS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        disposable.clear();
        if (!BuildConfig.DEBUG) {
            int admobCount = preferences.getInt(Preferences.ADMOB_COUNT);
            if (admobCount >= 9 || admobCount < 0) {
                admobCount = 0;
                new Handler().postDelayed(this::showAd, 3000);
            } else {
                admobCount++;
            }
            preferences.putInt(Preferences.ADMOB_COUNT, admobCount);
        }
        buttonsContainer.post(this::scrollButtons);
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(new MultiplePermissionsListener() {

                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        if (manager.isDbClosed() && !manager.openDb(getApplicationContext(),
                            DbCallback.BASE_NAME)) {
                            Timber.w("Cannot handle db after accessing permission");
                            Toast.makeText(getApplicationContext(), "Cannot open " +
                                AppUtil.getFilePath(storage, getApplicationContext(), "data" +
                                    BuildConfig.DB_VERSION + ".db"), Toast.LENGTH_LONG)
                                .show();
                            return;
                        }
                        Intent intent = getIntent();
                        if (intent.hasExtra(KEY_WORD)) {
                            words.setText(Html.fromHtml(intent.getStringExtra(KEY_WORD)));
                            intent.removeExtra(KEY_WORD);
                            setStartFragment();
                        } else {
                            disposable.add(manager.onSelectTable("SELECT rowid, * FROM words WHERE best = 1")
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((Cursor cursor) -> {
                                    StringBuilder best = new StringBuilder();
                                    try {
                                        Word word = new Word();
                                        while (cursor.moveToNext()) {
                                            word.parseCursor(cursor);
                                            best.append(word.word);
                                            if (!cursor.isLast()) {
                                                best.append("<br><br>");
                                            }
                                        }
                                    } finally {
                                        cursor.close();
                                    }
                                    words.setText(Html.fromHtml(best.toString()));
                                    setStartFragment();
                                }));
                        }
                    } else {
                        // what is an aim of this?
                        finish();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                               PermissionToken token) {
                    // what is an aim of this?
                    finish();
                }
            })
            .check();
    }

    private void setStartFragment() {
        int yearX = AppUtil.getYearX(getApplicationContext());
        int yearNow = Calendar.getInstance()
            .get(Calendar.YEAR);
        if (yearNow > yearX) {
            viewPager.setCurrentItem(yearNow - yearX, true);
        } else {
            onSelectedPage(true, true, false);
        }
    }

    public void onSelectedPage(boolean animAllDays, boolean animActions, boolean hideSwipe) {
        if (manager.isDbClosed() && !manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            Timber.w("Cannot handle db on selected page");
            if (hideSwipe) {
                hideAllSwipes();
            }
            return;
        }
        if (animAllDays) {
            allDays.setInterpolator(new AccelerateInterpolator())
                .countAnimation(0, (int) TimeUnit.MILLISECONDS
                    .toDays(Calendar.getInstance().getTimeInMillis() -
                        AppUtil.getDateX(getApplicationContext()).getTime()));
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM records")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((Cursor cursor) -> {
                int countActions = 0, countActionDays = 0;
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
                actionDays.setInterpolator(new AccelerateInterpolator())
                    .countAnimation(0, countActionDays);
                if (animActions) {
                    actions.setInterpolator(new AccelerateInterpolator())
                        .countAnimation(0, countActions);
                }
                if (hideSwipe) {
                    hideAllSwipes();
                }
                if (!hasSetupNextAlarm) {
                    // after all ui setup on start
                    hasSetupNextAlarm = true;
                    AlarmUtil.setupNextAlarm(getApplicationContext(), MainActivity.class);
                }
            }));
    }

    private void hideAllSwipes() {
        for (int f = 0; f < adapter.fragments.size(); f++) {
            if (adapter.fragments.get(f).swipe != null) {
                adapter.fragments.get(f).swipe.setRefreshing(false);
            }
        }
    }

    @OnClick(R.id.zero)
    void onZero(Button button) {
        int count = Integer.parseInt(button.getText().toString());
        if (count == 0) {
            scrollButtons();
            return;
        }
        setActionsCount(Record.FORMAT.format(new Date()), count, "0");
    }

    @OnClick({R.id.plus1, R.id.plus2, R.id.plus3, R.id.plus5, R.id.plus10, R.id.plus20, R.id.plus30, R.id.plus50})
    void onPlus(Button button) {
        int count = Integer.parseInt(button.getText().toString());
        setActionsCount(Record.FORMAT.format(new Date()), count, String.valueOf(-1 * count));
    }

    private void scrollButtons() {
        actionsBar.scrollTo(Math.abs(buttonsContainer.getWidth() -
            ViewUtil.getWindow(getApplicationContext()).x) / 2, 0);
    }

    @SuppressWarnings("all")
    private void setActionsCount(String day, int count, String zeroValue) {
        if (manager.isDbClosed() && !manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            Timber.w("Cannot handle db on set actions count");
            return;
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM records WHERE day = '" + day + "' LIMIT 1")
            .subscribe((Cursor cursor) -> {
                Record record;
                try {
                    if (cursor.moveToFirst()) {
                        record = new Record();
                        record.parseCursor(cursor);
                    } else {
                        record = null;
                    }
                } finally {
                    cursor.close();
                }
                if (record == null) {
                    manager.onInsertRow(new Record(day, count))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((Row row) -> {
                            zero.setText(zeroValue);
                            scrollButtons();
                            onSelectedPage(false, true, false);
                        });
                } else {
                    record.actions += count;
                    if (record.actions < 0) {
                        record.actions = 0;
                    }
                    manager.onUpdateRow(record)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((Integer value) -> {
                            zero.setText(zeroValue);
                            scrollButtons();
                            onSelectedPage(false, true, false);
                        });
                }
            }));
    }

    @OnClick(R.id.fab)
    void onWords() {
        if (manager.isDbClosed() && !manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            Timber.w("Cannot handle db on fab");
            return;
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM words ORDER BY RANDOM() LIMIT 1")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((Cursor cursor) -> {
                try {
                    if (cursor.moveToFirst()) {
                        Word word = new Word();
                        word.parseCursor(cursor);
                        words.setText(Html.fromHtml(word.word));
                    }
                } finally {
                    cursor.close();
                }
            }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        for (int v = 0; v < buttonsContainer.getChildCount(); v++) {
            buttonsContainer.getChildAt(v).setEnabled(true);
        }
        menu.findItem(R.id.menu_comment)
            .setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                ServiceUtil.startServiceRightWay(getApplicationContext(), UpgradeService.class, null);
                return true;
            case R.id.menu_comment:
                writeComment(Record.FORMAT.format(new Date()));
                return true;
            case R.id.menu_star:
                Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/androidovshchik/DoSmthGreat"));
                startActivity(intent);
                return true;
            case R.id.menu_information:
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.menu_information))
                    .setMessage(getString(R.string.information, BuildConfig.VERSION_NAME, BuildConfig.DB_VERSION))
                    .setPositiveButton(getString(android.R.string.ok), null)
                    .create()
                    .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("all")
    private void writeComment(String day) {
        if (manager.isDbClosed() && !manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            Timber.w("Cannot handle db on write comment");
            return;
        }
        disposable.add(manager.onSelectTable("SELECT rowid, * FROM records WHERE day = '" + day + "' LIMIT 1")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((Cursor cursor) -> {
                Record record;
                try {
                    if (cursor.moveToFirst()) {
                        record = new Record();
                        record.parseCursor(cursor);
                    } else {
                        record = null;
                    }
                } finally {
                    cursor.close();
                }
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.comment_date, day))
                    .setView(View.inflate(getApplicationContext(), R.layout.dialog_comment, null))
                    .setNegativeButton(getString(android.R.string.cancel), null)
                    .setPositiveButton(getString(android.R.string.ok), null)
                    .create();
                alertDialog.setOnShowListener((DialogInterface dialogInterface) -> {
                    EditText comment = alertDialog.findViewById(R.id.comment);
                    comment.setText(record == null ? "" : record.comment);
                    comment.setSelection(comment.getText().length());
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener((View view) -> {
                        if (record == null) {
                            String value = comment.getText().toString();
                            if (!TextUtils.isEmpty(value)) {
                                manager.onInsertRow(new Record(day, value))
                                    .subscribe();
                            }
                        } else {
                            record.comment = comment.getText().toString();
                            manager.onUpdateRow(record)
                                .subscribe();
                        }
                        alertDialog.cancel();
                    });
                });
                alertDialog.show();
            }));
    }

    private void showAd() {
        InterstitialAd interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId("ca-app-pub-3898038055741115/5362686364");
        interstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        });
        if (BuildConfig.DEBUG) {
            interstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("BD1C60E379701FB989CE8D2BDBEE9501")
                .addTestDevice("FAA1BA6958CC85BA6B1B0483BE321991")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build());
        } else {
            interstitialAd.loadAd(new AdRequest.Builder()
                .build());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TTS) {
            if (resultCode < TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        manager.closeDb();
    }
}
