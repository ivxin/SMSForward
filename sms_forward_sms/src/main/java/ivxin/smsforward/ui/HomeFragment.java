package ivxin.smsforward.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ivxin.smsforward.Constants;
import ivxin.smsforward.R;
import ivxin.smsforward.base.BaseFragment;

/**
 * Created by yaping.wang on 2017/9/14.
 */

public class HomeFragment extends BaseFragment {
    TextView tv_welcome;
    private SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);
        tv_welcome = view.findViewById(R.id.tv_welcome);
        sp = getContext().getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
        boolean isStarted = sp.getBoolean(Constants.STARTED_KEY, false);

        String a = isStarted ? "<big><b>短信转发已经启动</b></big><br><br>" + buildTip() : "<big><b>没有启动,在设置中配置</b></big>";
        String b = Constants.HAVE_PERMISSION ? "<br>在设置中修改转发条件" : "<br><color=red>最少需要收短信和发短信的权限才能正常工作</color>";
        tv_welcome.setText(Html.fromHtml(a + b));
        return view;
    }

    private String buildTip() {
        String tip = "";
        String from = sp.getString(Constants.NUM_REX_KEY, "").trim();
        String keyword = sp.getString(Constants.REX_KEY, "").trim();
        String target = sp.getString(Constants.TARGET_KEY, "").trim();
        String email_target = sp.getString(Constants.EMAIL_TARGET_KEY, "").trim();
        boolean is_sms_forward = sp.getBoolean(Constants.SMS_FORWARD, false);
        boolean is_email_forward = sp.getBoolean(Constants.EMAIL_FORWARD, false);
        if (TextUtils.isEmpty(from)) {
            from = "任何人的";
        } else {
            from = "包含[" + from.replaceAll(";", "]或[") + "]的号码";
        }
        if (TextUtils.isEmpty(keyword)) {
            keyword = "任何内容";
        } else {
            keyword = "内容含有[" + keyword.replaceAll(";", "]或[") + "]的字符";
        }
        if (is_sms_forward && !TextUtils.isEmpty(target)) {
            target = "[" + target.replaceAll(";", "],[") + "]";
        } else {
            target = "";
        }
        if (is_email_forward && !TextUtils.isEmpty(email_target)) {
            email_target = "[" + email_target.replaceAll(";", "],[") + "]";
        } else {
            email_target = "";
        }
        tip = "将转发来自" + from + "<br>且" + keyword + "的信息<br>到" + target + "<br>" + email_target;
        return tip;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
