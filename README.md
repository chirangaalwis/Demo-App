# Demo-Web-Artifact-Handler-POC-App
A Java application which deploys specified web artifacts in Apache Tomcat Docker containers orchestrated by Google Kubernetes. 

Uses the https://github.com/spotify/docker-client repo for Docker image handling and https://github.com/fabric8io/fabric8/tree/master/components/kubernetes-api repo for Google Kubernetes orchestration.

This Java IntelliJ IDEA project has been packaged using the maven-assembly-plugin. 

Follow the steps below to run the application:

1. Download and extract web-app-handler binary distribution to a desired location.
2. Unzip the java-web-artifact-handler-1.0-SNAPSHOT.zip.
3. Run /bin/web-app-handler-extension.sh.
