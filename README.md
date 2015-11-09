# SignalFx NewRelic client agent [![Build Status](https://travis-ci.org/signalfx/newrelic-integration.svg)](https://travis-ci.org/signalfx/newrelic-integration)

This repository contains agent and libraries for retrieving and reporting NewRelic metrics
to SignalFx. You will need NewRelic API token and host information as well as 
SignalFx account and organization API token to report the data.

## Supported Languages

* Java 7+

## Sending metrics

newrelic-report-standalone module is a standalone process that parses configurations and report
NewRelic metric every specified intervals.

To run
```
maven install
cd newrelic-report-standalone
maven exec:java
```

or you can use Dockerfile to build and run NewRelic client agent

### Configurations

#### Environment Variables

Required
```
NEWRELIC_API_KEY=<New relic api key>
NEWRELIC_HOST=<https://NewRelic Host>
SIGNALFX_TOKEN=<SignalFx token>
```

Optional
```
SIGNALFX_NEWRELIC_METRICS=<metric configurations filename (default to metrics.json)>
NEWRELIC_INTERVAL=<time in minutes of metric lookup interval (default to 1 minute)>
```

#### Metrics.json

Metrics.json contains configurations for list of top level API groups.
Available groups are: servers, applications, browser_applications and mobile_applications
      
Default metrics.json is provided with Application Infrastructure Performance metrics configured.


### Process Status Metrics

newrelic-report-standalone also reports metrics pertaining to the syncing process to SignalFx.

That includes:
- mtsReported
- mtsEmpty
- dataPointsReported
- newRelicRequestFailure