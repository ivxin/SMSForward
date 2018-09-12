package ivxin.smsforward.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;


/**
 * Created by yaping.wang on 2017/9/18.
 */

public class SMSReceiver extends BroadcastReceiver {
    private static OnSMSReceivedListener onSMSReceivedListener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[0];
        int receiverCard = 0;
        if (pdus != null) {
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                byte[] pdu = (byte[]) pdus[i];
                receiverCard = 1;
                SmsMessage msg = SmsMessage.createFromPdu(pdu, "3gpp");
                if (msg == null) {//the second card if it has
                    msg = SmsMessage.createFromPdu(pdu, "3gpp2");
                    receiverCard = 2;
                }
                messages[i] = msg;
            }
        }
        if (messages.length == 0) return;
        long time = 0;
        StringBuilder smsContent = new StringBuilder();
        String senderAddress = "";
        for (SmsMessage message : messages) {
            time = message.getTimestampMillis();
            senderAddress = message.getOriginatingAddress();
            smsContent.append(message.getMessageBody());
        }
        if (onSMSReceivedListener != null) {
            onSMSReceivedListener.onSMSReceived(senderAddress, smsContent.toString(), receiverCard, time);
        }
    }

    public static void setOnSMSReceivedListener(OnSMSReceivedListener onSMSReceivedListener) {
        SMSReceiver.onSMSReceivedListener = onSMSReceivedListener;
    }

    public interface OnSMSReceivedListener {
        void onSMSReceived(String sender, String content, int receiverCard, long timestampMillis);
    }
}
