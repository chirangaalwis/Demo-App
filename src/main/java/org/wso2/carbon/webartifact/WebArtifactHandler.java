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
package org.wso2.carbon.webartifact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;
import org.wso2.carbon.docker.JavaWebArtifactImageBuilder;
import org.wso2.carbon.docker.interfaces.IDockerImageBuilder;
import org.wso2.carbon.kubernetes.tomcat.components.pods.TomcatPodHandler;
import org.wso2.carbon.kubernetes.tomcat.components.replication_controllers.TomcatReplicationControllerHandler;
import org.wso2.carbon.kubernetes.tomcat.components.replication_controllers.interfaces.ITomcatReplicationControllerHandler;
import org.wso2.carbon.kubernetes.tomcat.components.services.TomcatServiceHandler;
import org.wso2.carbon.kubernetes.tomcat.components.services.interfaces.ITomcatServiceHandler;
import org.wso2.carbon.webartifact.interfaces.IWebArtifactHandler;

import java.nio.file.Path;

public class WebArtifactHandler implements IWebArtifactHandler {

    private final TomcatPodHandler podHandler;
    private final ITomcatReplicationControllerHandler replicationControllerHandler;
    private final ITomcatServiceHandler serviceHandler;
    private final IDockerImageBuilder imageBuilder;

    private static final Log LOG = LogFactory.getLog(TomcatReplicationControllerHandler.class);

    public WebArtifactHandler(String endpointURL) throws WebArtifactHandlerException {
        podHandler = new TomcatPodHandler(endpointURL);
        replicationControllerHandler = new TomcatReplicationControllerHandler(endpointURL);
        serviceHandler = new TomcatServiceHandler(endpointURL);
        imageBuilder = new JavaWebArtifactImageBuilder();
    }

    public void deploy(String tenant, String appName, Path artifactPath, String version,
            int replicas) throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        String dockerImageName;

        try {
            dockerImageName = imageBuilder.buildImage(tenant, appName, version, artifactPath);
            Thread.sleep(5000);
            replicationControllerHandler.createReplicationController(componentName, componentName,
                    dockerImageName, replicas);
        } catch (Exception exception) {
            String message = String.format("Failed to deploy web artifact[web-artifact]: %s",
                    artifactPath.toString());
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public void undeploy(String tenant, String appName, Path artifactPath, String version)
            throws WebArtifactHandlerException {
        String componentName = generateKubernetesComponentName(tenant, appName);
        try {
            replicationControllerHandler.deleteReplicationController(componentName);
            podHandler.deleteReplicaPods(tenant, appName);
        } catch (Exception exception) {
            String message = String.format("Failed to remove web artifact[web-artifact]: %s",
                    artifactPath.toString());
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    private String generateKubernetesComponentName(String tenant, String appName) {
        return appName + "-" + tenant;
    }
}
