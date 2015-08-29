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
package org.wso2.carbon.kubernetes.tomcat.components.replication_controllers.interfaces;

import org.wso2.carbon.exceptions.WebArtifactHandlerException;

/**
 * A Java interface for replication controller handling operations
 */
public interface ITomcatReplicationControllerHandler {
    /**
     * creates a replication controller
     * @param controllerName            name of the replication controller
     * @param podLabel                  value for pod label
     * @param tomcatDockerImageName     Apache Tomcat based Docker Image name
     * @param numberOfReplicas          number of pod replicas to be created
     * @throws Exception
     */
    void createReplicationController(String controllerName, String podLabel, String tomcatDockerImageName,
            int numberOfReplicas) throws WebArtifactHandlerException;

    /**
     * deletes the specified replication controller
     * @param controllerName            name of the replication controller
     * @throws Exception
     */
    void deleteReplicationController(String controllerName) throws WebArtifactHandlerException;
}
