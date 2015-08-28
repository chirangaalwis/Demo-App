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
package org.wso2.carbon.kubernetes.components.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.kubernetes.client.KubernetesApiClient;
import org.apache.stratos.kubernetes.client.KubernetesConstants;
import org.apache.stratos.kubernetes.client.exceptions.KubernetesClientException;
import org.apache.stratos.kubernetes.client.interfaces.KubernetesAPIClientInterface;
import org.wso2.carbon.kubernetes.support.WebArtifactHandlerException;

public class ServiceHandler implements IServiceHandler {

    private final KubernetesAPIClientInterface client;

    private static final Log LOG = LogFactory.getLog(ServiceHandler.class);

    public ServiceHandler(String uri) {
        client = new KubernetesApiClient(uri);
    }

    public void createService(String serviceId, String serviceName) throws WebArtifactHandlerException {
        // TODO: To be changed
        try {
            client.createService(serviceId, serviceName, 30001, KubernetesConstants.NODE_PORT, "http-1", 8080, "None");
        } catch (KubernetesClientException e) {
            String message = String.format("Could not create the service[service-identifier]: "
                    + "%s", serviceId);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public void deleteService(String serviceId) throws WebArtifactHandlerException {
        try {
            client.deleteService(serviceId);
        } catch (KubernetesClientException e) {
            String message = String.format("Could not delete the service[service-identifier]: "
                    + "%s", serviceId);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

}