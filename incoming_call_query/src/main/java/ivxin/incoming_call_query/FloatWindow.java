package ivxin.incoming_call_query;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class FloatWindow implements View.OnTouchListener {
    private Context mContext;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager mWindowManager;

    private View mFloatLayout;
    private float mInViewX;
    private float mInViewY;
    private float mDownInScreenX;
    private float mDownInScreenY;
    private float mInScreenX;
    private float mInScreenY;

    public FloatWindow(Context context, View view) {
        this.mContext = context;
        initFloatWindow(view);
    }

    private void initFloatWindow(View view) {
        mFloatLayout = view;
        mFloatLayout.setOnTouchListener(this);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {//8.0新特性
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return floatLayoutTouch(motionEvent);
    }

    private boolean floatLayoutTouch(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获取相对View的坐标，即以此View左上角为原点
                mInViewX = motionEvent.getX();
                mInViewY = motionEvent.getY();
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                mDownInScreenX = motionEvent.getRawX();
                mDownInScreenY = motionEvent.getRawY() - getSysBarHeight(mContext);
                mInScreenX = motionEvent.getRawX();
                mInScreenY = motionEvent.getRawY() - getSysBarHeight(mContext);
                break;
            case MotionEvent.ACTION_MOVE:
                // 更新浮动窗口位置参数
                mInScreenX = motionEvent.getRawX();
                mInScreenY = motionEvent.getRawY() - getSysBarHeight(mContext);
                mWindowParams.x = (int) (mInScreenX - mInViewX);
                mWindowParams.y = (int) (mInScreenY - mInViewY);
                // 手指移动的时候更新小悬浮窗的位置
                mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (mDownInScreenX == mInScreenX && mDownInScreenY == mInScreenY) {

                }
                break;
        }
        return true;
    }

    public void showFloatWindow() {
        if (mFloatLayout.getParent() == null) {
            DisplayMetrics metrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
            mWindowParams.x = mWindowParams.width > 0 ? (getScreenWidth(mContext) - mWindowParams.width) / 2 : 0;
            mWindowParams.y = mWindowParams.height > 0 ? (getScreenHeight(mContext) - mWindowParams.height) / 2 : getScreenHeight(mContext) / 4;
            mWindowManager.addView(mFloatLayout, mWindowParams);
        }
    }

    public void hideFloatWindow() {
        if (mFloatLayout.getParent() != null)
            mWindowManager.removeView(mFloatLayout);
    }

    public void setFloatLayoutAlpha(float alpha) {
        mFloatLayout.setAlpha(alpha);
    }

    public void setFloatWindowWidth(int floatWindowWidth) {
        mWindowParams.width = floatWindowWidth;
    }

    public void setFloatWindowHeight(int floatWindowHeight) {
        mWindowParams.height = floatWindowHeight;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.heightPixels;
    }

    // 获取系统状态栏高度
    public static int getSysBarHeight(Context contex) {
        Class<?> c;
        Object obj;
        Field field;
        int x;
        int sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = contex.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
}
