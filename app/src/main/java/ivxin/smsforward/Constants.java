package ivxin.smsforward;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by yaping.wang on 2017/9/18.
 */

public class Constants {
    public static final boolean sdkIsAboveM = Build.VERSION.SDK_INT >= 23;//Build.VERSION_CODES.M			Android 6.0
    public static final String DB_FILE_NAME = "sms_db_name.db";
    public static final String SP_FILE_NAME = "sp_file";

    public static final String NUM_REX_KEY = "numRex";
    public static final String TARGET_KEY = "target";
    public static final String REX_KEY = "rex";
    public static final String STARTED_KEY = "started";

    public static final String SAVE_SMS_KEY = "SAVE_SMS";
    public static final String SAVE_FORWARD_ONLY_KEY = "SAVE_FORWARD_ONLY";

    public static final String PATTERN = "yyyy-MM-dd HH:ss:mm";
    public static final String ACTION = "com.ivxin.smsforward";

    public static boolean HAVE_PERMISSION = false;
    public static boolean started = false;

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
//            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static int getScreenHeightPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenWidthPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

}
