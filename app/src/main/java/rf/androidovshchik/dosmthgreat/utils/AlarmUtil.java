package rf.androidovshchik.dosmthgreat.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import rf.androidovshchik.dosmthgreat.triggers.ServiceTrigger;
import timber.log.Timber;

public class AlarmUtil {

    @SuppressWarnings("all")
    public static void next(Context context, long interval, Class clss) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
            new Intent(context, ServiceTrigger.class), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval, pendingIntent);
        Timber.d("New alarm with delay in " + interval + " milliseconds from class " +
            clss.getSimpleName());
    }
}
