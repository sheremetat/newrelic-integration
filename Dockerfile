FROM maven:3.3.3-jdk-7-onbuild

# Run the command on container startup
WORKDIR /usr/src/app/newrelic-report-standalone/
CMD ["mvn", "exec:java"]
