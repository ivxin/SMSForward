package ivxin.sms_forward_ding_talk_bot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.sms_forward_ding_talk_bot.constants.Constants;
import ivxin.sms_forward_ding_talk_bot.service.MainService;
import ivxin.sms_forward_ding_talk_bot.util.AssertReader;
import ivxin.sms_forward_ding_talk_bot.util.DingTalkBotSenderUtil;
import ivxin.sms_forward_ding_talk_bot.util.SignalUtil;

public class MainActivity extends BaseActivity {
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    /**
     * 钉钉机器人TOKEN
     */
    private TextView tv_token;
    private TextView tv_status;
    private CheckBox cb_keep_screen_on;
    private CheckBox cb_reject_call;
    /**
     * 保存 开始
     */
    private ToggleButton tv_start;
    private TextView tv_last_forward;
    private long lastPressed;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            default:
                break;
            case R.id.menu_help:
                AssertReader.readStringFromAssertFile(this, "help.txt", string -> showMessageDialog("帮助", string));
//                showMessageDialog("帮助", AssertReader.readStringFromAssertFile(this, "help.txt"));
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getPermission();
        if (Constants.HAVE_PERMISSION && Constants.spLoadBoolean(this, Constants.START_KEY)) {
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
        }

        if (getIntent().getExtras() != null) {
            String message = getIntent().getExtras().getString("message");
            if (!TextUtils.isEmpty(message)) {
                getWindow().getDecorView().postDelayed(() -> {
                    toast(message);
                    singleThreadExecutor.execute(() -> DingTalkBotSenderUtil.postDingTalk(Constants.spLoad(MainActivity.this, Constants.TOKEN_KEY, Constants.DING_TALK_BOT_TOKEN),
                            String.format(Locale.CHINA, "App启动消息：%s", message)));
                }, 1000);
            }
        }
        setKeepScreenOn(Constants.spLoadBoolean(this, Constants.KEEP_SCREEN_ON_KEY));
        DingTalkBotSenderUtil.setOnSendCallbackListener((message, result) -> {
            if (Constants.spLoadBoolean(MainActivity.this, Constants.REJECT_INCOMING_KEY) && Constants.isRinging) {
                SignalUtil.endcall(MainActivity.this);
            }
            Constants.spSave(MainActivity.this, Constants.LAST_FORWARD_KEY, String.format(Locale.CHINA, "%s\n\n%s", result, message));
            runOnUiThread(() -> {
                if (!isFinishing() && tv_last_forward != null) {
                    tv_last_forward.setText(String.format(Locale.CHINA, "%s\n\n%s", result, message));
                }
            });
        });
    }

    private void initView() {
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_token = (TextView) findViewById(R.id.tv_token);
        cb_keep_screen_on = (CheckBox) findViewById(R.id.cb_keep_screen_on);
        cb_reject_call = (CheckBox) findViewById(R.id.cb_reject_call);
        tv_start = (ToggleButton) findViewById(R.id.tv_start);
        tv_last_forward = (TextView) findViewById(R.id.tv_last_forward);

        boolean isStarted = Constants.spLoadBoolean(this, Constants.START_KEY);
        tv_status.setText(isStarted ? "启动中" : "已停止");
        tv_start.setChecked(isStarted);
        tv_token.setEnabled(!isStarted);
        tv_token.setText(Constants.spLoad(this, Constants.TOKEN_KEY));
        tv_last_forward.setText(Constants.spLoad(this, Constants.LAST_FORWARD_KEY));
        cb_keep_screen_on.setChecked(Constants.spLoadBoolean(this, Constants.KEEP_SCREEN_ON_KEY));
        cb_reject_call.setChecked(Constants.spLoadBoolean(this, Constants.REJECT_INCOMING_KEY));

        tv_token.setOnClickListener(v -> {
            final EditText editText = new EditText(MainActivity.this);
            editText.setText(tv_token.getText());
            editText.setSelectAllOnFocus(true);
            editText.setSelection(editText.getText().length());
            editText.setPadding(50, 50, 50, 50);
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(editText)
                    .setPositiveButton("确定", (dialog, which) -> {
                        dialog.dismiss();
                        tv_token.setText(editText.getText());
                        Constants.spSave(MainActivity.this, Constants.TOKEN_KEY, editText.getText().toString());
                    })
                    .setNegativeButton("取消", null)
                    .create();
            alertDialog.show();
        });

        tv_start.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String token = Constants.spLoad(MainActivity.this, Constants.TOKEN_KEY);
            if (Constants.HAVE_PERMISSION && !TextUtils.isEmpty(token)) {
                Intent intent = new Intent(this, MainService.class);
                startService(intent);
            } else {
                if (TextUtils.isEmpty(token)) {
                    showMessageDialog("提示", "token为空 检查配置");
                }
                if (!Constants.HAVE_PERMISSION) {
                    showMessageDialog("提示", "请给权限");
                }
                tv_start.setChecked(false);
                return;
            }
            tv_token.setEnabled(!isChecked);
            tv_status.setText(isChecked ? "启动中" : "已停止");
            Constants.spSave(MainActivity.this, Constants.START_KEY, isChecked);
        });
        cb_keep_screen_on.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Constants.spSave(MainActivity.this, Constants.KEEP_SCREEN_ON_KEY, isChecked);
            setKeepScreenOn(isChecked);
        });
        cb_reject_call.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.spSave(MainActivity.this, Constants.REJECT_INCOMING_KEY, isChecked));

    }

    private void setKeepScreenOn(boolean on) {
        getWindow().setFlags(on ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void getPermission() {
        checkPermissions(new OnPermissionCheckedListener() {
                             @Override
                             public void onPermissionGranted(String permission) {
                                 Constants.HAVE_PERMISSION = true;
                             }

                             @Override
                             public void onPermissionDenied(String permission) {
                                 Constants.HAVE_PERMISSION = false;
                             }
                         }, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastPressed < 2000) {
            finish();
            overridePendingTransition(0, 0);
        } else {
            toast("再按一次退出");
            lastPressed = System.currentTimeMillis();
        }
    }
}
