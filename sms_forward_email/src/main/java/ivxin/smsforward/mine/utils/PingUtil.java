package ivxin.smsforward.mine.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingUtil {
    private static PingPongThread pingPongThread;

    public interface PingCallback {
        void pong(String content);
    }

    public static void ping(String host, PingCallback pingCallback) {
        pingPongThread = new PingPongThread(host, pingCallback);
        pingPongThread.start();
    }

    public static void stopPing() {
        if (pingPongThread != null) {
            pingPongThread.interrupt();
        }
    }

    static class PingPongThread extends Thread {
        private String host;
        private PingCallback pingCallback;
        private boolean interrupt = false;

        private PingPongThread(String host, PingCallback pingCallback) {
            this.host = host;
            this.pingCallback = pingCallback;
        }

        public void interrupt() {
            this.interrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!interrupt) {
                    sleep(2000);
                    Process process = Runtime.getRuntime().exec("ping -c 5 -w 2 " + host);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder stringBuffer = new StringBuilder();
                    String content;
                    while ((content = bufferedReader.readLine()) != null) {
                        stringBuffer.append(content).append("\n");
                    }
                    content = stringBuffer.toString();
                    Log.d("PingUtil", "run: "+content);
                    if (pingCallback != null) {
                        pingCallback.pong(content);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
