package rf.androidovshchik.dosmthgreat.services;

import android.content.Intent;

import com.squareup.sqlbrite3.BriteDatabase;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.data.DbCallback;
import rf.androidovshchik.dosmthgreat.data.DbManager;
import rf.androidovshchik.dosmthgreat.data.Preferences;
import rf.androidovshchik.dosmthgreat.models.Action;
import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.models.Word;
import rf.androidovshchik.dosmthgreat.utils.AlarmUtil;

public class UpgradeService extends ForegroundService {

    public DbManager tempManager = new DbManager();

    @Override
    public void onCreate() {
        super.onCreate();
        if (!manager.openDb(getApplicationContext(), DbCallback.BASE_NAME)) {
            stopWork("Cannot open base db", null);
            return;
        }
        if (!tempManager.openDb(getApplicationContext(), DbCallback.TEMP_NAME)) {
            stopWork("Cannot open temp db", null);
        }
    }

    @Override
    @SuppressWarnings("all")
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (manager.isDbClosed() || tempManager.isDbClosed()) {
            // stop is already called
            return START_NOT_STICKY;
        }
        disposable.add(Observable.fromCallable(() -> {
            ArrayList<Action> actions = new ArrayList<>();
            ArrayList<Task> tasks = new ArrayList<>();
            ArrayList<Word> words = new ArrayList<>();
            BriteDatabase.Transaction tempTransaction = tempManager.db.newTransaction();
            try {
                actions.addAll(Action.getRows(tempManager.db.query("SELECT rowid, * FROM actions"), Action.class));
                tasks.addAll(Task.getRows(tempManager.db.query("SELECT rowid, * FROM timeline"), Task.class));
                words.addAll(Word.getRows(tempManager.db.query("SELECT rowid, * FROM words"), Word.class));
                tempTransaction.markSuccessful();
            } catch (Exception e) {
                stopWork("Problems with temp db transaction", e);
                return false;
            } finally {
                tempTransaction.end();
            }
            BriteDatabase.Transaction transaction = manager.db.newTransaction();
            try {
                manager.clearTable("actions");
                for (Action action: actions) {
                    manager.insertRow(action);
                }
                manager.clearTable("timeline");
                for (Task task: tasks) {
                    manager.insertRow(task);
                }
                manager.clearTable("words");
                for (Word word: words) {
                    manager.insertRow(word);
                }
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            AlarmUtil.next(getApplicationContext(), Task.nextDelayFromNow(new Preferences(getApplicationContext()),
                tasks), UpgradeService.class);
            stopWork();
            return true;
        }).subscribeOn(Schedulers.io())
            .subscribe());
        return START_NOT_STICKY;
    }
}