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
package org.wso2.carbon.kubernetes.tomcat;

import org.wso2.carbon.kubernetes.tomcat.components.pods.TomcatPodHandler;
import org.wso2.carbon.kubernetes.tomcat.components.pods.interfaces.ITomcatPodHandler;
import org.wso2.carbon.kubernetes.tomcat.components.replication_controllers.TomcatReplicationControllerHandler;
import org.wso2.carbon.kubernetes.tomcat.components.replication_controllers.interfaces.ITomcatReplicationControllerHandler;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;

public class KubernetesTester {

    public static void main(String[] args) {
        try {
            TomcatPodHandler podHandler = new TomcatPodHandler(KubernetesTestConstants.ENDPOINT_URL);

            /*podHandler.createPod(KubernetesTestConstants.POD_NAME, KubernetesTestConstants.POD_LABEL,
                    KubernetesTestConstants.POD_IMAGE);*/

//            podHandler.deletePod(KubernetesTestConstants.POD_NAME);

            ITomcatReplicationControllerHandler replicationControllerHandler =
                    new TomcatReplicationControllerHandler(KubernetesTestConstants.ENDPOINT_URL);

//            replicationControllerHandler.createReplicationController("helloworld-rc", "helloworld", "helloworld", 3);

            replicationControllerHandler.deleteReplicationController("helloworld-rc");

        } catch (WebArtifactHandlerException e) {
            e.printStackTrace();
        }
    }

}
