package ivxin.smsforward.mine;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;


/**
 * Created by yaping.wang on 2017/9/14.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    private OnPermissionCheckedListener onPermissionCheckedListener;
    static final int PERMISSION_REQUEST_CODE = 112;

    public void checkPermissions(OnPermissionCheckedListener listener, String... permissions) {
        onPermissionCheckedListener = listener;

        for (String permission : permissions) {
            if (Constants.sdkIsAboveM) {
                // 检查权限
                if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    if (onPermissionCheckedListener != null)
                        onPermissionCheckedListener.onPermissionGranted(permission);
                } else {
                    // 进入到这里代表没有权限.执行请求权限
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
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
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
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

    public void showMessageDialog(CharSequence title, CharSequence message) {
        showConfirmDialog(title, message, "OK", (dialog, which) -> dialog.dismiss(), "", null);
    }

    public void showMessageDialog(CharSequence title, CharSequence message, long dismissDelay) {
        AlertDialog alertDialog = showConfirmDialog(title, message, "OK", (dialog, which) -> dialog.dismiss(), "", null);
        getWindow().getDecorView().postDelayed(alertDialog::dismiss, dismissDelay);
    }

    public AlertDialog showConfirmDialog(CharSequence title, CharSequence message,
                                         CharSequence positiveText, DialogInterface.OnClickListener positiveListener,
                                         CharSequence negativeText, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        if (!TextUtils.isEmpty(positiveText))
            builder.setPositiveButton(positiveText, positiveListener);
        if (!TextUtils.isEmpty(negativeText))
            builder.setNegativeButton(negativeText, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public void toast(CharSequence text) {
        toast(text, Toast.LENGTH_SHORT);
    }

    public void toast(CharSequence text, int length) {
        toast(this, text, length);
    }

    private Toast mToast;

    public void toast(Context context, CharSequence text, int length) {
        mToast = Toast.makeText(context, text, length);
        mToast.setText(text);
        mToast.setDuration(length);
        mToast.show();
    }

    public void toast(Context context, int resId, int length) {
        mToast = Toast.makeText(context, resId, length);
        mToast.setText(resId);
        mToast.setDuration(length);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null)
            mToast.cancel();
    }

}
