package ivxin.smsforward.mine.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JunkSMSAnalysisUtil {
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void analysisText(String text, OnFinishCallbackListener onFinishCallbackListener) {
        singleThreadExecutor.execute(() -> {



            if (onFinishCallbackListener != null) {
                onFinishCallbackListener.onFinish(text);
            }
        });


    }

    public interface OnFinishCallbackListener {
        void onFinish(String result);
    }
}
