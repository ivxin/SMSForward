package ivxin.smsforward.entity;

/**
 * Created by yaping.wang on 2017/9/15.
 */

public class SMSEntity extends DisplayableEntity {
    private String id;
    private String sender;
    private String receiver;
    private String title;
    private String content;
    private long sendTime;
    private long receivedTime;
    private boolean isForwarded;
    private String isStar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public boolean isForwarded() {
        return isForwarded;
    }

    public void setForwarded(boolean forwarded) {
        isForwarded = forwarded;
    }

    public String isStar() {
        return isStar;
    }

    public void setStar(String star) {
        isStar = star;
    }
}
