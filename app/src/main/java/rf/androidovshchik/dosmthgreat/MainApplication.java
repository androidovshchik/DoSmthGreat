package rf.androidovshchik.dosmthgreat;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import timber.log.Timber;

@SuppressWarnings("all")
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
            MobileAds.initialize(getApplicationContext(), "ca-app-pub-3898038055741115~9698475056");
        }
        StethoTool.init(getApplicationContext());
    }
}
