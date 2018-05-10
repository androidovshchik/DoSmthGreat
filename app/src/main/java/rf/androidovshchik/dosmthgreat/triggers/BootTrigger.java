package rf.androidovshchik.dosmthgreat.triggers;

import android.content.Context;
import android.support.annotation.WorkerThread;

import rf.androidovshchik.dosmthgreat.models.Task;

public class BootTrigger extends AlarmTrigger {

    @Override
    @WorkerThread
    public void onHavingTask(Context context, Task task) {}
}
