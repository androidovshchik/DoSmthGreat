package rf.androidovshchik.dosmthgreat.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.snatik.storage.Storage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class AppUtil {

    @SuppressWarnings("all")
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @NonNull
    public static String getName(Context context) {
        return getLabel(context).split("From")[0];
    }

    @NonNull
    public static Date getDateX(Context context) {
        String label = getLabel(context);
        if (!label.contains("From")) {
            return new Date();
        }
        try {
            return FORMAT.parse(label.split("From")[1].substring(0, 10));
        } catch (Exception e) {
            Timber.e(e);
            return new Date();
        }
    }

    public static int getYearX(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateX(context));
        return calendar.get(Calendar.YEAR);
    }

    @SuppressWarnings("all")
    public static boolean isAimAlive(Context context) {
        Date stopDate = getStopDate(context);
        return stopDate == null || new Date().before(stopDate);
    }

    @Nullable
    public static Date getStopDate(Context context) {
        String label = getLabel(context);
        if (!label.contains("To")) {
            return null;
        }
        try {
            return FORMAT.parse(label.split("To")[1].substring(0, 10));
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    public static String getPath(Storage storage, Context context) {
        return storage.getExternalStorageDirectory() + File.separator + AppUtil.getName(context);
    }

    public static String getFilePath(Storage storage, Context context, String filename) {
        return getPath(storage, context) + File.separator + filename;
    }

    @NonNull
    public static String getLabel(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }
}
