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
package org.wso2.carbon.kubernetes.tomcat.components.services.interfaces;

import io.fabric8.kubernetes.api.model.Service;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;

public interface ITomcatServiceHandler {
    /**
     * creates a Kubernetes service
     *
     * @param serviceId   id of the service
     * @param serviceName service name to be used by the label name
     * @throws WebArtifactHandlerException
     */
    void createService(String serviceId, String serviceName) throws WebArtifactHandlerException;

    /**
     * returns a service corresponding to the service id
     *
     * @param serviceId id of the service
     * @return a service corresponding to the service id
     * @throws WebArtifactHandlerException
     */
    Service getService(String serviceId) throws WebArtifactHandlerException;

    /**
     * returns access URL String value of the Cluster IP service specified by the service ID
     *
     * @param serviceId id of the service
     * @param appName   name of the web artifact deployed
     * @return access URL String value of the Cluster IP service specified by the service ID
     * @throws WebArtifactHandlerException
     */
    String getClusterIP(String serviceId, String appName) throws WebArtifactHandlerException;

    /**
     * returns access URL String value of the NodePort service most recently created
     *
     * @param serviceId id of the service
     * @param appName   name of the web artifact deployed
     * @return access URL String value of the NodePort service most recently created
     * @throws WebArtifactHandlerException
     */
    String getNodePortIP(String serviceId, String appName) throws WebArtifactHandlerException;

    /**
     * removes the specified Kubernetes service
     *
     * @param serviceId id of the service
     * @throws WebArtifactHandlerException
     */
    void deleteService(String serviceId) throws WebArtifactHandlerException;
}
