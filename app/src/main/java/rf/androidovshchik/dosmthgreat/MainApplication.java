package rf.androidovshchik.dosmthgreat;

import android.app.Application;
import android.content.Context;

import com.squareup.sqlbrite3.BriteDatabase;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.data.DbManager;
import rf.androidovshchik.dosmthgreat.data.Preferences;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.utils.AlarmUtil;
import timber.log.Timber;

@ReportsCrashes(mailTo = "vladkalyuzhnyu@gmail.com",
    customReportContent = {
        ReportField.APP_VERSION_CODE,
        ReportField.APP_VERSION_NAME,
        ReportField.ANDROID_VERSION,
        ReportField.PHONE_MODEL,
        ReportField.BRAND,
        ReportField.PRODUCT,
        ReportField.USER_COMMENT,
        ReportField.USER_APP_START_DATE,
        ReportField.USER_CRASH_DATE,
        ReportField.SHARED_PREFERENCES,
        ReportField.STACK_TRACE,
        ReportField.LOGCAT
    },
    mode = ReportingInteractionMode.DIALOG,
    resDialogText = R.string.error_crash,
    resDialogCommentPrompt = R.string.error_comment,
    resDialogTheme = R.style.AppTheme_Dialog)
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (ACRA.isACRASenderServiceProcess()) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            ACRA.init(this);
        }
        StethoTool.init(getApplicationContext());
    }

    public static void setupNextAlarm(Context context, Class<?> clss) {
        Observable.fromCallable(() -> {
            DbManager manager = new DbManager();
            if (!manager.openDb(context, DbCallback.BASE_NAME)) {
                Timber.w("Cannot handle open db on reboot");
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
            AlarmUtil.next(context, Task.nextDelayFromNow(new Preferences(context), tasks), clss);
            return true;
        }).subscribeOn(Schedulers.io())
            .subscribe();
    }
}
