package ivxin.sms_forward_ding_talk_bot;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class MyApplication extends Application {
    public static ArrayList<Activity> activityArrayList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }

    public static void finishAllActivities() {
        for (Activity activity : activityArrayList) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }


    public static void removeActivity(Activity activity) {
        activityArrayList.remove(activity);
    }


    public static void addActivity(Activity activity) {
        activityArrayList.add(activity);
    }


    /**
     * 重启应用
     *
     * @param context context
     */
    public static void restartApplication(Context context) {
        restartApplication(context, null);
    }

    /**
     * 重启应用
     *
     * @param context context
     */
    public static void restartApplication(Context context, Bundle params) {
        //重启
        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        if (params != null) {
            intent.putExtras(params);
        }
        PendingIntent restartIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); //1秒钟后重启应用
        // 退出程序
        MyApplication.finishAllActivities();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
