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
package org.wso2.carbon6.poc.kubernetes.tomcat.components.replication_controllers.interfaces;

import io.fabric8.kubernetes.api.model.ReplicationController;
import org.wso2.carbon6.poc.miscellaneous.exceptions.WebArtifactHandlerException;

/**
 * A Java interface for replication controller handling operations
 */
public interface ITomcatReplicationControllerHandler {
    /**
     * creates a replication controller
     *
     * @param controllerName        name of the replication controller
     * @param podLabel              value for pod label
     * @param tomcatDockerImageName Apache Tomcat based Docker Image name
     * @param numberOfReplicas      number of pod replicas to be created
     * @throws WebArtifactHandlerException
     */
    void createReplicationController(String controllerName, String podLabel, String tomcatDockerImageName,
            int numberOfReplicas) throws WebArtifactHandlerException;

    /**
     * returns a replication controller corresponding to the controller name
     *
     * @param controllerName name of the replication controller
     * @return a replication controller corresponding to the controller name
     */
    ReplicationController getReplicationController(String controllerName);

    /**
     * returns the number of replica pods that has been already deployed
     *
     * @param controllerName name of the replication controller
     * @return the number of replica pods that has been already deployed
     * @throws WebArtifactHandlerException
     */
    int getNoOfReplicas(String controllerName) throws WebArtifactHandlerException;

    /**
     * set a new number of pod replicas to a specified replication controller
     *
     * @param controllerName name of the replication controller
     * @param newReplicas    new number of replicas
     * @throws WebArtifactHandlerException
     */
    void updateNoOfReplicas(String controllerName, int newReplicas) throws WebArtifactHandlerException;

    /**
     * set a new Docker image to a specified replication controller
     *
     * @param controllerName name of the replication controller
     * @param dockerImage    new Docker image
     * @throws WebArtifactHandlerException
     */
    void updateImage(String controllerName, String dockerImage) throws WebArtifactHandlerException;

    /**
     * deletes the specified replication controller
     *
     * @param controllerName name of the replication controller
     * @throws WebArtifactHandlerException
     */
    void deleteReplicationController(String controllerName) throws WebArtifactHandlerException;
}
