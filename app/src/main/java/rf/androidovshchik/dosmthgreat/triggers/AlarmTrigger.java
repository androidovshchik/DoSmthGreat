package rf.androidovshchik.dosmthgreat.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import rf.androidovshchik.dosmthgreat.data.Preferences;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.utils.AlarmUtil;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;
import timber.log.Timber;

public abstract class AlarmTrigger extends BroadcastReceiver {

    @Override
    @SuppressWarnings("all")
    public void onReceive(Context context, Intent intent) {
        if (!AppUtil.isAimAlive(context)) {
            Timber.w("It's a pity");
            return;
        }
        Task task;
        Preferences preferences = new Preferences(context);
        if (preferences.has(Preferences.EXECUTE_TASK)) {
            Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
            try {
                task = gson.fromJson(preferences.getString(Preferences.EXECUTE_TASK), Task.class);
            } catch (JsonSyntaxException e) {
                Timber.e(e);
                AlarmUtil.setup(context, getClass())
                    .subscribe();
                return;
            }
            preferences.remove(Preferences.EXECUTE_TASK);
        } else {
            Timber.w("Not task found on service trigger");
            task = null;
        }
        AlarmUtil.setup(context, getClass())
            .subscribe((Boolean value) -> {
                if (task != null) {
                    onHavingTask(context, task);
                }
            });
    }

    @WorkerThread
    public abstract void onHavingTask(Context context, Task task);
}