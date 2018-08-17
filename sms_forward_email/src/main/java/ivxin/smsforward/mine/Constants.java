package ivxin.smsforward.mine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import ivxin.smsforward.mine.entity.CommandEmail;

/**
 * Created by yaping.wang on 2017/9/18.
 */

public class Constants {
    public static final String BR = "<br>";
    public static final String DB_FILE_NAME = "sent_email.db";

    public class SP_KEYS {
        public static final String KEY_STARTED = "KEY_STARTED";
        public static final String KEY_CONTENT_IN_SUBJECT = "KEY_CONTENT_IN_SUBJECT";
        public static final String KEY_INCOMING_CALL_MAIL = "KEY_INCOMING_CALL_MAIL";
        public static final String KEY_REJECT_INCOMING_CALLS = "KEY_REJECT_INCOMING_CALLS";
        public static final String KEY_KEEP_SCREEN_ON = "KEY_KEEP_SCREEN_ON";

        public static final String KEY_SENDER_EMAIL = "KEY_SENDER_EMAIL";
        public static final String KEY_SENDER_PASSWORD = "KEY_SENDER_PASSWORD";
        public static final String KEY_SERVER_HOST = "KEY_SERVER_HOST";
        public static final String KEY_SERVER_PORT = "KEY_SERVER_PORT";
        public static final String KEY_SOCKET_FACTORY_PORT = "KEY_SOCKET_FACTORY_PORT";
        public static final String KEY_AUTENTICATION = "KEY_AUTENTICATION";
        public static final String KEY_RECEIVER_EMAIL = "KEY_RECEIVER_EMAIL";

        public static final String KEY_REMOTE_SEND_SMS = "KEY_REMOTE_SEND_SMS";
        public static final String KEY_COMMAND_SERVER_HOST = "KEY_COMMAND_SERVER_HOST";
        public static final String KEY_COMMAND_USERNAME = "KEY_COMMAND_USERNAME";
        public static final String KEY_COMMAND_PASSWORD = "KEY_COMMAND_PASSWORD";
        public static final String KEY_COMMAND_CODE = "KEY_COMMAND_CODE";
        public static final String KEY_COMMAND_CHECK_TIME = "KEY_COMMAND_CHECK_TIME";


    }

    public static final boolean sdkIsAboveM = Build.VERSION.SDK_INT >= 23;//Build.VERSION_CODES.M			Android 6.0
    private static final String sp_file_name = "config.xml";

    public static final String PATTERN = "yyyy-MM-dd HH:ss:mm";
    public static boolean HAVE_PERMISSION = false;

    public static String card1Name = "";
    public static String card2Name = "";
    public static int cardCount;
    public static int signalType;

    public static boolean started = false;

    public static String senderEmail = "";
    public static String senderEmailPassword = "";

    public static String serverHost = "smtp.qq.com";
    public static int serverPort = 465;
    public static int socketFactoryPort = 465;
    public static boolean autenticationEnabled = true;

    public static String receiverEmail = "";
    public static boolean isContentInSubject = false;
    public static boolean incomingCallMail = false;
    public static boolean rejectIncomingCalls = false;
    public static boolean keepScreenOn = true;

    public static boolean remoteSentSms = false;

    public static String commandMailHost = "imap.qq.com";
    public static String commandUsername = "";
    public static String commandPassword = "";
    public static int commandCheckTime = 60;
    public static String commandCode = "";

    public static int battery_level;
    public static long battery_low_warning_last_time;
    public static boolean isCharging;
    public static String networkState;
    public static boolean isRinging = false;

