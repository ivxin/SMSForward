package ivxin.sms_forward_ding_talk_bot.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class Constants {
    public static final String START_KEY = "START_KEY";
    public static final String TOKEN_KEY = "TOKEN_KEY";
    public static final String LAST_FORWARD_KEY = "LAST_FORWARD_KEY";
    public static final String REJECT_INCOMING_KEY = "REJECT_INCOMING_KEY";
    public static final String KEEP_SCREEN_ON_KEY = "KEEP_SCREEN_ON_KEY";


    private static final String SP_FILE_NAME = "config.xml";
    public static final String DING_TALK_BOT_TOKEN = "36a3d3d413b7e3be0a399626a69c77764e80560cb80954aee0352ee33f25d906";
    public static boolean HAVE_PERMISSION = false;
    public static long battery_low_warning_last_time;
    public static boolean isRinging;

    public static final String DEVICE_NAME = Build.BRAND + " " + Build.MODEL;

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
