package ch.yoinc.services;

import ch.yoinc.models.InternalUser;

import java.util.List;

public class DataService extends BaseService {

    public DataService() {
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
