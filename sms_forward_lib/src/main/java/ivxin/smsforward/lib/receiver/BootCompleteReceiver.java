package ivxin.smsforward.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ivxin.smsforward.lib.service.BaseService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BaseService.class));
    }
}
