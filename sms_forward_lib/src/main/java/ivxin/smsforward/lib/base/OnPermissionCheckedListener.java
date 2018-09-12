package ivxin.smsforward.lib.base;

/**
 * Created by yaping.wang on 2017/9/18.
 */

public interface OnPermissionCheckedListener {
    void onPermissionGranted(String permission);

    void onPermissionDenied(String permission);
}
