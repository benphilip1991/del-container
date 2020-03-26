package com.del.delcontainer.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Data handler to manage data storage/retrieval from
 * the database.
 *
 * This file interfaces the mini apps to the stored data
 * and allows services to store data in the db.
 */
public class DataManager {

    // TODO: Need a scheduler to go through the mapped requests and deliver
    private static HashMap<String, ArrayList<String>> dataRequestMap;
    private static DataManager dataManager = new DataManager();

    private DataManager() {
        dataRequestMap = new HashMap<>();
    }

    public static DataManager getInstance() {
        return dataManager;
    }

    public HashMap<String, ArrayList<String>> getDataRequestMap() {
        return dataRequestMap;
    }
}
