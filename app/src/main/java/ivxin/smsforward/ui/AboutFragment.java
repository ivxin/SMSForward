package ivxin.smsforward.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ivxin.smsforward.Constants;
import ivxin.smsforward.R;
import ivxin.smsforward.base.BaseFragment;

/**
 * Created by yaping.wang on 2017/9/14.
 */

public class AboutFragment extends BaseFragment {
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_qrcode_alipay:
                    showImageDialog(R.mipmap.qrcode_alipay_);
                    break;
                case R.id.iv_qrcode_weixin:
                    showImageDialog(R.mipmap.qrcode_weixin_);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, null);
        TextView tv_app_version = view.findViewById(R.id.tv_app_version);
        ImageView iv_qrcode_alipay = view.findViewById(R.id.iv_qrcode_alipay);
        ImageView iv_qrcode_weixin = view.findViewById(R.id.iv_qrcode_weixin);
        tv_app_version.setText(Constants.getAppVersionName(getContext()));
        iv_qrcode_alipay.setOnClickListener(onClickListener);
        iv_qrcode_weixin.setOnClickListener(onClickListener);
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void showImageDialog(final int imageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder.create();
        ImageView imageView = new ImageView(getContext());
        dialog.setView(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(50, 50, 50, 50);
        imageView.setMinimumHeight(Constants.getScreenWidthPixels(getContext()));
        imageView.setImageResource(imageResId);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "长按保存图片", Toast.LENGTH_SHORT).show();
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new SaveImageRunnable(imageResId).start();
                return true;
            }
        });
        dialog.show();
    }

    Handler saveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String text = null;
            switch (msg.what) {
                case 1:
                    text = "保存成功";
                    break;
                case 0:
                    text = "保存失败";
                    break;
            }
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        }
    };

    private class SaveImageRunnable extends Thread {
        int resId;

        SaveImageRunnable(int resId) {
            this.resId = resId;
        }

        @Override
        public void run() {
            Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
            try {
                saveFile(((BitmapDrawable) drawable).getBitmap(), "图片" + resId + ".jpg");
                saveHandler.sendEmptyMessage(1);
            } catch (Exception e) {
                e.printStackTrace();
                saveHandler.sendEmptyMessage(0);
            }
        }

        /**
         * 保存文件
         *
         * @param bm
         * @param fileName
         * @throws IOException
         */
        private void saveFile(Bitmap bm, String fileName) throws Exception {
            File dirFolder = new File(Environment.getExternalStorageDirectory(), fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dirFolder));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        }
    }

}
