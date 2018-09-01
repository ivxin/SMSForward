package ivxin.sms_forward_ding_talk_bot.util;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class PhoneNumberJudge {

    public PhoneNumberJudge() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
    }

    public interface HarassingResultListener {
        /**
         * not in ui thread
         */
        void onSuccess(String result);
    }

    public void judgeNumberFrom360(String phoneNumber, HarassingResultListener harassingResultListener) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect("https://m.so.com/s?q=" + phoneNumber).get();
//                Document doc = Jsoup.connect("https://m.so.com/s?q=037155620323").get();
                Log.d("PhoneNumberJudge", "judgeNumberFrom360,doc.body: "+doc.body().toString().trim());
                Element element = doc.body();
                Elements mohe = element.getElementsByClass("mohe-cont");
//                String result = mohe.toString().trim();
                String result = mohe.text();
                Log.d("PhoneNumberJudge", "judgeNumberFrom360: " + result);
                harassingResultListener.onSuccess(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}