/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.signalfx.newrelic.client.cache.CacheService;
import com.signalfx.newrelic.client.cache.Principal;
import org.json.JSONArray;
import org.json.JSONObject;

import com.signalfx.newrelic.client.exception.RequestException;
import com.signalfx.newrelic.client.exception.UnauthorizedException;
import com.signalfx.newrelic.client.model.MetricData;
import com.signalfx.newrelic.client.model.MetricValue;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * MetricDataRequest performs request to NewRelic REST API to retrieve metrics data.
 * <p>
 * NewRelic API is documented <a href="https://rpm.newrelic.com/api/explore">here</a>.
 */
public class MetricDataRequest {

    /**
     * NewRelic host URL
     */
    private final String newRelicURL;

    /**
     * NewRelic API token
     */
    private final String newRelicApiToken;

    /**
     * NewRelic application name
     */
    private String appName;

    /**
     * Time parameters to query the data.
     */
    private TimeParams timeParams;

    public MetricDataRequest(String url, String apiToken) {
        this.newRelicURL = url;
        this.newRelicApiToken = apiToken;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setTimeParams(TimeParams timeParams) {
        this.timeParams = timeParams;
    }

    /**
     * Perform retrieval of metrics from NewRelic using specified parameters.
     *
     * @return list of metric data.
     * @throws RequestException      when there was an error with request.
     * @throws UnauthorizedException when unable to authorize with given credentials.
     */
    public List<MetricData> get() throws RequestException, UnauthorizedException {
        List<MetricData> metricData = new ArrayList<>();

        CacheService cache = CacheService.create();
        if (cache.isExpired(this.appName)) {
            cache.updateCache(this.newRelicURL, this.appName, this.newRelicApiToken);
        }

        List<Principal> principals = cache.readPrincipals(appName);
        for (Principal principal : principals) {
            HttpResponse<String> response;
            try {
                response = Unirest.get(
                        this.newRelicURL + "/" + principal.getType() + "/" + principal.getId() + "/metrics/data.json")
                        .header("X-Api-Key", this.newRelicApiToken)
                        .queryString(getQueryString())
                        .queryString("output", "json")
                        .queryString("names[]", principal.getMetrics())
                        .asString();
            } catch (UnirestException e) {
                throw new RequestException("Something was wrong with sending request.", e);
            }
            if (response == null) {
                throw new RequestException("Response is empty.");
            }
            switch (response.getStatus()) {
                case 200: {
                    metricData.addAll(process(new JsonNode(response.getBody()), principal));
                    continue;
                }
                case 401: {
                    throw new UnauthorizedException("Authentication failed");
                }
                default: {
                    throw new RequestException("Unhandled response code " + response.getStatus());
                }
            }
        }
        return metricData;
    }

    /**
     * Generate querystring for the request.
     *
     * @return map of query strings.
     */
    protected Map<String, Object> getQueryString() {
        Map<String, Object> qs = new HashMap<>();

        if (timeParams != null) {
            if (timeParams.startTime > 0) {
                qs.put("from", getQueryDateString(timeParams.startTime));
            }
            if (timeParams.endTime > 0) {
                qs.put("to", getQueryDateString(timeParams.endTime));
            }
        }
        return qs;
    }

    private String getQueryDateString(long dateTime) {
        //2015-10-30T14:17:00+00:00
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String out = format.format(dateTime);

        return out.substring(0, 21).concat(":").concat(out.substring(22));
    }

    /**
     * Process the JSON response from the request.
     *
     * @param node root node of JSON response.
     * @return list of {@link MetricData}
     */
    protected List<MetricData> process(JsonNode node, Principal principal) {
        JSONObject jsonObject = node.getObject();
        JSONObject metricsDataListObject = jsonObject.getJSONObject("metric_data");


        JSONArray dataArray = metricsDataListObject.getJSONArray("metrics");

        List<MetricData> list = new LinkedList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            String metricNamePref = data.getString("name");

            JSONArray timeSlices = data.getJSONArray("timeslices");

            for (int j = 0; j < timeSlices.length(); j++) {

                JSONObject timeSlice = timeSlices.getJSONObject(j);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                long timeStamp = new Date().getTime(); //timeSlice.getString("to");
                try {
                    Date result = df.parse(timeSlice.getString("to"));
                    timeStamp = result.getTime(); //timeSlice.getString("to");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONObject values = timeSlice.getJSONObject("values");
                for (int k = 0; k < values.length(); k++) {

                    MetricData metricData =
                            new MetricData(metricNamePref + "/" + values.names().getString(k));
                    metricData.dimensions.put("type", principal.getType());
                    metricData.dimensions.put(principal.getType().toLowerCase() + "_id", principal.getId());

                    Object value = values.get(values.names().getString(k));
                    if (value != JSONObject.NULL) {
                        metricData.metricValues.add(
                                new MetricValue(values.getDouble(values.names().getString(k)), timeStamp));
                        list.add(metricData);
                    }
                }

            }
        }

        return list;
    }

    /**
     * TimeParams represent time parameters in querystring.
     */
    public static class TimeParams {
        private final String type;
        private final long duration;
        private final long startTime;
        private final long endTime;

        /**
         * @param duration duration (in minutes) to return the metric data.
         * @return {@link com.signalfx.newrelic.client.MetricDataRequest.TimeParams}
         */
        public static TimeParams beforeNow(long duration) {
            long timeEnd = System.currentTimeMillis();
            long timeStart = timeEnd - duration * 60 * 1000;
            return new TimeParams("BEFORE_NOW", duration, timeStart, timeEnd);
        }

        /**
         * @param duration duration (in minutes) to return the metric data.
         * @param endTime  end time (in milliseconds) until which the metric data is returned in UNIX epoch
         *                 time.
         * @return {@link com.signalfx.newrelic.client.MetricDataRequest.TimeParams}
         */
        public static TimeParams beforeTime(long duration, long endTime) {
            return new TimeParams("BEFORE_TIME", duration, 0, endTime);
        }

        /**
         * @param duration  duration (in minutes) to return the metric data.
         * @param startTime start time (in milliseconds) from which the metric data is returned in UNIX epoch
         *                  time.
         * @return {@link com.signalfx.newrelic.client.MetricDataRequest.TimeParams}
         */
        public static TimeParams afterTime(long duration, long startTime) {
            return new TimeParams("AFTER_TIME", duration, startTime, 0);
        }

        /**
         * @param startTime start time (in milliseconds) from which the metric data is returned in UNIX epoch
         *                  time.
         * @param endTime   end time (in milliseconds) until which the metric data is returned in UNIX epoch
         *                  time.
         * @return {@link com.signalfx.newrelic.client.MetricDataRequest.TimeParams}
         */
        public static TimeParams betweenTime(long startTime, long endTime) {
            return new TimeParams("BETWEEN_TIMES", 0, startTime, endTime);
        }

        protected TimeParams(String type, long duration, long startTime, long endTime) {
            this.type = type;
            this.duration = duration;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public boolean equals(Object that) {
            return that instanceof TimeParams && equals((TimeParams) that);
        }

        public boolean equals(TimeParams that) {
            return (this.type == null ? that.type == null : this.type.equals(that.type)) &&
                    this.duration == that.duration && this.startTime == that.startTime &&
                    this.endTime == that.endTime;
        }

        @Override
        public String toString() {
            return String.format("Type: %s, Duration: %d, StartTime: %d, EndTime: %d",
                    type, duration, startTime, endTime);
        }
    }
}
