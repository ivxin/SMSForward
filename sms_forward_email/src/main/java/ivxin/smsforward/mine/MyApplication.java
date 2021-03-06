package ivxin.smsforward.mine;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MyApplication extends Application {
    public static ArrayList<Activity> activityArrayList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initBugly();
    }

    private void initBugly() {
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "41f9b0a0b0", true, strategy);
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

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