    public static void saveLastMail(Context context, CommandEmail commandEmail) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(sp_file_name, Context.MODE_PRIVATE);
        sp.edit().putString(commandEmail.getMessageId(), commandEmail.getContent()).apply();
    }

    public static boolean isLastMail(Context context, String messageId) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(sp_file_name, Context.MODE_PRIVATE);
        return sp.contains(messageId);
    }

    public static void saveConfigToSP(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(sp_file_name, Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean(SP_KEYS.KEY_STARTED, started)
                .putBoolean(SP_KEYS.KEY_CONTENT_IN_SUBJECT, isContentInSubject)
                .putBoolean(SP_KEYS.KEY_INCOMING_CALL_MAIL, incomingCallMail)
                .putBoolean(SP_KEYS.KEY_REJECT_INCOMING_CALLS, rejectIncomingCalls)
                .putBoolean(SP_KEYS.KEY_KEEP_SCREEN_ON, keepScreenOn)
                .putBoolean(SP_KEYS.KEY_REMOTE_SEND_SMS, remoteSentSms)
                .putString(SP_KEYS.KEY_COMMAND_SERVER_HOST, commandMailHost)
                .putString(SP_KEYS.KEY_COMMAND_USERNAME, commandUsername)
                .putString(SP_KEYS.KEY_COMMAND_PASSWORD, commandPassword)
                .putString(SP_KEYS.KEY_COMMAND_CODE, commandCode)
                .putInt(SP_KEYS.KEY_COMMAND_CHECK_TIME, commandCheckTime)
                .putString(SP_KEYS.KEY_SENDER_EMAIL, senderEmail)
                .putString(SP_KEYS.KEY_SENDER_PASSWORD, senderEmailPassword)
                .putString(SP_KEYS.KEY_SERVER_HOST, serverHost)
                .putInt(SP_KEYS.KEY_SERVER_PORT, serverPort)
                .putInt(SP_KEYS.KEY_SOCKET_FACTORY_PORT, socketFactoryPort)
                .putBoolean(SP_KEYS.KEY_AUTENTICATION, autenticationEnabled)
                .putString(SP_KEYS.KEY_RECEIVER_EMAIL, receiverEmail).apply();

    }

    public static void loadConfigFromSP(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(sp_file_name, Context.MODE_PRIVATE);
        started = sp.getBoolean(SP_KEYS.KEY_STARTED, false);
        isContentInSubject = sp.getBoolean(SP_KEYS.KEY_CONTENT_IN_SUBJECT, false);
        incomingCallMail = sp.getBoolean(SP_KEYS.KEY_INCOMING_CALL_MAIL, false);
        rejectIncomingCalls = sp.getBoolean(SP_KEYS.KEY_REJECT_INCOMING_CALLS, false);
        keepScreenOn = sp.getBoolean(SP_KEYS.KEY_KEEP_SCREEN_ON, true);
        remoteSentSms = sp.getBoolean(SP_KEYS.KEY_REMOTE_SEND_SMS, false);
        commandMailHost = sp.getString(SP_KEYS.KEY_COMMAND_SERVER_HOST, "");
        commandUsername = sp.getString(SP_KEYS.KEY_COMMAND_USERNAME, "");
        commandPassword = sp.getString(SP_KEYS.KEY_COMMAND_PASSWORD, "");
        commandCode = sp.getString(SP_KEYS.KEY_COMMAND_CODE, "");
        commandCheckTime = sp.getInt(SP_KEYS.KEY_COMMAND_CHECK_TIME, 60);
        senderEmail = sp.getString(SP_KEYS.KEY_SENDER_EMAIL, "");
        senderEmailPassword = sp.getString(SP_KEYS.KEY_SENDER_PASSWORD, "");
        serverHost = sp.getString(SP_KEYS.KEY_SERVER_HOST, "");
        serverPort = sp.getInt(SP_KEYS.KEY_SERVER_PORT, 0);
        socketFactoryPort = sp.getInt(SP_KEYS.KEY_SOCKET_FACTORY_PORT, 0);
        autenticationEnabled = sp.getBoolean(SP_KEYS.KEY_AUTENTICATION, false);
        receiverEmail = sp.getString(SP_KEYS.KEY_RECEIVER_EMAIL, "");
    }

    public static String getDeviceState() {
        return String.format(BR + "设备名称：%s" + BR +
                        "电池电量：%s" + BR +
                        "充电状态：%s" + BR +
                        "网络状态：%s" + BR,
                Build.BRAND + " " + Build.MODEL, Constants.battery_level, Constants.isCharging ? "是" : "否", Constants.networkState);
    }
}
