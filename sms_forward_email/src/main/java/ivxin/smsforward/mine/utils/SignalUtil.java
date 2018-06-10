package ivxin.smsforward.mine.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class SignalUtil {
    public static final int NETWORKTYPE_WIFI = 0;
    public static final int NETWORKTYPE_4G = 1;
    public static final int NETWORKTYPE_2G = 2;
    public static final int NETWORKTYPE_NONE = 3;

    public static class SignalListener extends PhoneStateListener {
        private Context context;
        private OnSignalChangeCallback onSignalChangeCallback;

        public SignalListener(Context context, OnSignalChangeCallback onSignalChangeCallback) {
            this.context = context;
            this.onSignalChangeCallback = onSignalChangeCallback;
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
    }

    public interface OnSignalChangeCallback {
        void onSignalChange(int signalType, int gsmSignalStrength);
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
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE;
    }
}
