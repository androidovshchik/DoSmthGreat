package rf.androidovshchik.dosmthgreat.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class DbCallback extends SupportSQLiteOpenHelper.Callback {

    private static final String DB_PATH_SUFFIX = "/databases/";

    private static final int DB_VERSION = 1;

    public static final String BASE_NAME = "base.db";
    public static final String TEMP_NAME = "temp.db";

    public DbCallback() {
        super(DB_VERSION);
    }

    @Override
    public void onCreate(SupportSQLiteDatabase db) {}

    @Override
    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {}

    @SuppressWarnings("all")
    public boolean openDb(Context context, String externalPath, String internalName) {
        File internalDb = context.getDatabasePath(internalName);
        if (!internalDb.exists() || internalName.equals(TEMP_NAME) && internalDb.delete()) {
            try {
                Timber.d("Coping database from " + externalPath);
                copyDb(context, externalPath, internalName);
            } catch (IOException e) {
                Timber.e(e);
                return false;
            }
        } else {
            Timber.d("Missing copy database from " + externalPath);
        }
        return true;
    }

    private void copyDb(Context context, String externalPath, String internalName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(externalPath);
        File dbFolder = new File(context.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!dbFolder.exists() && !dbFolder.mkdirs()) {
            Timber.w("Unable to create database path");
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(dbFolder.getPath() + internalName);
        try {
            int length;
            byte[] buffer = new byte[1024];
            while ((length = fileInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        } finally {
            fileOutputStream.flush();
            fileOutputStream.close();
            fileInputStream.close();
        }
    }
}