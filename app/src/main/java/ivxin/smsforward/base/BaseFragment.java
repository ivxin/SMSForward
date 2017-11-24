package ivxin.smsforward.base;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import ivxin.smsforward.Constants;

/**
 * Created by yaping.wang on 2017/9/14.
 */

public abstract class BaseFragment extends Fragment {
    public abstract boolean onBackPressed();

    public void showConfirmDialog(String title, String msg, String buttonText, DialogInterface.OnClickListener onBottonClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setPositiveButton(buttonText, onBottonClick);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public void showConfirmDialog(String title, String msg,
                                  String buttonText1, String buttonText2,
                                  DialogInterface.OnClickListener onBottonClick1,
                                  DialogInterface.OnClickListener onBottonClick2) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setPositiveButton(buttonText1, onBottonClick1);
        alertDialog.setNegativeButton(buttonText2, onBottonClick2);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    private OnPermissionCheckedListener onPermissionCheckedListener;
    static final int PERMISSION_REQUEST_CODE = 112;

    public void checkPermissions(OnPermissionCheckedListener listener, String... permissions) {
        onPermissionCheckedListener = listener;
        for (String permission : permissions) {
            if (Constants.sdkIsAboveM) {
                // 检查权限
                if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                    if (onPermissionCheckedListener != null)
                        onPermissionCheckedListener.onPermissionGranted(permission);
                } else {
                    // 进入到这里代表没有权限.执行请求权限
                    ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_CODE);
                }
            } else {// 6.0以下不用检查权限
                if (onPermissionCheckedListener != null)
                    onPermissionCheckedListener.onPermissionGranted(permission);
            }
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (onPermissionCheckedListener != null)
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        int grantResult = grantResults[i];
                        if (grantResults.length > 0 && grantResult == PackageManager.PERMISSION_GRANTED) {
                            // 用户同意授权
                            onPermissionCheckedListener.onPermissionGranted(permission);
                        } else {
                            // 用户拒绝授权
                            onPermissionCheckedListener.onPermissionDenied(permission);
                        }
                    }
                break;
        }
    }
}
