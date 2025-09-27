package ch.yoinc.services;

import ch.yoinc.models.InternalUser;

import java.util.List;
import java.util.Properties;

public class DataService extends BaseService {

    public DataService(Properties properties) {
        super(properties);
    }

    /**
     * Returns all Destiny users.
     *
     * @return List of InternalUser objects
     */
    public List<InternalUser> getAllDestinyUsers() {
        return connection.getAllDestinyUsers();
    }
}
