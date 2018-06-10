package ivxin.smsforward.mine.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ivxin.smsforward.mine.Constants;
import ivxin.smsforward.mine.entity.MailEntity;
import ivxin.smsforward.mine.entity.SMSEntity;

public class MailSenderHelper {
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static OnMailSentCallback onMailSentCallback;

    public static void sendEmail(SMSEntity sms) {
        if (Constants.started)
            singleThreadExecutor.execute(new EmailSendTask(sms));
    }

    public static void sendTestEmail() {
        SMSEntity smsEntity = new SMSEntity();
        smsEntity.setContent("TEST EMAIL");
        smsEntity.setSender("[SENDER]");
        smsEntity.setReceivedTime(System.currentTimeMillis());
        smsEntity.setSendTime(System.currentTimeMillis());
        singleThreadExecutor.execute(new EmailSendTask(smsEntity));
    }


    private static class EmailSendTask implements Runnable {
        private SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERN, Locale.CHINA);
        private SMSEntity sms;

        EmailSendTask(SMSEntity sms) {
            this.sms = sms;
        }

        @Override
        public void run() {
            try {
                MailSender mailSender = new MailSender();
                mailSender.useMailPropertiesSNMP(
                        Constants.serverHost,
                        Constants.serverPort,
                        Constants.socketFactoryPort,
                        Constants.autenticationEnabled);
                mailSender.setCredentials(Constants.senderEmail, Constants.senderEmailPassword);
                mailSender.setToAddresses(new String[]{Constants.receiverEmail});
                String mailText = String.format(Locale.CHINA,
                        "%s\n\n" +
                                "SMS From:%s\n" +
                                "SMS ReceiverCard:Card %d\n" +
                                "SMS ReceiveTime:%s\n" +
                                "Mail SendTime:%s\n" +
                                "Device:%s\n",
                        sms.getContent(),
                        sms.getSender(),
                        sms.getReceiverCard(),
                        sdf.format(sms.getReceivedTime()),
                        sdf.format(sms.getSendTime()),
                        android.os.Build.BRAND + " " + android.os.Build.MODEL);
                String subject = Constants.isContentInSubject ? sms.getContent() : String.format(Locale.CHINA, "[SMS Forward] From:%s\n", sms.getSender());

                mailSender.setSubject(subject);
                if (mailText.contains("check")) {
                    String deviceState = String.format("\nBattery:%s\nCharging:%s\nNetwork:%s\n", Constants.battery_level, Constants.isCharging, Constants.networkState);
                    mailText = mailText.concat(deviceState);
                }
                mailSender.setMailText(mailText);
                mailSender.send();
                if (onMailSentCallback != null) {
                    MailEntity mailEntity = new MailEntity();
                    mailEntity.setSendTime(System.currentTimeMillis());
                    mailEntity.setContent(mailText);
                    mailEntity.setSubject(subject);
                    mailEntity.setReceiver(Constants.receiverEmail);
                    onMailSentCallback.onSuccess(mailEntity);
                }
            } catch (Exception e) {
                if (onMailSentCallback != null) {
                    onMailSentCallback.onFail(e);
                }
                e.printStackTrace();
            }
        }
    }

    public static void setOnMailSentCallback(OnMailSentCallback onMailSentCallback) {
        MailSenderHelper.onMailSentCallback = onMailSentCallback;
    }

    public interface OnMailSentCallback {
        /**
         * not ui thread
         *
         * @param mailEntity was sent
         */
        void onSuccess(MailEntity mailEntity);

        /**
         * not ui thread
         *
         * @param e is an error
         */
        void onFail(Exception e);
    }
}
