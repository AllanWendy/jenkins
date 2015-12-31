package com.wecook.common.modules.property;

import android.content.Context;
import android.content.SharedPreferences;

import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 共享索引属性保存
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/3/14
 */
public class SharePreferenceProperties extends BaseModule{

    private static final String FILE_SHARE = "wecook_shared_file";

    private static SharedPreferences mSharePreferences;

    private static SharePreferenceProperties sharePreferenceProperties;

    private Set<OnSharedPreferenceSetListener> listeners = new HashSet<>();

    public static SharePreferenceProperties asInstance() {
        if (sharePreferenceProperties == null) {
            sharePreferenceProperties = (SharePreferenceProperties) ModuleManager.asInstance().getModule(SharePreferenceProperties.class);
        }
        return sharePreferenceProperties;
    }

    @Override
    public void setup(Context context) {
        super.setup(context);
        mSharePreferences = context.getSharedPreferences(FILE_SHARE, Context.MODE_PRIVATE);
    }

    @Override
    public void release() {
        super.release();
    }

    public static void set(String key, Object value) {
        SharedPreferences.Editor editor = mSharePreferences.edit();
        Object old = null;
        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
            old = get(key, 0);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
            old = get(key, "");
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
            old = get(key, 0L);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
            old = get(key, false);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
            old = get(key, 0f);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
            old = get(key, null);
        }
        editor.apply();
        for (OnSharedPreferenceSetListener l : asInstance().listeners) {
            l.onSharedPreferenceSet(mSharePreferences, key, value, old);
        }
    }

    public static <T> T get(String key, T def) {
        if (def instanceof Integer) {
            return (T) Integer.valueOf(mSharePreferences.getInt(key, (Integer) def));
        } else if (def instanceof String) {
            return (T) String.valueOf(mSharePreferences.getString(key, (String) def));
        } else if (def instanceof Long) {
            return (T) Long.valueOf(mSharePreferences.getLong(key, (Long) def));
        } else if (def instanceof Boolean) {
            return (T) Boolean.valueOf(mSharePreferences.getBoolean(key, (Boolean) def));
        } else if (def instanceof Float) {
            return (T) Float.valueOf(mSharePreferences.getFloat(key, (Float) def));
        } else if (def instanceof Set) {
            Set<String> set = mSharePreferences.getStringSet(key, null);
            return set == null ? def : null;
        }
        return def;
    }

    public static void register(OnSharedPreferenceSetListener listener) {
        asInstance().listeners.add(listener);
    }

    public static void unregister(OnSharedPreferenceSetListener listener) {
        asInstance().listeners.remove(listener);
    }

    public interface OnSharedPreferenceSetListener {
        void onSharedPreferenceSet(SharedPreferences sharedPreferences, String key, Object value, Object old);
    }
}
