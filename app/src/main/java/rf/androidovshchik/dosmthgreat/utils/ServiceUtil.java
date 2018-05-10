package rf.androidovshchik.dosmthgreat.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.services.ForegroundService;
import timber.log.Timber;

public class ServiceUtil {

    @SuppressWarnings("all")
    public static boolean isRunning(Context context, Class<? extends ForegroundService> serviceClass) {
        ActivityManager manager = (ActivityManager)
            context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startServiceRightWay(Context context, Class<? extends ForegroundService> serviceClass,
                                            @Nullable Task task) {
        if (!isRunning(context, serviceClass)) {
            Intent intent = new Intent(context, serviceClass);
            if (task != null) {
                intent.putExtra(ForegroundService.EXTRA_DATA, task.data);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } else {
            Timber.w("Service is already running");
        }
    }
}
