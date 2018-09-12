package ivxin.smsforward.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.List;

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int intLevel = intent.getIntExtra("level", 0);
        int intScale = intent.getIntExtra("scale", 100);
        int percent = (int) (intLevel * 100.0 / intScale);
        if (onBatteryInfoUpdateListeners != null && onBatteryInfoUpdateListeners.size() > 0) {
            for (OnBatteryInfoUpdateListener onBatteryInfoUpdateListener : onBatteryInfoUpdateListeners) {
                onBatteryInfoUpdateListener.onBatteryUpdate(action, status, percent);
            }
        }
    }

    private static List<OnBatteryInfoUpdateListener> onBatteryInfoUpdateListeners = new ArrayList<>();

    public static void clearOnBatteryInfoUpdateListener() {
        onBatteryInfoUpdateListeners.clear();
    }

    public static void addOnBatteryInfoUpdateListener(OnBatteryInfoUpdateListener onBatteryInfoUpdateListener) {
        onBatteryInfoUpdateListeners.add(onBatteryInfoUpdateListener);
    }

    public static void removeOnBatteryInfoUpdateListener(OnBatteryInfoUpdateListener onBatteryInfoUpdateListener) {
        onBatteryInfoUpdateListeners.remove(onBatteryInfoUpdateListener);
    }

    public interface OnBatteryInfoUpdateListener {
        void onBatteryUpdate(String action, int status, int percent);
    }
}
