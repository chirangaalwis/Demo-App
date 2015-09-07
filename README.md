# Java-Kubernetes-Web-Artifact-Handler
A Java application which deploys specified web artifacts in Apache Tomcat Docker containers orchestrated by Google Kubernetes. 

Uses the https://github.com/spotify/docker-client repo for Docker image handling and https://github.com/fabric8io/fabric8/tree/master/components/kubernetes-api repo for Google Kubernetes orchestration.

This Java IntelliJ IDEA project has been packaged as a jar. 

First, clone the Java-Kubernetes-Web-Artifact-Handler repo and build it using Apache Maven. (Java target version is 1.7)

Maven-shade-plugin has been used to bundle up the required dependencies hence, when executing the java-web-artifact-handler-1.0-SNAPSHOT.jar, the uber-java-web-artifact-handler-1.0-SNAPSHOT.jar has to be in the classpath.(both jar files in the same folder)

You can execute the following command in the commandline tool to execute the Java application:

java -cp uber-java-web-artifact-handler-1.0-SNAPSHOT.jar org.wso2.carbon.Executor
