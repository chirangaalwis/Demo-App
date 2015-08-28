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
package org.wso2.carbon.kubernetes.components.pods;

import io.fabric8.kubernetes.api.KubernetesClient;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.kubernetes.support.KubernetesConstants;
import org.wso2.carbon.kubernetes.support.WebArtifactHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodHandler implements IPodHandler {

    private final KubernetesClient client;

    private static final Log LOG = LogFactory.getLog(PodHandler.class);

    public PodHandler(String uri) {
        client = new KubernetesClient(new KubernetesFactory(uri));
    }

    public void createPod(String podName, String podLabel, String dockerImageName)
            throws WebArtifactHandlerException {
        try {
            Pod pod = new Pod();

            pod.setApiVersion(Pod.ApiVersion.V_1);
            pod.setKind(KubernetesConstants.POD_KUBERNETES_COMPONENT_KIND);

            ObjectMeta metaData = new ObjectMeta();
            metaData.setName(podName);
            Map<String, String> labels = new HashMap<String, String>();
            labels.put(KubernetesConstants.LABEL_NAME, podLabel);
            metaData.setLabels(labels);

            pod.setMetadata(metaData);

            PodSpec podSpec = new PodSpec();

            Container podContainer =  new Container();
            podContainer.setName(podLabel);
            podContainer.setImage(dockerImageName);
            List<Container> containers = new ArrayList<Container>();
            containers.add(podContainer);

            podSpec.setContainers(containers);
            pod.setSpec(podSpec);

            client.createPod(pod);
        } catch (Exception e) {
            String message = String.format("Could not create the pod[pod-identifier]: "
                    + "%s", podName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public List<Pod> getPods() {
        return client.getPods().getItems();
    }

    public void deletePod(String podName) throws WebArtifactHandlerException {
        try {
            client.deletePod(podName);
        } catch (Exception e) {
            String message = String.format("Could not delete the pod[pod-identifier]: "
                    + "%s", podName);
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }

    public void deleteReplicaPods(String podBaseName) throws WebArtifactHandlerException {
        try {
            for(Pod pod : client.getPods().getItems()) {
                if(pod.getMetadata().getName().contains(podBaseName)) {
                    client.deletePod(pod.getMetadata().getName());
                }
            }
        } catch (Exception e) {
            String message = String.format("Could not delete the replica pods.");
            LOG.error(message, e);
            throw new WebArtifactHandlerException(message, e);
        }
    }
}
