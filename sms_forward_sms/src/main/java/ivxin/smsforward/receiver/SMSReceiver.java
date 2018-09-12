package ivxin.smsforward.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;

import ivxin.smsforward.entity.SMSEntity;
import ivxin.smsforward.utils.SMSSendingHandler;


/**
 * Created by yaping.wang on 2017/9/18.
 */

public class SMSReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[0];
        if (pdus != null) {
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                byte[] pdu = (byte[]) pdus[i];
                messages[i] = SmsMessage.createFromPdu(pdu, "3gpp");
            }
        }
        long time = 0;
        StringBuilder smsContent = new StringBuilder();
        String senderAddress = "";
        for (SmsMessage message : messages) {
            time = message.getTimestampMillis();
            senderAddress = message.getOriginatingAddress();
            smsContent.append(message.getMessageBody());
        }
        SMSEntity newSms = new SMSEntity();
        newSms.setReceivedTime(time);
        newSms.setSender(senderAddress);
        newSms.setContent(smsContent.toString());
        new SMSSendingHandler(context, newSms).start();
    }
}
