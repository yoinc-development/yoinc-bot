package ch.yoinc.services;

import ch.yoinc.http.Connection;
import ch.yoinc.http.ConnectionBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseService {

    protected static Connection sharedConnection;
    protected final Properties properties;
    protected final Connection connection;

    public BaseService() {
        this.properties = new Properties();
        try {
            InputStream inputStream = BaseService.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (sharedConnection == null) {
            sharedConnection = new ConnectionBuilder()
                    .bungieSettings(properties.getProperty("bungie.api"), properties.getProperty("bungie.key"))
                    .internalSettings(properties.getProperty("dashboard.api"))
                    .build();
        }
        this.connection = sharedConnection;
    }
}
