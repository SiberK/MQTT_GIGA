package accountmanagerlib;

import java.io.Serializable;

// Account.java
public class Account implements Serializable {
    private String server;
    private String port;
    private String login;
    private String password;

    public Account(String server, String port, String login, String password) {
        this.server = server;
        this.port = port;
        this.login = login;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }

    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return server + " : " + port + " (" + login + ")";
    }
}