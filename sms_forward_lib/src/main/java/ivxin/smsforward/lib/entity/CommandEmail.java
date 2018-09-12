package ivxin.smsforward.lib.entity;

import java.io.Serializable;

public class CommandEmail implements Serializable {
    private String messageId;
    private String from;
    private String to;
    private String sendDate;
    private String subject;
    private String content;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("messageId:").append(messageId).append("\n");
        stringBuilder.append("from:").append(from).append("\n");
        stringBuilder.append("to:").append(to).append("\n");
        stringBuilder.append("sendDate:").append(sendDate).append("\n");
        stringBuilder.append("subject:").append(subject).append("\n");
        stringBuilder.append("messageId:").append(messageId).append("\n");
        stringBuilder.append("content:").append(content).append("\n");
        return stringBuilder.toString();
    }
}
