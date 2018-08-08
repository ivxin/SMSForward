package ivxin.smsforward.mine.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ivxin.smsforward.mine.service.MainService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MainService.class));
    }
}
