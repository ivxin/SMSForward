package ivxin.smsforward.entity;

import java.io.Serializable;

public class EmailSenderConfig implements Serializable {

    private String serverHost = "";
    private int serverPort;
    private int socketFactoryPort;
    private boolean autenticationEnabled;

    private String usermail = "";
    private String password = "";

    public EmailSenderConfig() {
    }

    public EmailSenderConfig(String serverHost, int serverPort, int socketFactoryPort, boolean autenticationEnabled, String usermail, String password) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.socketFactoryPort = socketFactoryPort;
        this.autenticationEnabled = autenticationEnabled;
        this.usermail = usermail;
        this.password = password;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getSocketFactoryPort() {
        return socketFactoryPort;
    }

    public void setSocketFactoryPort(int socketFactoryPort) {
        this.socketFactoryPort = socketFactoryPort;
    }

    public boolean isAutenticationEnabled() {
        return autenticationEnabled;
    }

    public void setAutenticationEnabled(boolean autenticationEnabled) {
        this.autenticationEnabled = autenticationEnabled;
    }

    public String getUsermail() {
        return usermail;
    }

    public void setUsermail(String usermail) {
        this.usermail = usermail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
