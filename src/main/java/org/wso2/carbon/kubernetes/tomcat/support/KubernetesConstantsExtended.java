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
package org.wso2.carbon.kubernetes.tomcat.support;

public class KubernetesConstantsExtended {
    public static final String POD_KUBERNETES_COMPONENT_KIND = "Pod";
    public static final String REPLICATION_CONTROLLER_COMPONENT_KIND = "ReplicationController";
    public static final String LABEL_NAME = "name";
    public static final int NODE_PORT_LOWER_RANGE = 30000;
    public static final int NODE_PORT_UPPER_RANGE = 32767;
    public static final int TOMCAT_DOCKER_CONTAINER_EXPOSED_PORT = 8080;
    public static final String NODE_PORT_ALLOCATION_FILENAME = "NodePortAllocation.txt";
    public static final String SESSION_AFFINITY_CONFIG = "None";
    public static final String SERVICE_PORT_NAME = "http-1";
}
