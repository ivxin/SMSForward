package ivxin.smsforward.lib.utils.callhelper;

import android.content.Context;

import ivxin.smsforward.lib.utils.callhelper.scheme.CallSchemeAcceptADB;
import ivxin.smsforward.lib.utils.callhelper.scheme.CallSchemeAcceptAPI19;
import ivxin.smsforward.lib.utils.callhelper.scheme.CallSchemeAcceptAPI26;
import ivxin.smsforward.lib.utils.callhelper.scheme.CallSchemeAcceptAPI26_23;
import ivxin.smsforward.lib.utils.callhelper.scheme.CallSchemeReject;
import ivxin.smsforward.lib.utils.callhelper.scheme.ICallSchemeAccept;
import ivxin.smsforward.lib.utils.callhelper.scheme.ICallSchemeReject;


/**
 * created by Lin on 2017/12/16
 */

public class CallHelper {
    
    private static CallHelper sInstance = null;
    public synchronized static CallHelper getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CallHelper(context.getApplicationContext());
        }
        return sInstance;
    }
    
    private ICallSchemeAccept mICallSchemeAccept;
    private ICallSchemeReject mICallSchemeReject;
    private CallHelper(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            if (context.getApplicationInfo().targetSdkVersion <= 22) {
                mICallSchemeAccept = new CallSchemeAcceptAPI26();
            } else {
                mICallSchemeAccept = new CallSchemeAcceptAPI26_23();
            }
        } else if (android.os.Build.VERSION.SDK_INT >= 19) {
            mICallSchemeAccept = new CallSchemeAcceptAPI19();
        } else {
            mICallSchemeAccept = new CallSchemeAcceptADB();
        }
        mICallSchemeReject = new CallSchemeReject();
    }
    
    public void rejectCall(Context context) throws Exception {
        mICallSchemeReject.rejectCall(context);   
    }
    
    public void acceptCall(Context context) throws Exception {
        mICallSchemeAccept.acceptCall(context);
    }
    
}
