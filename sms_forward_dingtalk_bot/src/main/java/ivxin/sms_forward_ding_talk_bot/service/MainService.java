package ivxin.sms_forward_ding_talk_bot.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.sms_forward_ding_talk_bot.MainActivity;
import ivxin.sms_forward_ding_talk_bot.ProcessConnection;
import ivxin.sms_forward_ding_talk_bot.R;
import ivxin.sms_forward_ding_talk_bot.constants.Constants;
import ivxin.sms_forward_ding_talk_bot.receiver.BatteryReceiver;
import ivxin.sms_forward_ding_talk_bot.receiver.SMSReceiver;
import ivxin.sms_forward_ding_talk_bot.util.DingTalkBotSenderUtil;
import ivxin.sms_forward_ding_talk_bot.util.PhoneNumberJudge;
import ivxin.sms_forward_ding_talk_bot.util.SignalUtil;

public class MainService extends Service {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static final String TAG = MainService.class.getSimpleName();
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private TelephonyManager mTelephonyManager;
    private NotificationManager mNotificationManager;
    private SMSReceiver smsReceiver = new SMSReceiver();
    private BatteryReceiver batteryReceiver = new BatteryReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        SMSReceiver.setOnSMSReceivedListener((sender, content, receiverCard, timestampMillis) -> {
            String message = String.format(Locale.CHINA, "%s\n\n发信人:%s\n接收时间:%s", content, sender, sdf.format(timestampMillis));
            singleThreadExecutor.execute(() -> DingTalkBotSenderUtil.postDingTalk(Constants.spLoad(MainService.this, Constants.TOKEN_KEY, Constants.DING_TALK_BOT_TOKEN),
                    message));
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean started = Constants.spLoadBoolean(this, Constants.START_KEY);
        //绑定建立链接
        Intent mIntent = new Intent(getApplicationContext(), GuardService.class);
        bindService(mIntent, mServiceConnection, Context.BIND_IMPORTANT);
        Notification notification = showOnGoingNotification();
        startForeground(1, notification);
        Log.d(TAG, "onStartCommand: startId:" + startId);

        if (Constants.HAVE_PERMISSION && started) {
            startServiceAgainViaAlarm();
            registerReceivers();
        }

        return START_STICKY;
    }

    @Nullable
    private Notification showOnGoingNotification() {
        if (mNotificationManager == null) {
            return null;
        }
        String channelId = getPackageName();
        String channelName = getString(R.string.app_name);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(channel);
        }
        String contentText = Constants.spLoadBoolean(this, Constants.START_KEY) ? "已启动转发" : "已停止转发";
        mBuilder.setContentTitle(getString(R.string.app_name) + "正在运行")//设置通知栏标题
                .setContentText(contentText) //设置通知栏显示内容
                .setContentIntent(pendingIntent) //设置通知栏点击意图
                //  .setNumber(number) //设置通知集合的数量
                .setTicker(getString(R.string.app_name) + "已启动") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                //                                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.sms_rew);//设置通知小ICON
        Notification notification = mBuilder.build();
        mNotificationManager.notify(1, notification);
        return notification;
    }

    private void hideOnGoingNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    private void registerReceivers() {
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsIntentFilter.setPriority(1000);
        registerReceiver(smsReceiver, smsIntentFilter);

        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryIntentFilter.setPriority(1000);
        registerReceiver(batteryReceiver, batteryIntentFilter);

        BatteryReceiver.removeOnBatteryInfoUpdateListener(onBatteryInfoUpdateListener);
        BatteryReceiver.addOnBatteryInfoUpdateListener(onBatteryInfoUpdateListener);

        if (Constants.HAVE_PERMISSION) {
            mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_NONE);
            mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_SIGNAL_STRENGTHS |
                    SignalUtil.SignalListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_NONE);
        unregisterReceiver(smsReceiver);
        unregisterReceiver(batteryReceiver);
    }

    private SignalUtil.SignalListener mListener = new SignalUtil.SignalListener(this, null, incomingNumber -> {
        if (Constants.spLoadBoolean(this, Constants.START_KEY)) {
            singleThreadExecutor.execute(() -> {
                long callTime = System.currentTimeMillis();
                PhoneNumberJudge netJudge = new PhoneNumberJudge();
                netJudge.judgeNumberFrom360(incomingNumber, (result) -> DingTalkBotSenderUtil.postDingTalk(Constants.spLoad(MainService.this, Constants.TOKEN_KEY, Constants.DING_TALK_BOT_TOKEN),
                        String.format(Locale.CHINA, "[%s] 来电\n查询结果：%s \n呼叫时间:%s", incomingNumber, result, sdf.format(callTime))));
            });
        }
    });

    private BatteryReceiver.OnBatteryInfoUpdateListener onBatteryInfoUpdateListener = (action, status, percent) -> {
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if (percent <= 15) {
            if (System.currentTimeMillis() - Constants.battery_low_warning_last_time >= 10 * 60 * 1000) {
                singleThreadExecutor.execute(() -> DingTalkBotSenderUtil.postDingTalk(Constants.spLoad(MainService.this, Constants.TOKEN_KEY, Constants.DING_TALK_BOT_TOKEN),
                        String.format(Locale.CHINA, "设备:%s 低电量\n充电状态:%s 电量:%d\n提示时间:%s", Constants.DEVICE_NAME, isCharging, percent, sdf.format(System.currentTimeMillis()))));
                Constants.battery_low_warning_last_time = System.currentTimeMillis();
            }
        }
    };

    private void startServiceAgainViaAlarm() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getService(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (30 * 1000),
                    restartIntent);
        }

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "MainService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开链接
            unbindService(mServiceConnection);
            //重新绑定
            Intent intent = new Intent(getApplicationContext(), GuardService.class);
            bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    };
}
