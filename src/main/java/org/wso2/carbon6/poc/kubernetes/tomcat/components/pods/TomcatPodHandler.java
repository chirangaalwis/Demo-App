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
package org.wso2.carbon6.poc.kubernetes.tomcat.components.pods;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon6.poc.kubernetes.tomcat.components.pods.interfaces.ITomcatPodHandler;
import org.wso2.carbon6.poc.kubernetes.tomcat.support.KubernetesConstantsExtended;
import org.wso2.carbon6.poc.exceptions.WebArtifactHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Java class which implements the ITomcatPodHandler Java interface
 */
public class TomcatPodHandler implements ITomcatPodHandler {

    private final KubernetesClient client;
    private static final Logger LOG = LogManager.getLogger(TomcatPodHandler.class);

    public TomcatPodHandler(String uri) {
        client = new KubernetesClient(new KubernetesFactory(uri));
    }

    public void createPod(String podName, String podLabel, String dockerImageName) throws WebArtifactHandlerException {
        try {
            if ((podName != null) && (podLabel != null) && (dockerImageName != null)) {
                if (LOG.isDebugEnabled()) {
                    String message = String.format("Creating Kubernetes pod [pod-name] %s "
                            + "[pod-label] %s [pod-Docker-image-name] %s.", podName, podLabel, dockerImageName);
                    LOG.debug(message);
                }

                Pod pod = new Pod();

                pod.setApiVersion(Pod.ApiVersion.V_1);
                pod.setKind(KubernetesConstantsExtended.POD_COMPONENT_KIND);

                ObjectMeta metaData = new ObjectMeta();
                metaData.setName(podName);
                Map<String, String> labels = new HashMap<>();
                labels.put(KubernetesConstantsExtended.LABEL_NAME, podLabel);
                metaData.setLabels(labels);

                pod.setMetadata(metaData);

                PodSpec podSpec = new PodSpec();

                Container podContainer = new Container();
                podContainer.setName(podLabel);
                podContainer.setImage(dockerImageName);
                List<Container> containers = new ArrayList<>();
                containers.add(podContainer);

                podSpec.setContainers(containers);
                pod.setSpec(podSpec);

                client.createPod(pod);

                if (LOG.isDebugEnabled()) {
                    String message = String.format("Created Kubernetes pod [pod-name] %s "
                            + "[pod-label] %s [pod-Docker-image-name] %s.", podName, podLabel, dockerImageName);
                    LOG.debug(message);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    String message = String.format("Could not create Kubernetes pod [pod-name] %s "
                            + "[pod-label] %s [pod-Docker-image-name] %s.", podName, podLabel, dockerImageName);
                    LOG.error(message);
                    throw new WebArtifactHandlerException(message);
                }
            }
        } catch (Exception e) {
            String message = String.format("Could not create the pod[pod-identifier]: " + "%s", podName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public List<Pod> getPods() {
        return client.getPods().getItems();
    }

    public void deletePod(String podName) throws WebArtifactHandlerException {
        try {
            if (podName != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Deleting Kubernetes pod [pod-name] %s", podName));
                }
                client.deletePod(podName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Deleted Kubernetes pod [pod-name] %s", podName));
                }
            } else {
                String message = String.format("Could not create the pod[pod-identifier]: " + "%s", podName);
                LOG.error(message);
                throw new WebArtifactHandlerException(message);
            }
        } catch (Exception e) {
            String message = String.format("Could not delete the pod[pod-identifier]: " + "%s", podName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public void deleteReplicaPods(String creator, String podBaseName) throws WebArtifactHandlerException {
        try {
            if ((creator != null) && (podBaseName != null)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleting Kubernetes replica pods.");
                }
                for (Pod pod : client.getPods().getItems()) {
                    Map<String, String> labels = pod.getMetadata().getLabels();
                    if ((labels != null) && (pod.getMetadata().getLabels().get(KubernetesConstantsExtended.LABEL_NAME)
                            .equals(podBaseName + "-" + creator))) {
                        client.deletePod(pod.getMetadata().getName());
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleted Kubernetes replica pods.");
                }
            } else {
                String message = "Could not delete the replica pods. Arguments for creator or/and" + " cannot be null.";
                LOG.error(message);
                throw new WebArtifactHandlerException(message);
            }
        } catch (Exception e) {
            String message = "Could not delete the replica pods.";
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }
}