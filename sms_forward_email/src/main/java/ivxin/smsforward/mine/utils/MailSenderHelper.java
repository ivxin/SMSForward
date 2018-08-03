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
                    "%s" + Constants.BR + Constants.BR +
                            "短信发信人：%s" + Constants.BR +
                            "短信接收卡：Card %d" + Constants.BR +
                            "短信接收时间：%s" + Constants.BR +
                            "此邮件发送时间：%s" + Constants.BR,
                    sms.getContent(),
                    sms.getSender(),
                    sms.getReceiverCard(),
                    sdf.format(sms.getReceivedTime()),
                    sdf.format(sms.getSendTime()));
            String subject = Constants.isContentInSubject ? sms.getContent() : String.format(Locale.CHINA, "[短信转发] From:%s" + Constants.BR, sms.getSender());
            mailText = mailText.concat(Constants.getDeviceState());

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
        String mailText = "[短信转发]测试邮件内容";
        mailText = mailText.concat(Constants.getDeviceState());
        MailEntity mailEntity = new MailEntity();
        mailEntity.setSendTime(System.currentTimeMillis());
        mailEntity.setSubject("[短信转发]测试邮件标题");
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
