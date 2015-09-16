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
package org.wso2.strategy.poc.docker.interfaces;

import org.wso2.strategy.poc.miscellaneous.exceptions.WebArtifactHandlerException;

import java.util.List;

/**
 * a Java interface specifying application specific Docker Container handling functions
 */
public interface IDockerContainerHandler {
    /**
     * returns a list of currently running Docker Container ids matched by the Docker Image
     *
     * @param dockerImage name of the Docker Image that runs in the containers
     * @return a list of currently running Docker Container ids matched
     * by the Docker Image
     * @throws WebArtifactHandlerException
     */
    List<String> getRunningContainerIdsByImage(String dockerImage) throws WebArtifactHandlerException;

    /**
     * removes the containers specified by the container IDs
     *
     * @param containerIds list of ids of the containers to be deleted
     * @throws WebArtifactHandlerException
     */
    void deleteContainers(List<String> containerIds) throws WebArtifactHandlerException;
}

