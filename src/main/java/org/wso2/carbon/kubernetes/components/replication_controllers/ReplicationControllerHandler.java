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
package org.wso2.carbon.kubernetes.components.replication_controllers;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.kubernetes.support.KubernetesConstants;
import org.wso2.carbon.kubernetes.support.WebArtifactHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplicationControllerHandler implements IReplicationControllerHandler {

    private final KubernetesClient client;

    private static final Log LOG = LogFactory.getLog(ReplicationControllerHandler.class);

    public ReplicationControllerHandler(String uri) {
        client = new KubernetesClient(new KubernetesFactory(uri));
    }

    public void createReplicationController(String controllerName, String podLabel, String dockerImageName,
            int numberOfReplicas) throws WebArtifactHandlerException {
        try {
            ReplicationController replicationController = new ReplicationController();

            replicationController.setApiVersion(ReplicationController.ApiVersion.V_1);
            replicationController.setKind(KubernetesConstants.REPLICATION_CONTROLLER_COMPONENT_KIND);

            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(controllerName);
            replicationController.setMetadata(metadata);

            ReplicationControllerSpec replicationControllerSpec = new ReplicationControllerSpec();
            replicationControllerSpec.setReplicas(numberOfReplicas);

            PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
            PodSpec podSpec = new PodSpec();

            List<Container> podContainers = new ArrayList<Container>();
            Container container = new Container();
            container.setImage(dockerImageName);
            container.setName(podLabel);
            podContainers.add(container);
            podSpec.setContainers(podContainers);

            podTemplateSpec.setSpec(podSpec);

            Map<String, String> selectors = new HashMap<String, String>();
            selectors.put(KubernetesConstants.LABEL_NAME, podLabel);

            ObjectMeta tempMeta = new ObjectMeta();
            tempMeta.setLabels(selectors);
            podTemplateSpec.setMetadata(tempMeta);

            replicationControllerSpec.setTemplate(podTemplateSpec);
            replicationControllerSpec.setSelector(selectors);
            replicationController.setSpec(replicationControllerSpec);

            client.createReplicationController(replicationController);
        } catch (Exception e) {
            String message = String.format("Could not create the replication controller[rc-identifier]: "
                    + "%s", controllerName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public void deleteReplicationController(String controllerName) throws WebArtifactHandlerException {
        try {
            client.deletePod(controllerName);
        } catch (Exception e) {
            String message = String.format("Could not delete the replication controller[rc-identifier]: "
                    + "%s", controllerName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

}
