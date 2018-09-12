package ivxin.incoming_call_query;

import android.content.Context;
import android.content.SharedPreferences;

class Constants {
    public static boolean HAVE_PERMISSION = false;
    public static boolean HAVE_FLOAT_PERMISSION = false;

    private static final String SP_FILE_NAME = "config.xml";
    public static final String KEY_LAST_QUERY = "KEY_LAST_QUERY";
    public static final String KEY_DING_TALK_FORWARD = "KEY_DING_TALK_FORWARD";
    public static final String KEY_DING_TALK_TOKEN = "KEY_DING_TALK_TOKEN";

    public static void spSave(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static void spSave(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    public static String spLoad(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static String spLoad(Context context, String key, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    public static boolean spLoadBoolean(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }
}
