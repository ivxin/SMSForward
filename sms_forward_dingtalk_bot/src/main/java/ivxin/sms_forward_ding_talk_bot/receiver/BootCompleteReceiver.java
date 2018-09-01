package ivxin.sms_forward_ding_talk_bot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ivxin.sms_forward_ding_talk_bot.service.MainService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MainService.class));
    }
}
