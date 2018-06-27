package ivxin.smsforward.mine;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.loadmore.LoadMoreView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ivxin.smsforward.mine.db.DataBaseService;
import ivxin.smsforward.mine.entity.MailEntity;
import ivxin.smsforward.mine.entity.SMSEntity;
import ivxin.smsforward.mine.receiver.SMSReceiver;
import ivxin.smsforward.mine.utils.MailSenderHelper;
import ivxin.smsforward.mine.utils.PhoneNumberJudge;
import ivxin.smsforward.mine.utils.PingUtil;
import ivxin.smsforward.mine.utils.SignalUtil;
import ivxin.smsforward.mine.view.EmailAdapter;
import ivxin.smsforward.mine.view.MailLoadMoreView;

public class MainActivity extends BaseActivity {
    public static final boolean sdkBelow17 = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
    private BaseActivity activity;
    private long lastPressed;
    private ImageView iv_battery;
    private ImageView iv_signal;
    private LinearLayout ll_mail_send_log;
    private RecyclerView rv_mail_send_log;
    private TextView tv_clear_log;
    private ProgressBar pb_battery;
    private ProgressBar pb_signal;

    private TextView tv_battery;
    private TextView tv_ping;

    private ScrollView sv_config;

    private EditText et_sender_email;
    private EditText et_sender_email_password;

    private EditText et_server_host;
    private EditText et_server_port;
    private EditText et_socket_factory_port;
    private CheckBox cb_autentication;

    private EditText et_receiver_email;

    private CheckBox cb_content_send_via_subject;
    private CheckBox cb_send_incoming_call;
    private CheckBox cb_reject_incoming_call;
    private CheckBox cb_show_running_notification;
    private Button btn_test_email;

    private CheckBox cb_keep_screen_on;
    private ToggleButton btn_edit_config;

    private TextView tv_clock;
    private ValueAnimator animator;

    private EmailAdapter adapter;
    private List<MailEntity> list = new ArrayList<>();
    private int page = 0;
    private final int pageSize = 10;

