package ivxin.smsforward.lib.utils;

import android.telephony.SmsManager;

import java.util.ArrayList;

public class SmsSender {
    public static void send(String content, String target) {
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> contents = manager.divideMessage(content);// 分割短信
        manager.sendMultipartTextMessage(target, null, contents, null, null);// 分割发送
    }
}
