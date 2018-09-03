package ivxin.sms_forward_ding_talk_bot.util;

import android.content.Context;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssertReader {

    public static void readStringFromAssertFile(Context context, String fileName, OnStringReadCallbackListener onStringReadCallbackListener) {
        new Thread(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                InputStream inputStream = context.getAssets().open(fileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                stringBuilder.append(e.getMessage());
            }
            if (onStringReadCallbackListener != null) {
                Looper.prepare();
                onStringReadCallbackListener.onSuccess(stringBuilder.toString());
                Looper.loop();
            }
        }).start();
    }

    public interface OnStringReadCallbackListener {
        void onSuccess(String string);
    }

    public static String readStringFromAssertFile(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
}
