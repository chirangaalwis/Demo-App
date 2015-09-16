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
package org.wso2.strategy.poc.kubernetes.components.service;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.stratos.kubernetes.client.KubernetesApiClient;
import org.apache.stratos.kubernetes.client.KubernetesConstants;
import org.apache.stratos.kubernetes.client.interfaces.KubernetesAPIClientInterface;
import org.wso2.strategy.poc.kubernetes.components.service.interfaces.ITomcatServiceHandler;
import org.wso2.strategy.poc.kubernetes.constants.KubernetesConstantsExtended;
import org.wso2.strategy.poc.miscellaneous.exceptions.WebArtifactHandlerException;
import org.wso2.strategy.poc.miscellaneous.io.FileInputSingletonDataThread;
import org.wso2.strategy.poc.miscellaneous.io.FileOutputThread;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TomcatServiceHandler implements ITomcatServiceHandler {
    // holds the next available, valid port allocation for NodePort
    private static int nodePortValue;
    private final KubernetesAPIClientInterface client;
    private static final Logger LOG = LogManager.getLogger(TomcatServiceHandler.class);

    public TomcatServiceHandler(String kubernetesURI) {
        client = new KubernetesApiClient(kubernetesURI);
        setInitNodePortValue();
    }

    public void createService(String serviceId, String serviceName) throws WebArtifactHandlerException {
        if (serviceId != null) {
            try {
                Service service = client.getService(serviceId);
                FileOutputThread fileOutput;
                if (service == null) {
                    if (LOG.isDebugEnabled()) {
                        String message = String
                                .format("Creating Kubernetes service" + " [service-ID] %s [service-name] %s ",
                                        serviceId, serviceName);
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
                    fileOutput = new FileOutputThread(KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME,
                            output);
                    fileOutput.run();
                }
            } catch (Exception exception) {
                String message = String.format("Could not create the service[service-identifier]: " + "%s", serviceId);
                LOG.error(message, exception);
                throw new WebArtifactHandlerException(message, exception);
            }

        }
    }

    public Service getService(String serviceId) throws WebArtifactHandlerException {
        Service service;
        if (serviceId != null) {
            try {
                service = client.getService(serviceId);
            } catch (Exception exception) {
                String message = String.format("Could not create the service[service-identifier]: " + "%s", serviceId);
                LOG.error(message, exception);
                throw new WebArtifactHandlerException(message, exception);
            }
        } else {
            String message = "Service id cannot be null.";
            throw new WebArtifactHandlerException(message);
        }
        return service;
    }

    public String getClusterIP(String serviceId, String appName) throws WebArtifactHandlerException {
        if (serviceId != null) {
            try {
                Service service = client.getService(serviceId);
                if (service != null) {
                    return KubernetesHelper.getServiceURL(service) + "/" + appName;
                } else {
                    return "ClusterIP not available.";
                }
            } catch (Exception exception) {
                String message = String
                        .format("Could not find the service[service-identifier] " + "cluster ip: %s", serviceId);
                LOG.error(message, exception);
                throw new WebArtifactHandlerException(message, exception);
            }
        } else {
            String message = "Service id cannot be null.";
            throw new WebArtifactHandlerException(message);
        }
    }

    public String getNodePortIP(String serviceId, String appName) throws WebArtifactHandlerException {
        if (serviceId != null) {
            int nodePort;
            try {
                Service service = client.getService(serviceId);
                final int portIndex = 0;
                if (service != null) {
                    nodePort = service.getSpec().getPorts().get(portIndex).getNodePort();
                } else {
                    nodePort = -1;
                }
                if (nodePort != -1) {
                    return String
                            .format("http://%s:%d/%s", InetAddress.getLocalHost().getHostName(), nodePort, appName);
                } else {
                    return "NodePortIP not available";
                }
            } catch (Exception exception) {
                String message = "Could not find the NodePort IP.";
                LOG.error(message, exception);
                throw new WebArtifactHandlerException(message, exception);
            }
        } else {
            String message = "Service id cannot be null.";
            throw new WebArtifactHandlerException(message);
        }
    }

    public Service deleteService(String serviceId) throws WebArtifactHandlerException {
        if (serviceId != null) {
            Service service;
            try {
                service = client.getService(serviceId);
                if (service != null) {
                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Deleting Kubernetes service" + " [service-ID] %s", serviceId);
                        LOG.debug(message);
                    }
                    client.deleteService(serviceId);
                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Deleted Kubernetes service" + " [service-ID] %s", serviceId);
                        LOG.debug(message);
                    }
                }
            } catch (Exception exception) {
                String message = String.format("Could not delete the service[service-identifier]: " + "%s", serviceId);
                LOG.error(message, exception);
                throw new WebArtifactHandlerException(message, exception);
            }
            return service;
        } else {
            String message = "Service id cannot be null.";
            throw new WebArtifactHandlerException(message);
        }
    }

    /**
     * reads in the next available NodePort service type, port allocation value and assigns
     * the value to nodePortValue-TomcatServiceHandler class member variable
     */
    private void setInitNodePortValue() {
        FileInputSingletonDataThread fileInput = new FileInputSingletonDataThread(
                KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME);
        fileInput.run();
        List<String> input = fileInput.getFileContent();
        if (input.size() > 0) {
            nodePortValue = Integer.parseInt(input.get(0));
        } else {
            nodePortValue = KubernetesConstantsExtended.NODE_PORT_LOWER_LIMIT + 1;
        }
    }
}
