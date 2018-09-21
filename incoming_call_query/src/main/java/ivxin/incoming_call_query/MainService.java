package ivxin.incoming_call_query;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.smsforward.lib.service.BaseService;
import ivxin.smsforward.lib.utils.ContactsUtil;
import ivxin.smsforward.lib.utils.DingTalkBotSenderUtil;
import ivxin.smsforward.lib.utils.html2md.HTML2Md;
import ivxin.smsforward.lib.utils.PhoneNumberJudge;
import ivxin.smsforward.lib.utils.SignalUtil;

public class MainService extends BaseService {
    public static final String PHONE_NUMBER_TO_QUERY = "PHONE_NUMBER_TO_QUERY";
    private MyHandler handler;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private TelephonyManager mTelephonyManager;
    private NotificationManager mNotificationManager;
    public WebView wv_result;
    private FloatWindow floatWindow;

    public static final String contentFormat = "<b><big>[%s]来电</big></b><br>%s<br><br>360查询结果:<br>%s<br><br>百度查询结果:<br>%s<br><br>114查询结果:<br>%s<br><br>";
    public static String incomingNumber = "";
    public static String result360 = "查询中";
    public static String result114 = "查询中";
    public static String resultBaidu = "查询中";
    public static long callTime;
    public static byte resultCount = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        initFloatWindow();
        handler = new MyHandler(this);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(PHONE_NUMBER_TO_QUERY)) {
            MainService.incomingNumber = intent.getStringExtra(PHONE_NUMBER_TO_QUERY);
            phoneNumberQuery(MainService.incomingNumber);
        }
        startServiceAgainViaAlarm();
        registerReceivers();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initFloatWindow() {
        View view = View.inflate(this, R.layout.layout_float_window, null);
        wv_result = view.findViewById(R.id.wv_result);
        ImageView iv_hide = view.findViewById(R.id.iv_hide);
        iv_hide.setOnClickListener(v -> floatWindow.hideFloatWindow());
        floatWindow = new FloatWindow(this, view);
        floatWindow.setFloatWindowWidth((int) (FloatWindow.getScreenWidth(this) * 0.8));
        wv_result.setOnTouchListener(floatWindow);
    }

    @Nullable
    @Override
    public Notification showOnGoingNotification(String content) {
        if (mNotificationManager == null) {
            return null;
        }
        String channelId = getPackageName();
        String channelName = getString(R.string.app_name);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, content.length() > 0 ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder.setContentTitle(channelName + "正在运行")//设置通知栏标题
                .setContentText(content) //设置通知栏显示内容
                .setContentIntent(pendingIntent) //设置通知栏点击意图
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(content.length() > 0 ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                //                                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.ic_search_white_24dp);//设置通知小ICON
        Notification notification = mBuilder.build();
        mNotificationManager.notify(1, notification);
        return notification;
    }

    private void registerReceivers() {
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
    }


    private SignalUtil.SignalListener mListener = new SignalUtil.SignalListener(this, null, (state, incomingNumber) -> {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(TAG, "onCallStateChanged:CALL_STATE_IDLE " + incomingNumber);
                reset();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK " + incomingNumber);
                reset();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(TAG, "onCallStateChanged: CALL_STATE_RINGING " + incomingNumber);
                floatWindow.showFloatWindow();
                phoneNumberQuery(incomingNumber);
                break;
        }
    });

    public void phoneNumberQuery(String incomingNumber) {
        callTime = System.currentTimeMillis();
        MainService.incomingNumber = incomingNumber;
        singleThreadExecutor.execute(() -> {
            PhoneNumberJudge netJudge = new PhoneNumberJudge();
            netJudge.judgeNumberFrom360(incomingNumber, result -> {
                        String ad = "安装360手机卫士，骚扰电话无所遁形！";
                        result360 = deleteAd(result, ad);
                        resultCount++;
                        sendMessage(incomingNumber);
                        showOnGoingNotification(result360);
                    }
            );
            netJudge.judgeNumberFromBaidu(incomingNumber, result -> {
                        resultBaidu = result;
                        resultCount++;
                        sendMessage(incomingNumber);
                    }
            );
            netJudge.judgeNumberFrom114(incomingNumber, result -> {
                        String ad1 = "<img class=\"p1\" src=\"http://m.114best.com/images/ico_201609271010.png\" width=\"18\" height=\"18\" alt=\"电话\">";
                        String ad2 = "<img class=\"p1\" style=\"margin: 9px 5px;\" src=\"http://m.114best.com/images/ico_201609271011.png\" width=\"18\" height=\"18\" alt=\"归属地\">";
                        String ad3 = "<img class=\"p1\" src=\"http://m.114best.com/images/ico_201609271000.png\" width=\"18\" height=\"18\" alt=\"标记\">";
                        String ad4 = "<li style=\"text-align:center\"><a href=\"http://go.izd.cn/zdapp\" style=\"color:#369\">下载官方APP，随时随地查询精准信息</a></li>";
                        result114 = deleteAd(result, ad1, ad2, ad3, ad4);
                        resultCount++;
                        sendMessage(incomingNumber);
                    }
            );
        });
    }

    private String deleteAd(String result, String... ads) {
        for (String ad : ads) {
            if (result.contains(ad)) {
                result = result.replaceAll(ad, "");
            }
        }
        return result;
    }

    private void sendMessage(String incomingNumber) {
        String displayName = incomingNumber;
        String name = ContactsUtil.getDisplayNameByNumber(this, incomingNumber);
        if (!TextUtils.isEmpty(name)) {
            displayName = name;
        }
        String content = String.format(contentFormat, displayName, sdf.format(callTime), result360, resultBaidu, result114);

        if (resultCount == 3) {
            resultCount = 0;
            Constants.spSave(MainService.this, Constants.KEY_LAST_QUERY, content);
            if (Constants.spLoadBoolean(MainService.this, Constants.KEY_DING_TALK_FORWARD)) {
                try {
                    DingTalkBotSenderUtil.postDingTalkMarkDownMessage(Constants.spLoad(MainService.this, Constants.KEY_DING_TALK_TOKEN),
                            incomingNumber + "查询结果", HTML2Md.convertHtml(content, "utf-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Message msg = handler.obtainMessage();
        msg.obj = content;
        handler.sendMessage(msg);
    }

    public void reset() {
        floatWindow.hideFloatWindow();
        wv_result.loadDataWithBaseURL("", "", "text/html", "UTF-8", "");
        incomingNumber = "";
        result360 = "查询中";
        result114 = "查询中";
        resultBaidu = "查询中";
        callTime = 0;
        resultCount = 0;
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
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (120 * 1000),
                    restartIntent);
        }

    }

    private static class MyHandler extends Handler {
        MainService service;

        private MyHandler(MainService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String content = (String) msg.obj;
            service.wv_result.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
        }
    }
}
