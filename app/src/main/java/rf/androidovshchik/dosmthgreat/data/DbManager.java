package rf.androidovshchik.dosmthgreat.data;

import android.Manifest;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Factory;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.snatik.storage.Storage;
import com.squareup.sqlbrite3.BriteDatabase;
import com.squareup.sqlbrite3.SqlBrite;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import rf.androidovshchik.dosmthgreat.BuildConfig;
import rf.androidovshchik.dosmthgreat.models.Row;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;
import timber.log.Timber;

public class DbManager {

    private static final String TAG = "DbManager";

    public BriteDatabase db;

    public DbManager() {}

    @SuppressWarnings("all")
    public boolean openDb(Context context, String internalName) {
        closeDb();
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            Timber.w("No read permission");
            return false;
        }
        DbCallback dbCallback = new DbCallback();
        Storage storage = new Storage(context);
        String externalPath = AppUtil.getFilePath(storage, context, "data" + BuildConfig.DB_VERSION+ ".db");
        if (!storage.isFileExist(externalPath)) {
            Timber.w("Db file doesn't exist");
            return false;
        }
        if (!dbCallback.openDb(context, externalPath, internalName)) {
            Timber.w("Cannot open db");
            return false;
        }
        Configuration configuration = Configuration.builder(context)
            .name(internalName)
            .callback(dbCallback)
            .build();
        Factory factory = new FrameworkSQLiteOpenHelperFactory();
        SupportSQLiteOpenHelper openHelper = factory.create(configuration);
        db = new SqlBrite.Builder()
            .logger((String message) -> Log.v(TAG, message))
            .build()
            .wrapDatabaseHelper(openHelper, Schedulers.io());
        db.setLoggingEnabled(BuildConfig.DEBUG);
        return true;
    }

    @SuppressWarnings("all")
    public Observable<Cursor> onSelectTable(String sql) {
        return Observable.create((ObservableEmitter<Cursor> emitter) -> {
            if (emitter.isDisposed()) {
                return;
            }
            Cursor cursor = null;
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                cursor = db.query(sql);
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            if (cursor != null) {
                emitter.onNext(cursor);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @SuppressWarnings("all")
    public Observable<Row> onInsertRow(Row row) {
        return Observable.create((ObservableEmitter<Row> emitter) -> {
            if (emitter.isDisposed()) {
                return;
            }
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                row.rowId = insertRow(row);
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            emitter.onNext(row);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @SuppressWarnings("all")
    public long insertRow(Row row) {
        return db.insert(row.getTable(), SQLiteDatabase.CONFLICT_REPLACE, row.toContentValues());
    }

    @SuppressWarnings("all")
    public Observable<Integer> onUpdateRow(Row row) {
        return Observable.create((ObservableEmitter<Integer> emitter) -> {
            if (emitter.isDisposed()) {
                return;
            }
            int result = 0;
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                result = updateRow(row);
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            emitter.onNext(result);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @SuppressWarnings("all")
    public int updateRow(Row row) {
        return db.update(row.getTable(), SQLiteDatabase.CONFLICT_IGNORE, row.toContentValues(),
            "rowid=?", String.valueOf(row.rowId));
    }

    @SuppressWarnings("all")
    public int deleteRow(Row row) {
        return db.delete(row.getTable(), "rowid=?", String.valueOf(row.rowId));
    }

    @SuppressWarnings("all")
    public int clearTable(String table) {
        return db.delete(table, null, null);
    }

    public boolean isDbClosed() {
        return db == null;
    }

    @SuppressWarnings("all")
    public void closeDb() {
        if (!isDbClosed()) {
            db.close();
            db = null;
        }
    }
}
