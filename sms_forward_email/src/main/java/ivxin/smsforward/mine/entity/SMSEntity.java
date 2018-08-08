package ivxin.smsforward.mine.entity;

public class SMSEntity {
    private String sender;
    private String content;
    private long sendTime;
    private long receivedTime;
    private int receiverCard;
    private String  receiverCardName;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public int getReceiverCard() {
        return receiverCard;
    }

    public void setReceiverCard(int receiverCard) {
        this.receiverCard = receiverCard;
    }

    public String getReceiverCardName() {
        return receiverCardName;
    }

    public void setReceiverCardName(String receiverCardName) {
        this.receiverCardName = receiverCardName;
    }
}
