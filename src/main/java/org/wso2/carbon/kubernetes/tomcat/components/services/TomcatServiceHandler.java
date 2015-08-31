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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static int nodePortValue;
    private final KubernetesAPIClientInterface client;

    private static final Log LOG = LogFactory.getLog(TomcatServiceHandler.class);

    public TomcatServiceHandler(String uri) {
        client = new KubernetesApiClient(uri);
        setInitNodePortValue();
    }

    public void createService(String serviceId, String serviceName) throws WebArtifactHandlerException {
        FileOutputThread fileOutput;

        try {
            client.createService(serviceId, serviceName, nodePortValue, KubernetesConstants.NODE_PORT,
                    KubernetesConstantsExtended.SERVICE_PORT_NAME,
                    KubernetesConstantsExtended.TOMCAT_DOCKER_CONTAINER_EXPOSED_PORT,
                    KubernetesConstantsExtended.SESSION_AFFINITY_CONFIG);

            if(nodePortValue < (KubernetesConstantsExtended.NODE_PORT_UPPER_RANGE - 1)) {
                nodePortValue++;
            }
            else {
                nodePortValue = KubernetesConstantsExtended.NODE_PORT_LOWER_RANGE;
            }

            List<String> output = new ArrayList<String>();
            output.add("" + nodePortValue);

            fileOutput = new
                    FileOutputThread(KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME, output);
            fileOutput.run();
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

    private void setInitNodePortValue() {
        FileInputThread fileInput = new FileInputThread(KubernetesConstantsExtended.NODE_PORT_ALLOCATION_FILENAME);

        fileInput.run();
        List<String> input = fileInput.getFileContent();

        // TODO: To be tested
        if(input.size() > 0) {
            nodePortValue = Integer.parseInt(input.get(0));
        }
        else {
            nodePortValue = KubernetesConstantsExtended.NODE_PORT_LOWER_RANGE + 1;
        }
    }

}
