package ivxin.smsforward.mine.service;


import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ivxin.smsforward.mine.ProcessConnection;

public class GuardService extends Service {
    public static final String TAG = GuardService.class.getSimpleName();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        //绑定建立链接
        Intent mIntent = new Intent(getApplicationContext(), MainService.class);
        bindService(mIntent, mServiceConnection, Context.BIND_IMPORTANT);

        return START_STICKY;
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "GuardService:建立链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开链接
            unbindService(mServiceConnection);
            //重新绑定
            Intent intent = new Intent(getApplicationContext(), MainService.class);
            bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    };
}