    private TelephonyManager mTelephonyManager;
    private SignalUtil.SignalListener mListener;
    private int signalType;
    private NotificationManager mNotificationManager;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            Constants.isCharging = isCharging;
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int intLevel = intent.getIntExtra("level", 0);
                int intScale = intent.getIntExtra("scale", 100);
                final int percent = (int) (intLevel * 100.0 / intScale);
                Constants.battery_level = percent;
                runOnUiThread(() -> {
                    tv_battery.setText(String.format(Locale.CHINA, "%s％", percent));
                    pb_battery.setProgress(percent);
                    if (isCharging) {
                        iv_battery.setImageResource(R.drawable.ic_battery_charging_full_24dp);
                    } else {
                        if (percent == 100) {
                            iv_battery.setImageResource(R.drawable.ic_battery_std_24dp);
                        } else if (percent > 90) {
                            iv_battery.setImageResource(R.drawable.ic_battery_90_24dp);
                        } else if (percent > 80) {
                            iv_battery.setImageResource(R.drawable.ic_battery_80_24dp);
                        } else if (percent > 60) {
                            iv_battery.setImageResource(R.drawable.ic_battery_60_24dp);
                        } else if (percent > 50) {
                            iv_battery.setImageResource(R.drawable.ic_battery_50_24dp);
                        } else if (percent > 30) {
                            iv_battery.setImageResource(R.drawable.ic_battery_30_24dp);
                        } else if (percent > 20) {
                            iv_battery.setImageResource(R.drawable.ic_battery_20_24dp);
                        } else {
                            iv_battery.setImageResource(R.drawable.ic_battery_alert_24dp);
                        }
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setContentView(R.layout.activity_main);
        initView();
        getPermission();
        registerReceivers();
        page = 0;
        getMailData(page);
        SMSReceiver.setOnSMSReceivedListener((sender, content, receiverCard, timestampMillis) -> {
            SMSEntity smsEntity = new SMSEntity();
            smsEntity.setContent(content);
            smsEntity.setSender(sender);
            smsEntity.setReceivedTime(timestampMillis);
            smsEntity.setReceiverCard(receiverCard);
            smsEntity.setSendTime(System.currentTimeMillis());
            MailSenderHelper.sendEmail(smsEntity);
        });
        MailSenderHelper.setOnMailSentCallback(new MailSenderHelper.OnMailSentCallback() {
            @Override
            public void onSuccess(MailEntity mailEntity) {
                if (Constants.rejectIncomingCalls && Constants.isRinging) {
                    SignalUtil.endcall(MainActivity.this);
                }
                DataBaseService dataBaseService = new DataBaseService(activity);
                dataBaseService.insertMail(mailEntity);
                runOnUiThread(() -> {
                    if (isFinishing()) {
                        return;
                    }
                    showMessageDialog("", String.format(Locale.CHINA, "Send to %s succeed!", mailEntity.getReceiver()), 3000);
                    page = 0;
                    getMailData(page);
                });
            }

            @Override
            public void onFail(Exception e) {
                if (Constants.rejectIncomingCalls && Constants.isRinging) {
                    SignalUtil.endcall(MainActivity.this);
                }
                runOnUiThread(() -> toast("Failed:" + e.getMessage()));
            }
        });
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatInfoReceiver, intentFilter);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mListener = new SignalUtil.SignalListener(this, (signalType, gsmSignalStrength) -> this.signalType = signalType, incomingNumber -> {
            if (Constants.started) {
                if (Constants.incomingCallMail) {
                    PhoneNumberJudge netJudge = new PhoneNumberJudge();
                    netJudge.judgeNumberFrom360(incomingNumber, (result) -> {
                        String subject = String.format(Locale.CHINA, "[SMS Forward]%s 来电", incomingNumber);
                        MailEntity mailEntity = new MailEntity();
                        mailEntity.setReceiver(Constants.receiverEmail);
                        mailEntity.setSubject(subject);
                        mailEntity.setContent("来电查询结果：" + result);
                        mailEntity.setSendTime(System.currentTimeMillis());
                        MailSenderHelper.sendEmail(mailEntity);
                    });
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.HAVE_PERMISSION && Constants.showRunningNotification) {
            if (Constants.started) {
                PingUtil.ping(Constants.serverHost, content -> runOnUiThread(new PingUIRunnable(content)));
                showOnGoingNotification();
            } else {
                PingUtil.stopPing();
                hideOnGoingNotification();
            }
        }
        if (Constants.HAVE_PERMISSION)
            mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_SIGNAL_STRENGTHS |
                    SignalUtil.SignalListener.LISTEN_CALL_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PingUtil.stopPing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PingUtil.stopPing();
        mTelephonyManager.listen(mListener, SignalUtil.SignalListener.LISTEN_NONE);
        unregisterReceiver(mBatInfoReceiver);
    }

    private void getMailData(int pageIndex) {
        new Thread(() -> {
            DataBaseService dataBaseService = new DataBaseService(activity);
            List<MailEntity> dataList = dataBaseService.selectMailList(pageIndex, pageSize);
            if (dataList != null && dataList.size() > 0) {
                runOnUiThread(() -> {
                    if (pageIndex == 0) {
                        list.clear();
                    }
                    list.addAll(dataList);
                    if (dataList.size() < pageSize) {
                        adapter.loadMoreEnd(true);
                    } else {
                        adapter.loadMoreComplete();
                    }
                });
            }
        }).start();
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
            toast(getString(R.string.press_again_to_exit));
            lastPressed = System.currentTimeMillis();
        }
    }

    private void initView() {
        tv_battery = (TextView) findViewById(R.id.tv_battery);
        tv_ping = (TextView) findViewById(R.id.tv_ping);
        iv_battery = (ImageView) findViewById(R.id.iv_battery);
        iv_signal = (ImageView) findViewById(R.id.iv_signal);
        ll_mail_send_log = (LinearLayout) findViewById(R.id.ll_mail_send_log);
        rv_mail_send_log = (RecyclerView) findViewById(R.id.rv_mail_send_log);
        tv_clear_log = (TextView) findViewById(R.id.tv_clear_log);
        sv_config = (ScrollView) findViewById(R.id.sv_config);
        pb_battery = (ProgressBar) findViewById(R.id.pb_battery);
        pb_signal = (ProgressBar) findViewById(R.id.pb_signal);
        et_sender_email = (EditText) findViewById(R.id.et_sender_email);
        et_sender_email_password = (EditText) findViewById(R.id.et_sender_email_password);

        et_server_host = (EditText) findViewById(R.id.et_server_host);
        et_server_port = (EditText) findViewById(R.id.et_server_port);
        et_socket_factory_port = (EditText) findViewById(R.id.et_socket_factory_port);
        cb_autentication = (CheckBox) findViewById(R.id.cb_autentication);

        et_receiver_email = (EditText) findViewById(R.id.et_receiver_email);
        cb_content_send_via_subject = (CheckBox) findViewById(R.id.cb_content_send_via_subject);
        cb_send_incoming_call = (CheckBox) findViewById(R.id.cb_send_incoming_call);
        cb_reject_incoming_call = (CheckBox) findViewById(R.id.cb_reject_incoming_call);
        cb_show_running_notification = (CheckBox) findViewById(R.id.cb_show_running_notification);
        btn_test_email = (Button) findViewById(R.id.btn_test_email);
        cb_keep_screen_on = (CheckBox) findViewById(R.id.cb_keep_screen_on);
        btn_edit_config = (ToggleButton) findViewById(R.id.btn_edit_config);

        if (sdkBelow17) {
            tv_clock = findViewById(R.id.tv_clock);
            animator = ValueAnimator.ofInt(0, Integer.MAX_VALUE);
            animator.setDuration(Long.MAX_VALUE);
            animator.addUpdateListener(animation -> {
                if (!isFinishing()) {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
                    tv_clock.setText(format.format(System.currentTimeMillis()));
                } else {
                    animator.cancel();
                }
            });
            animator.start();
        }

        rv_mail_send_log.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmailAdapter(R.layout.item_mail, list);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setEnableLoadMore(true);
        LoadMoreView loadMoreView = new MailLoadMoreView();
        adapter.setLoadMoreView(loadMoreView);
        adapter.setOnLoadMoreListener(() -> getMailData(++page), rv_mail_send_log);
        rv_mail_send_log.setAdapter(adapter);
        adapter.disableLoadMoreIfNotFullPage();
        adapter.setOnItemClickListener((adapter, view, position) -> {
            MailEntity mailEntity = list.get(position);
            if (mailEntity != null) {
                showMessageDialog(mailEntity.getReceiver(), mailEntity.getContent());
            }
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            MailEntity mailEntity = list.get(position);
            if (mailEntity != null) {
                showConfirmDialog(getString(R.string.delete_this_log), "", getString(R.string.yes), (dialog, which) -> {
                    dialog.dismiss();
                    DataBaseService dataBaseService = new DataBaseService(activity);
                    dataBaseService.deleteMail(mailEntity);
                    adapter.remove(position);
                }, getString(R.string.no), (dialog, which) -> dialog.dismiss());
            }
            return true;
        });

        loadConfig();
        cb_show_running_notification.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.showRunningNotification = isChecked);
        cb_autentication.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.autenticationEnabled = isChecked);
        cb_content_send_via_subject.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.isContentInSubject = isChecked);
        cb_send_incoming_call.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.incomingCallMail = isChecked);
        cb_reject_incoming_call.setOnCheckedChangeListener((buttonView, isChecked) -> Constants.rejectIncomingCalls = isChecked);
        btn_edit_config.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Constants.HAVE_PERMISSION) {
                    Constants.started = isChecked;
                    if (Constants.showRunningNotification) {
                        if (Constants.started) showOnGoingNotification();
                        else hideOnGoingNotification();
                    }
                    ll_mail_send_log.setVisibility(Constants.started ? View.VISIBLE : View.GONE);
                    sv_config.setVisibility(Constants.started ? View.GONE : View.VISIBLE);
                    if (Constants.started) toast(getString(R.string.check_tip), Toast.LENGTH_LONG);
                    saveConfig();
                } else {
                    btn_edit_config.setOnCheckedChangeListener(null);
                    btn_edit_config.setChecked(false);
                    btn_edit_config.setOnCheckedChangeListener(this);
                    toast(getString(R.string.need_permission), Toast.LENGTH_LONG);
                    getPermission();
                }

                if (Constants.started) {
                    PingUtil.ping(Constants.serverHost, content -> runOnUiThread(new PingUIRunnable(content)));
                } else {
                    PingUtil.stopPing();
                }
            }
        });
        btn_test_email.setOnClickListener(v -> {
            saveConfig();
            MailSenderHelper.sendTestEmail();
        });
        tv_clear_log.setOnClickListener(v -> {
            showConfirmDialog("", getString(R.string.delete_all_log), getString(R.string.yes), (dialog, which) -> {
                dialog.dismiss();
                DataBaseService dataBaseService = new DataBaseService(activity);
                dataBaseService.deleteAllMail();
                list.clear();
                adapter.notifyDataSetChanged();
            }, getString(R.string.no), (dialog, which) -> dialog.dismiss());

        });
        cb_keep_screen_on.setOnCheckedChangeListener((buttonView, isChecked) ->
                getWindow().setFlags(isChecked ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        );
    }

    private class PingUIRunnable implements Runnable {
        String content;

        private PingUIRunnable(String content) {
            this.content = content;
        }

        @Override
        public void run() {
            String networkDelaysState;
            float time = 0;
            if (content.contains("avg")) {
                try {
                    String[] lines = content.trim().split("\n");
                    String[] rtt_names = lines[lines.length - 1].split("=")[0].trim().split("/");
                    String[] rtt_values = lines[lines.length - 1].split("=")[1].trim().split("/");
                    for (int i = 0; i < rtt_names.length; i++) {
                        if ("avg".equalsIgnoreCase(rtt_names[i])) {
                            time = Float.valueOf(rtt_values[i]);
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
//                networkDelaysState="time out";
            }
            networkDelaysState = time > 0 ? String.format(Locale.CHINA, "%dms", (int) time) : getString(R.string.time_out);
            tv_ping.setText(networkDelaysState);

            float percent = 0;
            if (time > 0) {
                percent = (1000 / time) * 5;
                if (percent > 100) percent = 100;
            }
            pb_signal.setProgress((int) percent);
            String signalTypeString;
            switch (signalType) {
                case SignalUtil.NETWORKTYPE_WIFI:
                    signalTypeString = "WIFI";
                    if (percent > 90) {
                        iv_signal.setImageResource(R.drawable.ic_signal_wifi_4_bar_24dp);
                    } else if (percent > 70) {
                        iv_signal.setImageResource(R.drawable.ic_signal_wifi_3_bar_24dp);
                    } else if (percent > 50) {
                        iv_signal.setImageResource(R.drawable.ic_signal_wifi_2_bar_24dp);
                    } else if (percent > 30) {
                        iv_signal.setImageResource(R.drawable.ic_signal_wifi_1_bar_24dp);
                    } else {
                        iv_signal.setImageResource(R.drawable.ic_signal_wifi_0_bar_24dp);
                    }
                    break;
                case SignalUtil.NETWORKTYPE_2G:
                case SignalUtil.NETWORKTYPE_4G:
                    signalTypeString = "CELL";
                    if (percent > 90) {
                        iv_signal.setImageResource(R.drawable.ic_signal_cellular_4_bar_24dp);
                    } else if (percent > 70) {
                        iv_signal.setImageResource(R.drawable.ic_signal_cellular_3_bar_24dp);
                    } else if (percent > 50) {
                        iv_signal.setImageResource(R.drawable.ic_signal_cellular_2_bar_24dp);
                    } else if (percent > 30) {
                        iv_signal.setImageResource(R.drawable.ic_signal_cellular_1_bar_24dp);
                    } else {
                        iv_signal.setImageResource(R.drawable.ic_signal_cellular_0_bar_24dp);
                    }
                    break;
                case SignalUtil.NETWORKTYPE_NONE:
                default:
                case -1:
                    signalTypeString = "NONE";
                    iv_signal.setImageResource(R.drawable.ic_signal_cellular_0_bar_24dp);
                    break;
            }
            Constants.networkState = signalTypeString + " " + networkDelaysState;
        }
    }

    private void showOnGoingNotification() {
        if (mNotificationManager == null) {
            return;
        }
        String channelId = getPackageName();
        String channelName = getString(R.string.app_name);
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity, channelId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder.setContentTitle(String.format(Locale.CHINA, getString(R.string.app_is_running), getString(R.string.app_name)))//设置通知栏标题
                .setContentText(String.format(Locale.CHINA, getString(R.string.app_running_tip), et_receiver_email.getText().toString())) //设置通知栏显示内容
                .setContentIntent(pendingIntent) //设置通知栏点击意图
                //  .setNumber(number) //设置通知集合的数量
                .setTicker(getString(R.string.app_started)) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_MIN) //设置该通知优先级
                .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.sms_rew);//设置通知小ICON
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void hideOnGoingNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }


    private void loadConfig() {
        Constants.loadConfigFromSP(activity);
        ll_mail_send_log.setVisibility(Constants.started ? View.VISIBLE : View.GONE);
        sv_config.setVisibility(Constants.started ? View.GONE : View.VISIBLE);

        et_sender_email.setText(Constants.senderEmail);
        et_sender_email_password.setText(Constants.senderEmailPassword);

        et_server_host.setText(String.valueOf(Constants.serverHost));
        et_server_port.setText(String.valueOf(Constants.serverPort));
        et_socket_factory_port.setText(String.valueOf(Constants.socketFactoryPort));
        cb_autentication.setChecked(Constants.autenticationEnabled);
        cb_show_running_notification.setChecked(Constants.showRunningNotification);

        et_receiver_email.setText(Constants.receiverEmail);
        btn_edit_config.setChecked(Constants.started);
        cb_content_send_via_subject.setChecked(Constants.isContentInSubject);
        cb_send_incoming_call.setChecked(Constants.incomingCallMail);
        cb_reject_incoming_call.setChecked(Constants.rejectIncomingCalls);
    }

    private void saveConfig() {
        Constants.senderEmail = et_sender_email.getText().toString().trim();
        Constants.senderEmailPassword = et_sender_email_password.getText().toString().trim();

        Constants.serverHost = et_server_host.getText().toString().trim();
        Constants.serverPort = Integer.parseInt(et_server_port.getText().toString().trim());
        Constants.socketFactoryPort = Integer.parseInt(et_socket_factory_port.getText().toString().trim());
        Constants.autenticationEnabled = cb_autentication.isChecked();

        Constants.receiverEmail = et_receiver_email.getText().toString().trim();
        Constants.started = btn_edit_config.isChecked();
        Constants.isContentInSubject = cb_content_send_via_subject.isChecked();
        Constants.incomingCallMail = cb_send_incoming_call.isChecked();
        Constants.rejectIncomingCalls = cb_reject_incoming_call.isChecked();
        Constants.showRunningNotification = cb_show_running_notification.isChecked();
        Constants.saveConfigToSP(activity);
    }
}
