package ivxin.smsforward.lib.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import ivxin.smsforward.lib.ProcessConnection;


public abstract class BaseService extends Service {
    public static final String TAG = BaseService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //绑定建立链接
        Intent mIntent = new Intent(getApplicationContext(), GuardService.class);
        bindService(mIntent, mServiceConnection, Context.BIND_IMPORTANT);
        Notification notification = showOnGoingNotification("");
        startForeground(1, notification);
        Log.d(TAG, "onStartCommand: startId:" + startId);
        return START_STICKY;
    }

    public abstract Notification showOnGoingNotification(String content);

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

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "BaseService:建立链接");
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
