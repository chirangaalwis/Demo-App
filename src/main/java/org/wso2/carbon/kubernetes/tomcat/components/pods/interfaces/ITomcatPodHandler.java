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
package org.wso2.carbon.kubernetes.tomcat.components.pods.interfaces;

import io.fabric8.kubernetes.api.model.Pod;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;

import java.util.List;

/**
 * a Java interface for Pod handling operations
 */
public interface ITomcatPodHandler {
    /**
     * creates a new pod with the specified Docker Image name
     * @param podName                   name of the pod
     * @param podLabel                  value for pod label
     * @param tomcatDockerImageName     Apache Tomcat based Docker Image name
     * @throws WebArtifactHandlerException
     */
    void createPod(String podName, String podLabel, String tomcatDockerImageName) throws WebArtifactHandlerException;

    /**
     * returns the list of active pods
     * @return  list of Kubernetes pods
     */
    List<Pod> getPods();

    /**
     * deletes the pod specified by the identifier
     * @param podName           name of the pod
     * @throws WebArtifactHandlerException
     */
    void deletePod(String podName) throws WebArtifactHandlerException;
}

