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
package org.wso2.carbon6.poc.kubernetes.tomcat.components.replication_controllers;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.replication_controllers.interfaces.ITomcatReplicationControllerHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.constants.KubernetesConstantsExtended;
import org.wso2.carbon6.poc.miscellaneous.exceptions.WebArtifactHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Java class which implements the ITomcatReplicationControllerHandler interface
 */
public class TomcatReplicationControllerHandler implements ITomcatReplicationControllerHandler {
    private final KubernetesClient client;
    private static final Logger LOG = LogManager.getLogger(TomcatReplicationControllerHandler.class);

    public TomcatReplicationControllerHandler(String uri) {
        client = new KubernetesClient(new KubernetesFactory(uri));
    }

    public void createReplicationController(String controllerName, String podLabel, String tomcatDockerImageName,
            int numberOfReplicas) throws WebArtifactHandlerException {
        try {
            if ((controllerName != null) && (podLabel != null) && (tomcatDockerImageName != null)) {
                ReplicationController controller = getReplicationController(controllerName);

                if (controller == null) {
                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Creating Kubernetes replication controller"
                                        + " [controller-name] %s [pod-label] %s " + "[pod-Docker-image-name] %s",
                                controllerName, podLabel, tomcatDockerImageName);
                        LOG.debug(message);
                    }

                    ReplicationController replicationController = new ReplicationController();

                    replicationController.setApiVersion(ReplicationController.ApiVersion.V_1);
                    replicationController.setKind(KubernetesConstantsExtended.REPLICATION_CONTROLLER_COMPONENT_KIND);

                    ObjectMeta metadata = new ObjectMeta();
                    metadata.setName(controllerName);
                    replicationController.setMetadata(metadata);

                    ReplicationControllerSpec replicationControllerSpec = new ReplicationControllerSpec();
                    replicationControllerSpec.setReplicas(numberOfReplicas);

                    PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
                    PodSpec podSpec = new PodSpec();

                    List<Container> podContainers = new ArrayList<>();
                    Container container = new Container();
                    container.setImage(tomcatDockerImageName);
                    container.setName(podLabel);
                    podContainers.add(container);
                    podSpec.setContainers(podContainers);

                    podTemplateSpec.setSpec(podSpec);

                    Map<String, String> selectors = new HashMap<>();
                    selectors.put(KubernetesConstantsExtended.LABEL_NAME, podLabel);

                    ObjectMeta tempMeta = new ObjectMeta();
                    tempMeta.setLabels(selectors);
                    podTemplateSpec.setMetadata(tempMeta);

                    replicationControllerSpec.setTemplate(podTemplateSpec);
                    replicationControllerSpec.setSelector(selectors);
                    replicationController.setSpec(replicationControllerSpec);

                    client.createReplicationController(replicationController, "default");

                    if (LOG.isDebugEnabled()) {
                        String message = String.format("Created Kubernetes replication controller"
                                        + " [controller-name] %s [pod-label] %s " + "[pod-Docker-image-name] %s",
                                controllerName, podLabel, tomcatDockerImageName);
                        LOG.debug(message);
                    }
                }
            } else {
                String message = String
                        .format("Could not create the replication controller[rc-identifier]: " + "%s", controllerName);
                LOG.error(message);
                throw new WebArtifactHandlerException(message);
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not create the replication controller[rc-identifier]: " + "%s", controllerName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public ReplicationController getReplicationController(String controllerName) {
        ReplicationController controller = null;
        String controllerId;
        List<ReplicationController> replicationControllers = client.getReplicationControllers().getItems();
        if ((controllerName != null) && (replicationControllers != null)) {
            for (ReplicationController replicationController : replicationControllers) {
                controllerId = replicationController.getMetadata().getName();
                if (controllerName.equals(controllerId)) {
                    controller = replicationController;
                    break;
                }
            }
        }
        return controller;
    }

    public int getNoOfReplicas(String controllerName) throws WebArtifactHandlerException {
        ReplicationController replicationController = getReplicationController(controllerName);
        if (replicationController != null) {
            return replicationController.getSpec().getReplicas();
        } else {
            return 0;
        }
    }

    public void updateNoOfReplicas(String controllerName, int newReplicas) throws WebArtifactHandlerException {
        ReplicationController replicationController = getReplicationController(controllerName);
        try {
            if (replicationController != null) {
                ReplicationControllerSpec spec = replicationController.getSpec();
                if ((spec.getReplicas() != newReplicas) && (newReplicas > 0)) {
                    spec.setReplicas(newReplicas);
                    client.updateReplicationController(controllerName, replicationController);
                }
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not update the replication controller[rc-identifier]: " + "%s", controllerName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public void updateImage(String controllerName, String dockerImage) throws WebArtifactHandlerException {
        ReplicationController replicationController = getReplicationController(controllerName);
        final int imageIndex = 0;
        try {
            if (replicationController != null) {
                if (dockerImage != null) {
                    List<Container> podContainers = replicationController.getSpec().getTemplate().getSpec()
                            .getContainers();
                    if ((podContainers != null) && (podContainers.size() > 0)) {
                        podContainers.get(imageIndex).setImage(dockerImage);
                    }
                    client.updateReplicationController(controllerName, replicationController);
                }
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not update the replication controller[rc-identifier]: " + "%s", controllerName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public void deleteReplicationController(String controllerName) throws WebArtifactHandlerException {
        try {
            ReplicationController controller = getReplicationController(controllerName);
            if (controller != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Deleting Kubernetes replication controller" + " [rc-name] %s",
                            controllerName));
                }
                client.deleteReplicationController(controllerName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Deleting Kubernetes replication controller" + " [rc-name] %s",
                            controllerName));
                }
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not delete the replication controller[rc-identifier]: " + "%s", controllerName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }
}
