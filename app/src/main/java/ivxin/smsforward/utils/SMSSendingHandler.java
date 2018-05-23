package ivxin.smsforward.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.smsforward.Constants;
import ivxin.smsforward.db.DBService;
import ivxin.smsforward.entity.EmailSenderConfig;
import ivxin.smsforward.entity.SMSEntity;

public class SMSSendingHandler {
    private EmailSenderConfig emailSenderConfig;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private Context context;
    private SMSEntity newSms;
    private boolean smsSended = false;
    private static final int FINISHED = 3;
    private MyHandler handler = new MyHandler();
    private String numRex;
    private String rex;
    private String target;
    private String email_target;
    private boolean is_save_sms;
    private boolean is_save_forward_only;

    private boolean is_sms_forward;
    private boolean is_email_forward;

    public SMSSendingHandler(Context context, SMSEntity newSms) {
        this.context = context;
        this.newSms = newSms;
    }

    public void start() {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
        if (!sp.getBoolean(Constants.STARTED_KEY, false)) {
            return;
        }
        numRex = sp.getString(Constants.NUM_REX_KEY, "").trim();
        rex = sp.getString(Constants.REX_KEY, "").trim();
        target = sp.getString(Constants.TARGET_KEY, "").trim();
        email_target = sp.getString(Constants.EMAIL_TARGET_KEY, "").trim();
        is_save_sms = sp.getBoolean(Constants.SAVE_SMS_KEY, false);
        is_save_forward_only = sp.getBoolean(Constants.SAVE_FORWARD_ONLY_KEY, false);
        is_sms_forward = sp.getBoolean(Constants.SMS_FORWARD, false);
        is_email_forward = sp.getBoolean(Constants.EMAIL_FORWARD, false);

        if (is_email_forward && emailSenderConfig == null) {
            ObjectSerializationUtil.getInstance(context, object -> emailSenderConfig = (EmailSenderConfig) object).getObject(Constants.EmailSenderConfig_FILE_NAME);
        }

        new MyThread().start();
    }

    /**
     * 处理和发送消息
     */
    private void handleSMS() {
        if (newSms == null) {
            return;
        }
        String smsContent = newSms.getContent();
        String smsAddress = newSms.getSender();
//        boolean targetGood = false;
        boolean numRexGood = false;
        boolean rexGood = false;
//        // 判断要发送目标的号码可用
//        if (!TextUtils.isEmpty(target) && target.replaceAll("[0-9]", "").length() == 0) {
//            targetGood = true;
//        }
        // 判断符合收信号码规则
        String[] numRexs = numRex.split(";");
        if (numRexs.length == 0) {
            if (TextUtils.isEmpty(numRex) || smsAddress.contains(numRex)) {
                numRexGood = true;
            }
        } else {
            for (String numRex1 : numRexs) {
                if (smsAddress.contains(numRex1.trim())) {
                    numRexGood = true;
                }
            }
        }
        // 判断符合内容过滤规则
        String[] rexs = rex.split(";");
        if (rexs.length == 0) {
            if (TextUtils.isEmpty(rex) || smsContent.contains(rex)) {
                rexGood = true;
            }
        } else {
            for (String rex1 : rexs) {
                if (smsContent.contains(rex1.trim())) {
                    rexGood = true;
                }
            }
        }

        // 条件都满足时发信息
        if (numRexGood && rexGood
//                && targetGood
                ) {
            String[] targets = target.split(";");
            String[] email_targets = email_target.split(";");
            if (is_sms_forward && targets.length > 0) {
                for (String tar : targets) {
                    tar = tar.trim().replaceAll("\\s*", "");
                    singleThreadExecutor.execute(new SMSSendTask(newSms, tar));
                }
            }
            if (is_email_forward && email_targets.length > 0) {
                singleThreadExecutor.execute(new EmailSendTask(newSms, email_targets));
            }

            smsSended = true;
        } else {
            smsSended = false;
        }
        // sms保存在DB
        newSms.setReceiver_email(email_target);
        newSms.setReceiver(target);
        newSms.setForwarded(smsSended);
        newSms.setStar("0");
        newSms.setSendTime(System.currentTimeMillis());
        if (is_save_sms) {
            if (is_save_forward_only) {
                if (smsSended) {
                    DBService dbs = new DBService(context);
                    dbs.insertSMS(newSms);
                }
            } else {
                DBService dbs = new DBService(context);
                dbs.insertSMS(newSms);
            }
        }
    }

    private class EmailSendTask implements Runnable {
        SMSEntity sms;
        String[] targets;

        EmailSendTask(SMSEntity sms, String[] email_targets) {
            this.sms = sms;
            this.targets = email_targets;
        }

        @Override
        public void run() {
            MailSender mailSender = new MailSender();
            mailSender.useMailPropertiesSNMP(emailSenderConfig.getServerHost(), emailSenderConfig.getServerPort(), emailSenderConfig.getSocketFactoryPort(), emailSenderConfig.isAutenticationEnabled());
            mailSender.setCredentials(emailSenderConfig.getUsermail(), emailSenderConfig.getPassword());
            mailSender.setToAddresses(targets);
            mailSender.setSubject(String.format(Locale.CHINA, "[短信转发] 来自：%s", sms.getSender()));
            mailSender.setMailText(String.format(Locale.CHINA,
                    "%s\n" +
                            "来自：%s\n" +
                            "接收时间：%s\n" +
                            "转发时间：%s",
                    sms.getContent(),
                    sms.getSender(),
                    StringUtils.getDateFomated(Constants.PATTERN, sms.getReceivedTime()),
                    StringUtils.getDateFomated(Constants.PATTERN, sms.getSendTime())));
            try {
                mailSender.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class SMSSendTask implements Runnable {

        SMSEntity sms;
        String t;

        SMSSendTask(SMSEntity sms, String t) {
            this.sms = sms;
            this.t = t;
        }

        @Override
        public void run() {
            if (!TextUtils.isEmpty(t) && t.replaceAll("[0-9]", "").length() == 0) {
                SmsManager manager = SmsManager.getDefault();
                String str = sms.getContent() + "\n来自:" + sms.getSender() + "\n时间:"
                        + StringUtils.getDateFomated(Constants.PATTERN, sms.getReceivedTime());
                ArrayList<String> strs = manager.divideMessage(str);// 分割短信
                manager.sendMultipartTextMessage(t, null, strs, null, null);// 分割发送
                System.out.println(str);
                System.out.println(t);
            }
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            handleSMS();
            handler.sendEmptyMessage(FINISHED);
        }
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISHED:
                    if (smsSended) {
                        Toast.makeText(context, "转发来自:" + newSms.getSender() + "的短信成功", Toast.LENGTH_SHORT).show();
                        smsSended = false;
                        soundAlert();
                    }
                    Intent intent = new Intent(Constants.ACTION);
                    context.sendBroadcast(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void soundAlert() {
        final MediaPlayer player = new MediaPlayer();
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            player.setDataSource(context, alert);
            player.prepare();
            player.start();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(mp -> player.release());
    }

}
