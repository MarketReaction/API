FROM hypriot/rpi-java:1.8.0
EXPOSE 8080
ENTRYPOINT ["java", "-Xbootclasspath/p:/opt/jassoft/alpn-boot-8.1.7.v20160121.jar", "-javaagent:/opt/jassoft/API.jar", "-Dnewrelic.config.file=/opt/jassoft/newrelic.yml", "-jar", "/opt/jassoft/API.jar"]
ADD alpn-boot-8.1.7.v20160121.jar /opt/jassoft/alpn-boot-8.1.7.v20160121.jar
ADD newrelic.yml /opt/jassoft/newrelic.yml
ADD API.jar /opt/jassoft/API.jar