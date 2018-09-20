package ivxin.incoming_call_query;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import ivxin.smsforward.lib.base.BaseActivity;
import ivxin.smsforward.lib.base.OnPermissionCheckedListener;
import ivxin.smsforward.lib.utils.AssertReader;
import ivxin.smsforward.lib.utils.DingTalkBotSenderUtil;

public class MainActivity extends BaseActivity {
    private TextView tv_info;
    private Intent serviceIntent;
    private TextView tv_token;
    private CheckBox cb_ding_talk;
    private WebView wv_result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);
        getPermissions();
    }

    void getPermissions() {
        checkPermissions(new OnPermissionCheckedListener() {
            @Override
            public void onPermissionGranted(String permission) {
                Constants.HAVE_PERMISSION = true;
                tv_info.setText("已启动");
            }

            @Override
            public void onPermissionDenied(String permission) {
                Constants.HAVE_PERMISSION = false;
                tv_info.setText("没有权限");
            }
        }, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE);
        //权限判断
        if (Build.VERSION.SDK_INT >= 23) {
            Constants.HAVE_FLOAT_PERMISSION = Settings.canDrawOverlays(this);
            if (!Constants.HAVE_FLOAT_PERMISSION) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
            }
        } else {
            Constants.HAVE_FLOAT_PERMISSION = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && Build.VERSION.SDK_INT >= 23) {
            Constants.HAVE_FLOAT_PERMISSION = Settings.canDrawOverlays(this);
        }

    }

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
            case R.id.menu_mini:
                finish();
                break;
            case R.id.menu_about:
                AssertReader.readStringFromAssertFile(this, "about.txt", string -> showMessageDialog("关于", string));
                break;
            case R.id.menu_exit:
                finish();
                stopService(serviceIntent);
                Process.killProcess(Process.myPid());
                System.exit(0);
                break;
        }
        return true;
    }

    private void initView() {
        tv_info = (TextView) findViewById(R.id.tv_info);
        tv_token = (TextView) findViewById(R.id.tv_token);
        cb_ding_talk = (CheckBox) findViewById(R.id.cb_ding_talk);
        findViewById(R.id.iv_hide).setVisibility(View.GONE);
        wv_result = findViewById(R.id.wv_result);
        String content = Constants.spLoad(this, Constants.KEY_LAST_QUERY);
        wv_result.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
        cb_ding_talk.setChecked(Constants.spLoadBoolean(this, Constants.KEY_DING_TALK_FORWARD));
        cb_ding_talk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tv_token.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            Constants.spSave(MainActivity.this, Constants.KEY_DING_TALK_FORWARD, isChecked);
        });
        tv_token.setVisibility(cb_ding_talk.isChecked() ? View.VISIBLE : View.GONE);
        tv_token.setText(Constants.spLoad(this, Constants.KEY_DING_TALK_TOKEN));
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
                        Constants.spSave(MainActivity.this, Constants.KEY_DING_TALK_TOKEN, editText.getText().toString());
                    })
                    .setNegativeButton("取消", null)
                    .create();
            alertDialog.show();
        });
        DingTalkBotSenderUtil.setOnSendCallbackListener((message, result) -> runOnUiThread(() -> {
            if (!isFinishing()) {
                hideLoadingDialog();
//                MarkdownProcessor markdownProcessor = new MarkdownProcessor(this);
//                markdownProcessor.factory(TextFactory.create());
//                markdownProcessor.config(markdownConfiguration);
//                tv_info.setText(markdownProcessor.parse(message + "\n\n" + result));
//                MarkDownDisplayHelper.display(tv_info, message + "\n\n" + result);
                tv_info.setText(message + "\n\n" + result);
                wv_result.loadDataWithBaseURL("", Constants.spLoad(this, Constants.KEY_LAST_QUERY), "text/html", "UTF-8", "");
            }
        }));
    }

    public void close(View view) {
        finish();
    }

    public void query(View view) {
        final EditText editText = new EditText(MainActivity.this);
        editText.setPadding(50, 50, 50, 50);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(editText)
                .setPositiveButton("查询", (dialog, which) -> {
                    dialog.dismiss();
                    Intent serviceIntent = new Intent(this, MainService.class);
                    serviceIntent.putExtra(MainService.PHONE_NUMBER_TO_QUERY, editText.getText().toString().trim());
                    startService(serviceIntent);
                    showLoadingDialog();
                })
                .setNegativeButton("取消", null)
                .create();
        alertDialog.show();
    }

}
