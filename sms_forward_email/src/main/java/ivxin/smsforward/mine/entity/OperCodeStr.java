package ivxin.smsforward.mine.entity;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OperCodeStr {
    String code;
    String name;
    String country;

    private static List<OperCodeStr> readFromAssets(Context context) {
        List<OperCodeStr> operCodeStrList = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("oper.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            while (line != null) {
                if (!TextUtils.isEmpty(line)) {
                    OperCodeStr operCodeStr = new OperCodeStr();
                    String[] opers = line.trim().split(",");
                    operCodeStr.code = opers[0].trim();
                    operCodeStr.name = opers[1].trim();
                    operCodeStr.country = opers[2].trim();
                    operCodeStrList.add(operCodeStr);
                }
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return operCodeStrList;
    }

    public static String selectOperate(Context context, String code) {
        List<OperCodeStr> operCodeStrList = readFromAssets(context);
        for (OperCodeStr operCodeStr : operCodeStrList) {
            if (operCodeStr.code.equals(code)) {
                return operCodeStr.name;
            }
        }
        return "UNKNOWN";
    }
}
