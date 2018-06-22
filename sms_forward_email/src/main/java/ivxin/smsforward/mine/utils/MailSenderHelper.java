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
    private static SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERN, Locale.CHINA);

    public static void sendEmail(SMSEntity sms) {
        if (Constants.started) {
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
            if (mailText.contains("check")) {
                String deviceState = String.format("\nBattery:%s\nCharging:%s\nNetwork:%s\n", Constants.battery_level, Constants.isCharging, Constants.networkState);
                mailText = mailText.concat(deviceState);
            }
            MailEntity mailEntity = new MailEntity();
            mailEntity.setSendTime(System.currentTimeMillis());
            mailEntity.setContent(mailText);
            mailEntity.setSubject(subject);
            mailEntity.setReceiver(Constants.receiverEmail);
            sendEmail(mailEntity);
        }
    }

    public static void sendEmail(MailEntity mailEntity) {
        singleThreadExecutor.execute(new EmailSendTask(mailEntity));
    }

    public static void sendTestEmail() {
        String mailText = "TEST MAIL CONTENT";
        String deviceState = String.format("\nBattery:%s\nCharging:%s\nNetwork:%s\n", Constants.battery_level, Constants.isCharging, Constants.networkState);
        mailText = mailText.concat(deviceState);
        MailEntity mailEntity = new MailEntity();
        mailEntity.setSendTime(System.currentTimeMillis());
        mailEntity.setSubject("TEST MAIL SUBJECT");
        mailEntity.setContent(mailText);
        mailEntity.setReceiver(Constants.receiverEmail);
        singleThreadExecutor.execute(new EmailSendTask(mailEntity));
    }


    private static class EmailSendTask implements Runnable {
        private MailEntity mailEntity;

        EmailSendTask(MailEntity mailEntity) {
            this.mailEntity = mailEntity;
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
                mailSender.setSubject(mailEntity.getSubject());
                mailSender.setMailText(mailEntity.getContent());
                mailSender.send();
                if (onMailSentCallback != null) {
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
