package ivxin.smsforward.lib.utils;

import android.util.Log;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import ivxin.smsforward.lib.entity.CommandEmail;
import ivxin.smsforward.lib.entity.ReceiveOneMail;


public class MailFetchUtil {
    private static String TAG = MailFetchUtil.class.getSimpleName();

    private String host = "imap.qq.com";
    private String username = "";
    private String password = "";

    public void setProperties(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * 以IMAP方式读取邮件，可以判定读取邮件是否为已读
     * http://www.cnblogs.com/zhujiabin/p/6295477.html
     */
    public CommandEmail fetchIMAPEmail() {
        CommandEmail commandEmail = new CommandEmail();
        Properties prop = System.getProperties();
        prop.put("mail.store.protocol", "imap");
        prop.put("mail.imap.host", host);

        Session session = Session.getInstance(prop);

        IMAPStore store;
        try {
            store = (IMAPStore) session.getStore("imap"); // 使用imap会话机制，连接服务器

            store.connect(username, password);

            IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            if (messages.length > 0) {
                try {
                    Message lastMessage = messages[messages.length - 1];
                    // 获得邮件内容===============
                    ReceiveOneMail pmm = new ReceiveOneMail((MimeMessage) lastMessage);
                    pmm.setDateFormat("yyyy-MM-dd HH:mm:ss");
                    pmm.getMailContent(lastMessage);
                    commandEmail.setMessageId(pmm.getMessageId());
                    commandEmail.setFrom(pmm.getFrom());
                    commandEmail.setTo(pmm.getMailAddress("to"));
                    commandEmail.setSendDate(pmm.getSentDate());
                    commandEmail.setSubject(pmm.getSubject());
                    commandEmail.setContent(pmm.getBodyText());
                    Log.d(TAG, "fetchIMAPEmail: commandEmail:" + commandEmail.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            folder.close(false);
            store.close();
        } catch (javax.mail.NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }
        return commandEmail;

    }
}
