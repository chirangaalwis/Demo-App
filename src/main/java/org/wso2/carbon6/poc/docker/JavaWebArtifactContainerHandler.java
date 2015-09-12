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
package org.wso2.carbon6.poc.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon6.poc.docker.interfaces.IDockerContainerHandler;
import org.wso2.carbon6.poc.miscellaneous.exceptions.WebArtifactHandlerException;

import java.util.ArrayList;
import java.util.List;

public class JavaWebArtifactContainerHandler implements IDockerContainerHandler {

    private final DockerClient dockerClient;

    private static final int OPERATION_DELAY_IN_MILLISECONDS = 2000;
    private static final Logger LOG = LogManager.getLogger(JavaWebArtifactContainerHandler.class);

    public JavaWebArtifactContainerHandler() throws WebArtifactHandlerException {
        try {
            // creates a new com.spotify.docker.client.DockerClient
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating new DockerClient.");
            }
            dockerClient = DefaultDockerClient.fromEnv().build();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Creating new DockerClient[docker-client]: %s.", dockerClient));
            }
        } catch (Exception exception) {
            String message = "Could not create a new JavaWebArtifactContainerHandler instance.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public List<String> getRunningContainerIdsByImage(String dockerImage) throws WebArtifactHandlerException {
        String containerImage;
        List<String> listOfContainers = new ArrayList<>();
        try {
            for (Container container : dockerClient.listContainers()) {
                containerImage = container.image();
                if (containerImage.equals(dockerImage)) {
                    listOfContainers.add(container.id());
                }
            }
        } catch (Exception exception) {
            String message = "Could not load the list of Docker Containers.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
        return listOfContainers;
    }

    public void deleteContainers(List<String> containerIds) throws WebArtifactHandlerException {
        try {
            for (String containerId : containerIds) {
                dockerClient.removeContainer(containerId);
                Thread.sleep(OPERATION_DELAY_IN_MILLISECONDS);
            }
        } catch (Exception exception) {
            String message = "Could not delete the Docker Containers.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }
}
