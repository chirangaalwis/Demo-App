/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon6.poc.webartifact;

import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.carbon6.poc.docker.JavaWebArtifactContainerHandler;
import org.wso2.carbon6.poc.docker.JavaWebArtifactImageHandler;
import org.wso2.carbon6.poc.docker.interfaces.IDockerContainerHandler;
import org.wso2.carbon6.poc.docker.interfaces.IDockerImageHandler;
import org.wso2.carbon6.poc.miscellaneous.exceptions.WebArtifactHandlerException;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.pods.TomcatPodHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.pods.interfaces.ITomcatPodHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.replication_controllers.TomcatReplicationControllerHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.replication_controllers.interfaces.ITomcatReplicationControllerHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.services.TomcatServiceHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.services.interfaces.ITomcatServiceHandler;
import org.wso2.carbon6.poc.webartifact.interfaces.IWebArtifactHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WebArtifactHandler implements IWebArtifactHandler {
    private final ITomcatPodHandler podHandler;
    private final ITomcatReplicationControllerHandler replicationControllerHandler;
    private final ITomcatServiceHandler serviceHandler;
    private final IDockerImageHandler imageBuilder;
    private final IDockerContainerHandler containerHandler;

    private static final int IMAGE_BUILD_DELAY_IN_MILLISECONDS = 2000;
    private static final int KUBERNETES_COMPONENT_REMOVAL_DELAY_IN_MILLISECONDS = 10000;
    private static final Log LOG = LogFactory.getLog(TomcatReplicationControllerHandler.class);

    public WebArtifactHandler(String endpointURL) throws WebArtifactHandlerException {
        podHandler = new TomcatPodHandler(endpointURL);
        replicationControllerHandler = new TomcatReplicationControllerHandler(endpointURL);
        serviceHandler = new TomcatServiceHandler(endpointURL);
        imageBuilder = new JavaWebArtifactImageHandler();
        containerHandler = new JavaWebArtifactContainerHandler();
    }

    public boolean deploy(String tenant, String appName, Path artifactPath, String version, int replicas)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        String dockerImageName;
        try {
            if (imageBuilder.getExistingImages(tenant, appName, version).size() == 0) {
                // append build date and time to major version
                DateTime dateTime = new DateTime();
                String now = dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-"
                        + dateTime.getMillisOfDay();
                version += ("-" + now);
                dockerImageName = imageBuilder.buildImage(tenant, appName, version, artifactPath);
                Thread.sleep(IMAGE_BUILD_DELAY_IN_MILLISECONDS);
                replicationControllerHandler
                        .createReplicationController(componentName, componentName, dockerImageName, replicas);
                serviceHandler.createService(componentName, componentName);
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = String.format("Failed to deploy web artifact[web-artifact]: %s", artifactPath.toString());
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public boolean rollBack(String tenant, String appName, String version, String buildIdentifier)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        if ((imageBuilder.getExistingImages(tenant, appName, version).size() > 0)) {
            replicationControllerHandler.updateImage(componentName, buildIdentifier);
            podHandler.deleteReplicaPods(tenant, appName);
            return true;
        } else {
            return false;
        }
    }

    public boolean rollUpdate(String tenant, String appName, String version, Path artifactPath)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        if ((imageBuilder.getExistingImages(tenant, appName, version).size() > 0)) {
            DateTime dateTime = new DateTime();
            String now = dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-"
                    + dateTime.getMillisOfDay();
            version += ("-" + now);
            String dockerImageName = imageBuilder.buildImage(tenant, appName, version, artifactPath);
            replicationControllerHandler.updateImage(componentName, dockerImageName);
            podHandler.deleteReplicaPods(tenant, appName);
            return true;
        } else {
            return false;
        }
    }

    public boolean scale(String tenant, String appName, int noOfReplicas) throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            replicationControllerHandler.updateNoOfReplicas(componentName, noOfReplicas);
            return true;
        } else {
            return false;
        }
    }

    public int getNoOfReplicas(String tenant, String appName) throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        return replicationControllerHandler.getNoOfReplicas(componentName);
    }

    public List<String> listExistingBuildArtifacts(String tenant, String appName, String version)
            throws WebArtifactHandlerException {
        List<String> artifactList = new ArrayList<>();
        ImmutableList<String> repoTags;
        for (int count = 0; count < imageBuilder.getExistingImages(tenant, appName, version).size(); count++) {
            repoTags = imageBuilder.getExistingImages(tenant, appName, version).get(count).repoTags();
            for (String tag : repoTags) {
                if (tag.contains(tenant + "/" + appName + ":" + version)) {
                    artifactList.add(tag);
                }
            }
        }
        return artifactList;
    }

    public List<String> listHigherBuildArtifactVersions(String tenant, String appName, String version)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        List<String> majorArtifactList = new ArrayList<>();
        final int singleImageIndex = 0;
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            String lowerLimitVersion = replicationControllerHandler.getReplicationController(componentName).getSpec()
                    .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
            List<String> artifactList = listExistingBuildArtifacts(tenant, appName, version);
            majorArtifactList = new ArrayList<>();
            for (String artifactImageBuild : artifactList) {
                if (compareBuildVersions(lowerLimitVersion, artifactImageBuild) < 0) {
                    majorArtifactList.add(artifactImageBuild);
                }
            }
        }

        return majorArtifactList;
    }

    public List<String> listLowerBuildArtifactVersions(String tenant, String appName, String version)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        List<String> minorArtifactList = new ArrayList<>();
        final int singleImageIndex = 0;
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            String upperLimitVersion = replicationControllerHandler.getReplicationController(componentName).getSpec()
                    .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
            List<String> artifactList = listExistingBuildArtifacts(tenant, appName, version);
            minorArtifactList = new ArrayList<>();
            for (String artifactImageBuild : artifactList) {
                if (compareBuildVersions(upperLimitVersion, artifactImageBuild) > 0) {
                    minorArtifactList.add(artifactImageBuild);
                }
            }
        }

        return minorArtifactList;
    }

    public String getServiceAccessIPs(String tenant, String appName, Path artifactPath)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        String ipMessage;
        ipMessage = String.format("Cluster IP: %s\nPublic IP: %s\n\n",
                serviceHandler.getClusterIP(componentName, getArtifactName(artifactPath)),
                serviceHandler.getNodePortIP(componentName, getArtifactName(artifactPath)));

        return ipMessage;
    }

    public boolean remove(String tenant, String appName) throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        final int singleImageIndex = 0;
        try {
            if (replicationControllerHandler.getReplicationController(componentName) != null) {
                String dockerImage = replicationControllerHandler.getReplicationController(componentName).getSpec()
                        .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
                //List<String> containerIds = containerHandler.getRunningContainerIdsByImage(dockerImage);
                replicationControllerHandler.deleteReplicationController(componentName);
                podHandler.deleteReplicaPods(tenant, appName);
                serviceHandler.deleteService(componentName);
                /*Thread.sleep(KUBERNETES_COMPONENT_REMOVAL_DELAY_IN_MILLISECONDS);
                containerHandler.deleteContainers(containerIds);
                imageBuilder.removeImage(tenant, appName, getDockerImageVersion(dockerImage));*/
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = String.format("Failed to remove web artifact[artifact]: %s",
                    generateKubernetesComponentName(tenant, appName));
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    /**
     * utility method which returns a Kubernetes identifier based on the deploying
     * tenant and app name
     *
     * @param tenant  tenant which deploys the web artifact
     * @param appName name of the web artifact
     * @return Kubernetes identifier based on the deploying tenant and app name
     */
    private String generateKubernetesComponentName(String tenant, String appName) {
        return appName + "-" + tenant;
    }

    /**
     * utility method which returns the name of the web artifact specified
     *
     * @param artifactPath path to the web artifact
     * @return the name of the web artifact specified
     */
    private String getArtifactName(Path artifactPath) {
        String artifactFileName = artifactPath.getFileName().toString();
        return artifactFileName.substring(0, artifactFileName.length() - 4);
    }

    /**
     * compares two web artifact version builds and indicates which version should come before and after
     *
     * @param buildIdentifierOne web artifact version build one
     * @param buildIdentifierTwo web artifact version build two
     * @return indicates which version should come before and after
     */
    private int compareBuildVersions(String buildIdentifierOne, String buildIdentifierTwo) {
        int result;
        String[] buildIdentifierOneTenantSplit = buildIdentifierOne.split(":");
        String[] buildIdentifierTwoTenantSplit = buildIdentifierTwo.split(":");
        String[] buildIdentifierOneIdentifierSplit = buildIdentifierOneTenantSplit[1].split("-");
        String[] buildIdentifierTwoIdentifierSplit = buildIdentifierTwoTenantSplit[1].split("-");
        int repoIndex = 0;
        int versionIndex = 0;
        int yearIndex = 1;
        int monthIndex = 2;
        int dayIndex = 3;
        String identifierOne =
                buildIdentifierOneTenantSplit[repoIndex] + ":" + buildIdentifierOneIdentifierSplit[versionIndex] +
                        "-" + buildIdentifierOneIdentifierSplit[yearIndex] + "-"
                        + buildIdentifierOneIdentifierSplit[monthIndex] +
                        "-" + buildIdentifierOneIdentifierSplit[dayIndex];
        String identifierTwo =
                buildIdentifierTwoTenantSplit[repoIndex] + ":" + buildIdentifierTwoIdentifierSplit[versionIndex] +
                        "-" + buildIdentifierTwoIdentifierSplit[yearIndex] + "-"
                        + buildIdentifierTwoIdentifierSplit[monthIndex] +
                        "-" + buildIdentifierTwoIdentifierSplit[dayIndex];

        if (identifierOne.compareTo(identifierTwo) < 0) {
            result = -1;
        } else if (identifierOne.compareTo(identifierTwo) > 0) {
            result = 1;
        } else {
            long identifierOneTime = Long.parseLong(buildIdentifierOneIdentifierSplit[4]);
            long identifierTwoTime = Long.parseLong(buildIdentifierTwoIdentifierSplit[4]);
            if (identifierOneTime < identifierTwoTime) {
                result = -1;
            } else if (identifierOneTime > identifierTwoTime) {
                result = 1;
            } else {
                result = 0;
            }
        }
        return result;
    }

    /**
     * a utility method which returns the version component of the Docker Image specified
     *
     * @param dockerImageName the Docker Image
     * @return the version component of the Docker Image specified
     */
    private String getDockerImageVersion(String dockerImageName) {
        String[] imageComponents = dockerImageName.split(":");
        final int versionIndex = 1;
        return imageComponents[versionIndex];
    }
}
