package ivxin.sms_forward_ding_talk_bot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DingTalkBotSenderUtil {
    //https://oapi.dingtalk.com/robot/send?access_token=36a3d3d413b7e3be0a399626a69c77764e80560cb80954aee0352ee33f25d906
    public static final String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token={token}";


    public static String postDingTalk(String token, String message) {
        StringBuilder result = new StringBuilder();
        try {
            // 构建URL
            URL uri = new URL(WEBHOOK_TOKEN.replace("{token}", token));
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // 构建参数
            String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"" + message + "\"}}";
            byte[] param = textMsg.getBytes();
            // 设置请求头
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Charset", "uft-8");
            conn.setRequestProperty("Content-Length", String.valueOf(param.length));

            // 获取输出流,发送参数数据
            OutputStream out = conn.getOutputStream();
            out.write(param);
            out.flush();

            // 获取输入流,处理返回的数据
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } else {
                result = new StringBuilder(conn.getResponseCode() + ":" + conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = new StringBuilder("MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            result = new StringBuilder("IOException");
            e.printStackTrace();
        }
        if (onSendCallbackListener != null) {
            onSendCallbackListener.onSend(message, result.toString());
        }
        return result.toString();
    }

    public static void setOnSendCallbackListener(OnSendCallbackListener onSendCallbackListener) {
        DingTalkBotSenderUtil.onSendCallbackListener = onSendCallbackListener;
    }

    private static OnSendCallbackListener onSendCallbackListener;

    public interface OnSendCallbackListener {
        void onSend(String message, String result);
    }
}
