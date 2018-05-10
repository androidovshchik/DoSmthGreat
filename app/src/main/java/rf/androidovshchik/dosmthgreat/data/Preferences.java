package rf.androidovshchik.dosmthgreat.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    /* Preferences */

    private SharedPreferences preferences;

    public static final String EXECUTE_TASK = "executeTask";
    public static final String ADMOB_COUNT = "admobCount";

    public Preferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @SuppressWarnings("unused")
    public String getString(String name) {
        return preferences.getString(name, "").trim();
    }

    @SuppressWarnings("all")
    public <T> String getString(String name, T def) {
        return preferences.getString(name, toString(def)).trim();
    }

    @SuppressWarnings("unused")
    public boolean getBoolean(String name) {
        return preferences.getBoolean(name, false);
    }

    @SuppressWarnings("all")
    public boolean getBoolean(String name, boolean def) {
        return preferences.getBoolean(name, def);
    }

    @SuppressWarnings("unused")
    public int getInt(String name) {
        return preferences.getInt(name, 0);
    }

    @SuppressWarnings("unused")
    public int getInt(String name, int def) {
        return preferences.getInt(name, def);
    }

    @SuppressWarnings("unused")
    public long getLong(String name) {
        return preferences.getLong(name, 0L);
    }

    @SuppressWarnings("unused")
    public <T> void putString(String name, T value) {
        preferences.edit().putString(name, toString(value)).apply();
    }

    @SuppressWarnings("unused")
    public void putBoolean(String name, boolean value) {
        preferences.edit().putBoolean(name, value).apply();
    }

    @SuppressWarnings("unused")
    public void putInt(String name, int value) {
        preferences.edit().putInt(name, value).apply();
    }

    @SuppressWarnings("unused")
    public void putLong(String name, long value) {
        preferences.edit().putLong(name, value).apply();
    }

    /* Controls functions */

    @SuppressWarnings("all")
    public boolean has(String name) {
        return preferences.contains(name);
    }

    @SuppressWarnings("unused")
    public void clear() {
        preferences.edit().clear().apply();
    }

    @SuppressWarnings("unused")
    public void remove(String name) {
        if (has(name)) {
            preferences.edit().remove(name).apply();
        }
    }

    /* Utils functions */

    @SuppressWarnings("unused")
    private <T> String toString(T value) {
        return String.class.isInstance(value)? ((String) value).trim() : String.valueOf(value);
    }
}
