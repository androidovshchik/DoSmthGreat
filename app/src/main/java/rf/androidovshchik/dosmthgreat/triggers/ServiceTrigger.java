package rf.androidovshchik.dosmthgreat.triggers;

import android.content.Context;
import android.support.annotation.WorkerThread;

import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.services.ActionService;
import rf.androidovshchik.dosmthgreat.services.CommentService;
import rf.androidovshchik.dosmthgreat.services.ResultService;
import rf.androidovshchik.dosmthgreat.services.SoundService;
import rf.androidovshchik.dosmthgreat.services.VoiceService;
import rf.androidovshchik.dosmthgreat.services.WordService;
import rf.androidovshchik.dosmthgreat.utils.ServiceUtil;
import timber.log.Timber;

public class ServiceTrigger extends AlarmTrigger {

	@Override
	@WorkerThread
	public void onHavingTask(Context context, Task task) {
		switch (task.task) {
			case Task.TYPE_WORD:
				ServiceUtil.startServiceRightWay(context, WordService.class, task);
				break;
			case Task.TYPE_VOICE:
				ServiceUtil.startServiceRightWay(context, VoiceService.class, task);
				break;
			case Task.TYPE_SOUND:
				ServiceUtil.startServiceRightWay(context, SoundService.class, task);
				break;
			case Task.TYPE_ACTION:
				ServiceUtil.startServiceRightWay(context, ActionService.class, task);
				break;
			case Task.TYPE_COMMENT:
				ServiceUtil.startServiceRightWay(context, CommentService.class, task);
				break;
			case Task.TYPE_RESULT:
				ServiceUtil.startServiceRightWay(context, ResultService.class, task);
				break;
			default:
				Timber.w("Unknown task: %s", task.task);
				break;
		}
	}
}