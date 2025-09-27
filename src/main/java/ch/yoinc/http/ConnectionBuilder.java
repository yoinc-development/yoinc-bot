package ch.yoinc.http;

public class ConnectionBuilder {
    public String internalUrl;
    public String bungieUrl;
    protected String bungieKey;

    public ConnectionBuilder bungieSettings(String bungieUrl, String bungieKey) {
        this.bungieUrl = bungieUrl;
        this.bungieKey = bungieKey;
        return this;
    }

    public ConnectionBuilder internalSettings(String internalUrl) {
        this.internalUrl = internalUrl;
        return this;
    }

    public Connection build() {
        Connection connection = new Connection();
        connection.bungieUrl = this.bungieUrl;
        connection.bungieKey = this.bungieKey;
        connection.internalUrl = this.internalUrl;
        return connection;
    }
}
