package ch.yoinc.services;

import ch.yoinc.http.Connection;
import ch.yoinc.http.ConnectionBuilder;

import java.util.Properties;

public class BaseService {

    protected static Connection sharedConnection;
    protected final Properties properties;
    protected final Connection connection;

    public BaseService(Properties properties) {
        this.properties = properties;
        if (sharedConnection == null) {
            sharedConnection = new ConnectionBuilder()
                    .bungieSettings(properties.getProperty("bungie.api"), properties.getProperty("bungie.key"))
                    .internalSettings(properties.getProperty("dashboard.api"))
                    .build();
        }
        this.connection = sharedConnection;
    }
}
