/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Database class. Provides interface to persist and retrieval Principles
 * This class is singleton
 */
public class Database {
    private static Database INSTANCE;

    public static Database getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Database();

        return INSTANCE;
    }

    private HashMap<String, HashMap<String, Principal>> db;

    private Database() {
        db = new HashMap<>();
    }

    /**
     * Persist principal to DB.
     * Create group record if group record does not exists
     *
     * @param principal - Principal to save
     */
    public void createPrincipal(Principal principal) {
        if (!db.containsKey(principal.getType())) {
            db.put(principal.getType(), new HashMap<String, Principal>());
        }
        HashMap<String, Principal> principalHashMap = db.get(principal.getType());

        if(!principalHashMap.containsKey(principal.getId())){
            principalHashMap.put(principal.getId(), principal);
        } else {
            System.out.println("Principal with name [" + principal.getId() + "] already exists");
        }
    }

    /**
     * Add metric names to selected proncipal
     *
     * @param principal - Principal obj
     * @param metricNames - metric names to add to principal obj
     */
    public void refreshMetricsForPrincipal(Principal principal, List<String> metricNames) {
        HashMap<String, Principal> principals = db.get(principal.getType());
        if (principals != null) {
            Principal principalToUpdate = principals.get(principal.getId());
            principalToUpdate.updateMetrics(metricNames);
            System.out.println("metricNames: " + metricNames);
        } else {
            System.out.println("No persisted principal with type: " + principal.getType());
        }
    }

    /**
     * Read principals from database by type
     *
     * @param type - principle type
     * @return List of Principles or empty list
     */
    public List<Principal> readPrincipals(String type) {
        HashMap<String, Principal> principals = db.get(type);
        if (principals != null) {
            return new ArrayList<>(principals.values());
        } else {
            System.out.println("No principals with type [" + type + "] in database");
            return Collections.emptyList();
        }
    }
}
