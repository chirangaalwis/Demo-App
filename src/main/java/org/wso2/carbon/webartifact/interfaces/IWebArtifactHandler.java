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
package org.wso2.carbon.webartifact.interfaces;

import org.wso2.carbon.exceptions.WebArtifactHandlerException;

import java.nio.file.Path;

/**
 * a Java interface which orchestrates web artifact deployment in Apache Tomcat Docker containers
 */
public interface IWebArtifactHandler {
    /**
     * deploys the specified web app
     * @param tenant            name of the tenant
     * @param appName           name of the app
     * @param artifactPath      uri to the web app resource
     * @param version           deployed version of the artifact
     * @param replicas          number of deployed replicas of the web app
     * @throws WebArtifactHandlerException
     */
    void deploy(String tenant, String appName, Path artifactPath, String version, int replicas)
            throws WebArtifactHandlerException;

    /**
     * removes the deployed, specified web app
     * @param tenant            name of the tenant
     * @param appName           name of the app
     * @param artifactPath      uri to the web app resource
     * @param version           deployed version of the artifact
     * @throws WebArtifactHandlerException
     */
    void undeploy(String tenant, String appName, Path artifactPath, String version)
            throws WebArtifactHandlerException;
}
