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
package org.wso2.strategy.poc.webartifact;

import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.wso2.strategy.poc.docker.JavaDockerImageHandler;
import org.wso2.strategy.poc.docker.interfaces.IDockerImageHandler;
import org.wso2.strategy.poc.kubernetes.components.pod.TomcatPodHandler;
import org.wso2.strategy.poc.kubernetes.components.pod.interfaces.ITomcatPodHandler;
import org.wso2.strategy.poc.kubernetes.components.replication_controller.TomcatReplicationControllerHandler;
import org.wso2.strategy.poc.kubernetes.components.replication_controller.interfaces.ITomcatReplicationControllerHandler;
import org.wso2.strategy.poc.kubernetes.components.service.TomcatServiceHandler;
import org.wso2.strategy.poc.kubernetes.components.service.interfaces.ITomcatServiceHandler;
import org.wso2.strategy.poc.miscellaneous.exceptions.WebArtifactHandlerException;
import org.wso2.strategy.poc.miscellaneous.helper.WebArtifactHandlerHelper;
import org.wso2.strategy.poc.webartifact.interfaces.IWebArtifactHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WebArtifactHandler implements IWebArtifactHandler {
    private final IDockerImageHandler imageBuilder;
    private final ITomcatPodHandler podHandler;
    private final ITomcatReplicationControllerHandler replicationControllerHandler;
    private final ITomcatServiceHandler serviceHandler;

    private static final int IMAGE_BUILD_DELAY_IN_MILLISECONDS = 2000;
    private static final Log LOG = LogFactory.getLog(TomcatReplicationControllerHandler.class);

    public WebArtifactHandler(String dockerEndpointURL, String kubernetesEndpointURL)
            throws WebArtifactHandlerException {
        imageBuilder = new JavaDockerImageHandler(dockerEndpointURL);
        podHandler = new TomcatPodHandler(kubernetesEndpointURL);
        replicationControllerHandler = new TomcatReplicationControllerHandler(kubernetesEndpointURL);
        serviceHandler = new TomcatServiceHandler(kubernetesEndpointURL);
    }

    public boolean deploy(String tenant, String appName, Path artifactPath, String version, int replicas)
            throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
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
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        if ((imageBuilder.getExistingImages(tenant, appName, version).size() > 0)) {
            replicationControllerHandler.updateImage(componentName, buildIdentifier);
            podHandler.deleteReplicaPods(replicationControllerHandler.getReplicationController(
                    WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName)), tenant, appName);
            return true;
        } else {
            return false;
        }
    }

    public boolean rollUpdate(String tenant, String appName, String version, Path artifactPath)
            throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        if ((imageBuilder.getExistingImages(tenant, appName, version).size() > 0)) {
            DateTime dateTime = new DateTime();
            String now = dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-"
                    + dateTime.getMillisOfDay();
            version += ("-" + now);
            String dockerImageName = imageBuilder.buildImage(tenant, appName, version, artifactPath);
            replicationControllerHandler.updateImage(componentName, dockerImageName);
            podHandler.deleteReplicaPods(replicationControllerHandler.getReplicationController(
                    WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName)), tenant, appName);
            return true;
        } else {
            return false;
        }
    }

    public boolean scale(String tenant, String appName, int noOfReplicas) throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            replicationControllerHandler.updateNoOfReplicas(componentName, noOfReplicas);
            return true;
        } else {
            return false;
        }
    }

    public int getNoOfReplicas(String tenant, String appName) throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
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
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        List<String> majorArtifactList = new ArrayList<>();
        final int singleImageIndex = 0;
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            String lowerLimitVersion = replicationControllerHandler.getReplicationController(componentName).getSpec()
                    .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
            List<String> artifactList = listExistingBuildArtifacts(tenant, appName, version);
            majorArtifactList = new ArrayList<>();
            for (String artifactImageBuild : artifactList) {
                if (WebArtifactHandlerHelper.compareBuildVersions(lowerLimitVersion, artifactImageBuild) < 0) {
                    majorArtifactList.add(artifactImageBuild);
                }
            }
        }

        return majorArtifactList;
    }

    public List<String> listLowerBuildArtifactVersions(String tenant, String appName, String version)
            throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        List<String> minorArtifactList = new ArrayList<>();
        final int singleImageIndex = 0;
        if (replicationControllerHandler.getReplicationController(componentName) != null) {
            String upperLimitVersion = replicationControllerHandler.getReplicationController(componentName).getSpec()
                    .getTemplate().getSpec().getContainers().get(singleImageIndex).getImage();
            List<String> artifactList = listExistingBuildArtifacts(tenant, appName, version);
            minorArtifactList = new ArrayList<>();
            for (String artifactImageBuild : artifactList) {
                if (WebArtifactHandlerHelper.compareBuildVersions(upperLimitVersion, artifactImageBuild) > 0) {
                    minorArtifactList.add(artifactImageBuild);
                }
            }
        }

        return minorArtifactList;
    }

    public String getServiceAccessIPs(String tenant, String appName, Path artifactPath)
            throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        String ipMessage;
        ipMessage = String.format("Cluster IP: %s\nPublic IP: %s\n\n",
                serviceHandler.getClusterIP(componentName, WebArtifactHandlerHelper.getArtifactName(artifactPath)),
                serviceHandler.getNodePortIP(componentName, WebArtifactHandlerHelper.getArtifactName(artifactPath)));
        return ipMessage;
    }

    public boolean remove(String tenant, String appName) throws WebArtifactHandlerException {
        String componentName = WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName);
        try {
            if (replicationControllerHandler.getReplicationController(componentName) != null) {
                final int noPods = 0;
                scale(tenant, appName, noPods);
                replicationControllerHandler.deleteReplicationController(componentName);
                serviceHandler.deleteService(componentName);
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            String message = String.format("Failed to remove web artifact[artifact]: %s",
                    WebArtifactHandlerHelper.generateKubernetesComponentIdentifier(tenant, appName));
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }
}
