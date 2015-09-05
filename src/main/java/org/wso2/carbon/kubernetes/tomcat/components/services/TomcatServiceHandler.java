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
package org.wso2.carbon.kubernetes.tomcat.components.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.stratos.kubernetes.client.KubernetesApiClient;
import org.apache.stratos.kubernetes.client.KubernetesConstants;
import org.apache.stratos.kubernetes.client.exceptions.KubernetesClientException;
import org.apache.stratos.kubernetes.client.interfaces.KubernetesAPIClientInterface;
import org.wso2.carbon.docker.support.FileOutputThread;
import org.wso2.carbon.kubernetes.tomcat.components.services.interfaces.ITomcatServiceHandler;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;
import org.wso2.carbon.kubernetes.tomcat.support.FileInputThread;
import org.wso2.carbon.kubernetes.tomcat.support.KubernetesConstantsExtended;

import java.util.ArrayList;
import java.util.List;

public class TomcatServiceHandler implements ITomcatServiceHandler {

    // holds the next available, valid port allocation for NodePort
    private static int nodePortValue;
    private final KubernetesAPIClientInterface client;
    private static final Logger LOG = LogManager.getLogger(TomcatServiceHandler.class);

    public TomcatServiceHandler(String uri) {
        client = new KubernetesApiClient(uri);
        setInitNodePortValue();
    }

    public void createService(String serviceId, String serviceName) throws WebArtifactHandlerException {
        FileOutputThread fileOutput;

        try {

            if (LOG.isDebugEnabled()) {
                String message = String
                        .format("Creating Kubernetes service" + " [service-ID] %s [service-name] %s ", serviceId,
                                serviceName);
                LOG.debug(message);
            }

            client.createService(serviceId, serviceName, nodePortValue, KubernetesConstants.NODE_PORT,
                    KubernetesConstantsExtended.SERVICE_PORT_NAME,
                    KubernetesConstantsExtended.TOMCAT_DOCKER_CONTAINER_EXPOSED_PORT,
                    KubernetesConstantsExtended.SESSION_AFFINITY_CONFIG);

            if (LOG.isDebugEnabled()) {
                String message = String
                        .format("Created Kubernetes service" + " [service-ID] %s [service-name] %s ", serviceId,
                                serviceName);
                LOG.debug(message);
            }

            // changing the NodePort service type port value to the next available port value
            if (nodePortValue < (KubernetesConstantsExtended.NODE_PORT_UPPER_LIMIT)) {
                nodePortValue++;
            } else {
                nodePortValue = KubernetesConstantsExtended.NODE_PORT_LOWER_LIMIT + 1;
            }

            // write the next possible port allocation value to a text file
            List<String> output = new ArrayList<>();
            output.add("" + nodePortValue);
            fileOutput = new FileOutputThread(KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME, output);
            fileOutput.run();
        } catch (KubernetesClientException e) {
            String message = String.format("Could not create the service[service-identifier]: " + "%s", serviceId);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public String getClusterIP(String serviceId, String appName) throws WebArtifactHandlerException {
        try {
            return String.format("http://%s:%d/%s", client.getService(serviceId).getSpec().getClusterIP(),
                    KubernetesConstantsExtended.TOMCAT_DOCKER_CONTAINER_EXPOSED_PORT, appName);
        } catch (KubernetesClientException e) {
            String message = String
                    .format("Could not find the service[service-identifier] " + "cluster ip: %s", serviceId);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public String getNodePortIP(String appName) {
        int previousNodePort = (nodePortValue - 1);
        return String
                .format("http://%s:%d/%s", KubernetesConstantsExtended.LOCALHOST_NODE_IP, previousNodePort, appName);
    }

    public void deleteService(String serviceId) throws WebArtifactHandlerException {
        try {
            if (LOG.isDebugEnabled()) {
                String message = String.format("Deleting Kubernetes service" + " [service-ID] %s", serviceId);
                LOG.debug(message);
            }

            client.deleteService(serviceId);

            if (LOG.isDebugEnabled()) {
                String message = String.format("Deleted Kubernetes service" + " [service-ID] %s", serviceId);
                LOG.debug(message);
            }
        } catch (KubernetesClientException e) {
            String message = String.format("Could not delete the service[service-identifier]: " + "%s", serviceId);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    /**
     * reads in the next available NodePort service type, port allocation value and assigns
     * the value to nodePortValue-TomcatServiceHandler class member variable
     */
    private void setInitNodePortValue() {
        FileInputThread fileInput = new FileInputThread(KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME);

        fileInput.run();
        List<String> input = fileInput.getFileContent();

        if (input.size() > 0) {
            nodePortValue = Integer.parseInt(input.get(0));
        } else {
            nodePortValue = KubernetesConstantsExtended.NODE_PORT_LOWER_LIMIT + 1;
        }
    }

}
