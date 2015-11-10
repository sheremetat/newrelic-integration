/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client.cache;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.signalfx.newrelic.client.exception.RequestException;
import com.signalfx.newrelic.client.exception.UnauthorizedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Database service class. Provides interface to work with persisted pronciples
 * This class is singleton
 */
public class CacheService {
    private static CacheService INSTANCE;

    public static CacheService create() {
        if (INSTANCE == null)
            INSTANCE = new CacheService();

        return INSTANCE;
    }

    /**
     * Time interval to update metric name in database
     */
    public static final long CACHE_EXPIRED_INTERVAL = 24 * 60 * 60 * 1000; // time in ms

    private HashMap<String, Long> LAST_UPDATE_TIME = new HashMap<>();

    private CacheService() {
    }

    private void updateLastUpdateTime(String type, Long lastUpdate){
            LAST_UPDATE_TIME.put(type, lastUpdate);
    }

    public boolean isExpired(String type) {
        long lastUpdate = 0L;
        if(LAST_UPDATE_TIME.containsKey(type)){
            lastUpdate = LAST_UPDATE_TIME.get(type);
        }
        long now = new Date().getTime();
        return now - lastUpdate > CACHE_EXPIRED_INTERVAL;
    }

    /**
     * Update cached principals or, if principal does now exists - create it
     * This method will do request to NewRelic and load list of principal IDs
     *
     * @param newRelicUrl - base NewRelic URL
     * @param type - Type of metric group (servers, applications, browser_applications, etc)
     * @param newRelicApiToken - NewRelic API token
     */
    public void updateCache(String newRelicUrl, String type, String newRelicApiToken) throws RequestException, UnauthorizedException {
        HttpResponse<String> response;
        try {
            response = Unirest.get(
                    newRelicUrl + "/" + type + ".json")
                    .header("X-Api-Key", newRelicApiToken)
                    .queryString("output", "json")
                    .asString();
        } catch (UnirestException e) {
            throw new RequestException("Something was wrong with sending request.", e);
        }
        if (response == null) {
            throw new RequestException("Response is empty.");
        } else {
            switch (response.getStatus()) {
                case 200: {
                    JsonNode jsonNode = new JsonNode(response.getBody());
                    JSONObject dataObject = jsonNode.getObject();
                    JSONArray dataArray = dataObject.getJSONArray(type);
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject data = dataArray.getJSONObject(i);
                        int id = data.getInt("id");
                        Principal principal = new Principal(type, String.valueOf(id));
                        createPrincipal(principal);

                        loadMetricNames(newRelicUrl, newRelicApiToken, principal);
                    }
                    updateLastUpdateTime(type, System.currentTimeMillis());
                    return;
                }
                case 401: {
                    throw new UnauthorizedException("Authentication failed");
                }
                default: {
                    throw new RequestException("Unhandled response code " + response.getStatus());
                }
            }
        }
    }

    /**
     * Update or create metric name list for principal
     * This method will do request to NewRelic and load list of principal IDs
     *
     * @param newRelicUrl - base NewRelic URL
     * @param principal - Principal to update
     * @param newRelicApiToken - NewRelic API token
     */
    private void loadMetricNames(String newRelicUrl, String newRelicApiToken, Principal principal)  throws RequestException, UnauthorizedException {
        HttpResponse<String> response = null;
        try {
            response = Unirest.get(
                    newRelicUrl + "/" + principal.getType() + "/" + principal.getId() + "/metrics.json")
                    .header("X-Api-Key", newRelicApiToken)
                    .queryString("output", "json")
                    .asString();
        } catch (UnirestException e) {
            throw new RequestException("Something was wrong with sending request.", e);
        }
        if (response == null) {
            throw new RequestException("Response is empty.");
        } else {
            switch (response.getStatus()) {
                case 200: {
                    JsonNode jsonNode = new JsonNode(response.getBody());
                    JSONObject dataObject = jsonNode.getObject();
                    JSONArray dataArray = dataObject.getJSONArray("metrics");

                    List<String> metrics = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject data = dataArray.getJSONObject(i);
                        String metricName = data.getString("name");
                        metrics.add(metricName);
                    }
                    System.out.println("metrics: " + metrics);
                    refreshMetricsForPrincipal(principal, metrics);
                    return;
                }
                case 401: {
                    throw new UnauthorizedException("Authentication failed");
                }
                default: {
                    throw new RequestException("Unhandled response code " + response.getStatus());
                }
            }
        }
    }

    public void createPrincipal(Principal principal) {
        Database.getInstance().createPrincipal(principal);
    }

    public void refreshMetricsForPrincipal(Principal principal, List<String> metricNames) {
        Database.getInstance().refreshMetricsForPrincipal(principal, metricNames);
    }

    public List<Principal> readPrincipals(String type) {
        return Database.getInstance().readPrincipals(type);
    }


}
