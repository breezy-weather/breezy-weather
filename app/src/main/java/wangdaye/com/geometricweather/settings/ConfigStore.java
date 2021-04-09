package wangdaye.com.geometricweather.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

public class ConfigStore {

    private final SharedPreferences mPreferences;

    private ConfigStore(SharedPreferences sp) {
        mPreferences = sp;
    }

    public static ConfigStore getInstance(Context context) {
        return new ConfigStore(
                PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static ConfigStore getInstance(Context context, String name) {
        return new ConfigStore(
                context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    public Map<String, ?> getAll() {
        return mPreferences.getAll();
    }

    @Nullable
    public String getString(String key, @Nullable String defValue) {
        return mPreferences.getString(key, defValue);
    }

    @Nullable
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return mPreferences.getStringSet(key, defValues);
    }

    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return mPreferences.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return mPreferences.getFloat(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    public boolean contains(String key) {
        return mPreferences.contains(key);
    }

    public Editor edit() {
        return new Editor(mPreferences);
    }

    public static class Editor {

        final SharedPreferences.Editor mEditor;

        @SuppressLint("CommitPrefEdits")
        Editor(SharedPreferences sp) {
            mEditor = sp.edit();
        }

        public Editor putString(String key, @Nullable String value) {
            mEditor.putString(key, value);
            return this;
        }

        public Editor putStringSet(String key, @Nullable Set<String> values) {
            mEditor.putStringSet(key, values);
            return this;
        }

        public Editor putInt(String key, int value) {
            mEditor.putInt(key, value);
            return this;
        }

        public Editor putLong(String key, long value) {
            mEditor.putLong(key, value);
            return this;
        }

        public Editor putFloat(String key, float value) {
            mEditor.putFloat(key, value);
            return this;
        }

        public Editor putBoolean(String key, boolean value) {
            mEditor.putBoolean(key, value);
            return this;
        }

        public Editor remove(String key) {
            mEditor.remove(key);
            return this;
        }

        public Editor clear() {
            mEditor.clear();
            return this;
        }

        public boolean commit() {
            return mEditor.commit();
        }

        public void apply() {
            mEditor.apply();
        }
    }
}
