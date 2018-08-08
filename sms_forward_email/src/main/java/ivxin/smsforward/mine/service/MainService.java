package ivxin.smsforward.mine.service;

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
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.smsforward.mine.Constants;
import ivxin.smsforward.mine.MainActivity;
import ivxin.smsforward.mine.ProcessConnection;
import ivxin.smsforward.mine.R;
import ivxin.smsforward.mine.entity.CommandEmail;
import ivxin.smsforward.mine.entity.MailEntity;
import ivxin.smsforward.mine.entity.SMSEntity;
import ivxin.smsforward.mine.receiver.BatteryReceiver;
import ivxin.smsforward.mine.receiver.SMSReceiver;
import ivxin.smsforward.mine.utils.MailFetchUtil;
import ivxin.smsforward.mine.utils.MailSenderHelper;
import ivxin.smsforward.mine.utils.PhoneNumberJudge;
import ivxin.smsforward.mine.utils.PingUtil;
import ivxin.smsforward.mine.utils.SignalUtil;
import ivxin.smsforward.mine.utils.SmsSender;

public class MainService extends Service {
    public static final String TAG = MainService.class.getSimpleName();
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private TelephonyManager mTelephonyManager;

    private NotificationManager mNotificationManager;
    private SMSReceiver smsReceiver = new SMSReceiver();
    private BatteryReceiver batteryReceiver = new BatteryReceiver();

    private SignalUtil.SignalListener mListener = new SignalUtil.SignalListener(this, (signalType, gsmSignalStrength) -> Constants.signalType = signalType, incomingNumber -> {
        if (Constants.started) {
            if (Constants.incomingCallMail) {
                PhoneNumberJudge netJudge = new PhoneNumberJudge();
                netJudge.judgeNumberFrom360(incomingNumber, (result) -> {
                    String subject = String.format(Locale.CHINA, "[短信转发]%s 来电", incomingNumber);
                    MailEntity mailEntity = new MailEntity();
                    mailEntity.setReceiver(Constants.receiverEmail);
                    mailEntity.setSubject(subject);
                    mailEntity.setContent("来电查询结果：" + Constants.BR + result);
                    mailEntity.setSendTime(System.currentTimeMillis());
                    MailSenderHelper.sendEmail(mailEntity);
                });
            }
        }
    });

    private BatteryReceiver.OnBatteryInfoUpdateListener onBatteryInfoUpdateListener = (action, status, percent) -> {
        Constants.battery_level = percent;
        if (percent == 15 || percent == 5) {
            Constants.battery_low_warning_send = false;
        }
        if (percent > 30) {
            Constants.battery_low_warning_send = false;
        } else {
            if (!Constants.battery_low_warning_send) {
                Constants.battery_low_warning_send = true;
                MailEntity mailEntity = new MailEntity();
                mailEntity.setSendTime(System.currentTimeMillis());
                mailEntity.setReceiver(Constants.receiverEmail);
                mailEntity.setSubject("低电量提醒：" + Constants.battery_level);
                mailEntity.setContent("低电量：" + Constants.battery_level);
                MailSenderHelper.sendEmail(mailEntity);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        SMSReceiver.setOnSMSReceivedListener((sender, content, receiverCard, receiverCardName, timestampMillis) -> {
            SMSEntity smsEntity = new SMSEntity();
            smsEntity.setContent(content);
            smsEntity.setSender(sender);
            smsEntity.setReceivedTime(timestampMillis);
            smsEntity.setReceiverCard(receiverCard);
            smsEntity.setReceiverCardName(receiverCardName);
            smsEntity.setSendTime(System.currentTimeMillis());
            MailSenderHelper.sendEmail(smsEntity);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceivers();
        Notification notification = showOnGoingNotification();
        startForeground(1, notification);
        //绑定建立链接
        Intent mIntent = new Intent(getApplicationContext(), GuardService.class);
        bindService(mIntent, mServiceConnection, Context.BIND_IMPORTANT);
        if (Constants.started) {
            startServiceAgainViaAlarm();
        }
        Log.d(TAG, "onStartCommand: startId:" + startId);


        if (Constants.remoteSentSms) {
            singleThreadExecutor.execute(new CommandEmailCheckTask());
        }

        return START_STICKY;
    }

    private class CommandEmailCheckTask implements Runnable {

        @Override
        public void run() {
            MailFetchUtil mailFetchUtil = new MailFetchUtil();
            mailFetchUtil.setProperties(Constants.commandMailHost, Constants.commandUsername, Constants.commandPassword);
            CommandEmail commandEmail = mailFetchUtil.fetchIMAPEmail();
            if (commandEmail == null) {
                Looper.prepare();
                Toast.makeText(MainService.this, "收信邮箱配置异常", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return;
            }
            if (!Constants.isLastMail(MainService.this, commandEmail.getMessageId())) {
                Constants.saveLastMail(MainService.this, commandEmail);
                String subject = commandEmail.getSubject();
                if (!TextUtils.isEmpty(subject)) {
                    if (subject.contains("RemoteSmsSend")) {
                        parseCommand(subject);
                    }
                }

            }
        }
    }

    private void parseCommand(String mailSubject) {
        try {
            JSONObject jsonObject = new JSONObject(mailSubject);
            String target = jsonObject.getString("target");
            String smsContent = jsonObject.getString("content");
            String code = jsonObject.getString("code");
            if (Constants.commandCode.equals(code)) {
                SmsSender.send(smsContent, target);
                String subject = "[短信转发]远程短信已发送";
                String content = "短信内容:" + smsContent;

                MailEntity mailEntity = new MailEntity();
                mailEntity.setSubject(subject);
                mailEntity.setContent(content);
                mailEntity.setSendTime(System.currentTimeMillis());
                MailSenderHelper.sendEmail(mailEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String subject = "[短信转发]远程短信发送失败";
            String content = mailSubject + Constants.BR + e.getLocalizedMessage();
            MailEntity mailEntity = new MailEntity();
            mailEntity.setSubject(subject);
            mailEntity.setContent(content);
            mailEntity.setSendTime(System.currentTimeMillis());
            MailSenderHelper.sendEmail(mailEntity);
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

    private Notification showOnGoingNotification() {
        if (mNotificationManager == null) {
            return null;
        }
        String channelId = getPackageName();
        String channelName = getString(R.string.app_name);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(channel);
        }
        String contentText = String.format(Locale.CHINA, getString(Constants.started ? R.string.app_running_tip : R.string.app_stop_tip), Constants.receiverEmail);
        mBuilder.setContentTitle(String.format(Locale.CHINA, getString(R.string.app_is_running), getString(R.string.app_name)))//设置通知栏标题
                .setContentText(contentText) //设置通知栏显示内容
                .setContentIntent(pendingIntent) //设置通知栏点击意图
                //  .setNumber(number) //设置通知集合的数量
                .setTicker(getString(R.string.app_started)) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_MIN) //设置该通知优先级
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.sms_rew);//设置通知小ICON
        Notification notification = mBuilder.build();
        mNotificationManager.notify(1, notification);
        return notification;
    }

    private void hideOnGoingNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }


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
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (Constants.commandCheckTime * 1000),
                    restartIntent);
        }

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory: level:" + level);
        Toast.makeText(this, "TrimMemory! level:" + level, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: ");
        Toast.makeText(this, "LowMemory!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved: rootIntent" + rootIntent.toString());
        Toast.makeText(this, "TaskRemoved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PingUtil.stopPing();
        mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_NONE);
        unregisterReceiver(smsReceiver);
        unregisterReceiver(batteryReceiver);
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