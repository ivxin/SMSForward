package ivxin.smsforward.mine.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

public class SignalUtil {
    public static final String TAG = SignalUtil.class.getSimpleName();
    public static final int NETWORKTYPE_WIFI = 0;
    public static final int NETWORKTYPE_4G = 1;
    public static final int NETWORKTYPE_2G = 2;
    public static final int NETWORKTYPE_NONE = 3;

    public static class SignalListener extends PhoneStateListener {
        private Context context;
        private OnSignalChangeCallback onSignalChangeCallback;
        private OnPhoneCallIncomingCallback onPhoneCallIncomingCallback;

        public SignalListener(Context context, OnSignalChangeCallback onSignalChangeCallback, OnPhoneCallIncomingCallback onPhoneCallIncomingCallback) {
            this.context = context;
            this.onSignalChangeCallback = onSignalChangeCallback;
            this.onPhoneCallIncomingCallback = onPhoneCallIncomingCallback;
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
//            api23
//            int level = signalStrength.getLevel();//0-4
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            int netWorkType = getNetWorkType(context);
            if (onSignalChangeCallback != null) {
                onSignalChangeCallback.onSignalChange(netWorkType, gsmSignalStrength);
            }
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "onCallStateChanged:CALL_STATE_IDLE" + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK" + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged: CALL_STATE_RINGING" + incomingNumber);
                    if (onPhoneCallIncomingCallback != null) {
                        onPhoneCallIncomingCallback.onPhoneCallIncomingCallback(incomingNumber);
                    }
                    break;
            }
        }

    }

    /**
     * 通过反射的方式挂断电话
     */
    public static void endcall(Context context) {
        try {
            //获取到ServiceManager
            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<TelephonyManager> c = TelephonyManager.class;
            //获取到ServiceManager里面的方法
            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
            //允许访问私有方法
            mthEndCall.setAccessible(true);
            final Object obj = mthEndCall.invoke(telMag, (Object[]) null);

            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
            Method mt = obj.getClass().getMethod("endCall");
            //允许访问私有方法
            mt.setAccessible(true);
            mt.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnSignalChangeCallback {
        void onSignalChange(int signalType, int gsmSignalStrength);
    }

    public interface OnPhoneCallIncomingCallback {
        void onPhoneCallIncomingCallback(String incomingNumber);
    }

    public static int getNetWorkType(Context context) {
        int mNetWorkType = -1;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = NETWORKTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return isFastMobileNetwork(context) ? NETWORKTYPE_4G : NETWORKTYPE_2G;
            }
        } else {
            mNetWorkType = NETWORKTYPE_NONE;
        }
        return mNetWorkType;
    }

    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE;
    }
}
