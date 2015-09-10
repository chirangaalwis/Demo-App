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
package org.wso2.carbon6.poc.webartifact.interfaces;

import org.wso2.carbon6.poc.exceptions.WebArtifactHandlerException;

import java.nio.file.Path;
import java.util.List;

/**
 * a Java interface which orchestrates web artifact deployment in Apache Tomcat Docker containers
 */
public interface IWebArtifactHandler {
    /**
     * deploys the specified web app
     *
     * @param tenant       name of the tenant
     * @param appName      name of the app
     * @param artifactPath uri to the web app resource
     * @param version      deployed version of the artifact
     * @param replicas     number of deployed replicas of the web app
     * @return true if successfully deployed, else false
     * @throws WebArtifactHandlerException
     */
    boolean deploy(String tenant, String appName, Path artifactPath, String version, int replicas)
            throws WebArtifactHandlerException;

    /**
     * rolls back or forward to an existing version of the web artifact build
     *
     * @param tenant          name of the tenant
     * @param appName         name of the app
     * @param version         deployed version of the artifact
     * @param buildIdentifier identifier of web artifact build to be newly deployed
     * @return true if successfully updated, else false
     * @throws WebArtifactHandlerException
     */
    boolean rollBack(String tenant, String appName, String version, String buildIdentifier)
            throws WebArtifactHandlerException;

    /**
     * make a roll update to the newly deployed web artifact build
     *
     * @param tenant       name of the tenant
     * @param appName      name of the app
     * @param version      deployed version of the artifact
     * @param artifactPath newly deployed web artifact
     * @return true if successfully updated, else false
     * @throws WebArtifactHandlerException
     */
    boolean rollUpdate(String tenant, String appName, String version, Path artifactPath)
            throws WebArtifactHandlerException;

    /**
     * scale the number of web artifact replicas running
     *
     * @param tenant       name of the tenant
     * @param appName      name of the app
     * @param noOfReplicas latest number of replicas to be deployed
     * @return true if successfully scaled, else false
     * @throws WebArtifactHandlerException
     */
    boolean scale(String tenant, String appName, int noOfReplicas) throws WebArtifactHandlerException;

    /**
     * returns the number of replicas of a particular web artifact running, currently
     *
     * @param tenant  name of the tenant
     * @param appName name of the app
     * @return the number of replicas of a particular web artifact running, currently
     * @throws WebArtifactHandlerException
     */
    int getNoOfReplicas(String tenant, String appName) throws WebArtifactHandlerException;

    /**
     * utility method which returns a list of web artifact build versions under the specified
     * repo and version
     *
     * @param tenant  tenant which deploys the web artifact
     * @param appName name of the web artifact
     * @param version major version of the web artifact
     * @return a list of web artifact build versions under the specified repo and version
     * @throws WebArtifactHandlerException
     */
    List<String> listExistingBuildArtifacts(String tenant, String appName, String version)
            throws WebArtifactHandlerException;

    /**
     * returns a list of web artifact build (sub) versions under the specified
     * repo and version which are higher than the currently running build version
     *
     * @param tenant  tenant which deploys the web artifact
     * @param appName name of the web artifact
     * @param version version of the web artifact
     * @return a list of web artifact build (sub) versions under the specified
     * repo and version which are higher than the currently running build version
     * @throws WebArtifactHandlerException
     */
    List<String> listHigherBuildArtifactVersions(String tenant, String appName, String version)
            throws WebArtifactHandlerException;

    /**
     * returns a list of web artifact build (sub) versions under the specified
     * repo and version which are lower than the currently running build version
     *
     * @param tenant  tenant which deploys the web artifact
     * @param appName name of the web artifact
     * @param version version of the web artifact
     * @return a list of web artifact build (sub) versions under the specified
     * repo and version which are lower than the currently running build version
     * @throws WebArtifactHandlerException
     */
    List<String> listLowerBuildArtifactVersions(String tenant, String appName, String version)
            throws WebArtifactHandlerException;

    /**
     * returns a String message of access IPs for the most recently created service
     *
     * @param tenant       tenant which deploys the web artifact
     * @param appName      name of the web artifact
     * @param artifactPath path to the web artifact
     * @return a String message of access IPs for the most recently created service
     * @throws WebArtifactHandlerException
     */
    String getServiceAccessIPs(String tenant, String appName, Path artifactPath) throws WebArtifactHandlerException;

    /**
     * removes the deployed, specified web app
     *
     * @param tenant  name of the tenant
     * @param appName name of the app
     * @throws WebArtifactHandlerException
     */
    void remove(String tenant, String appName) throws WebArtifactHandlerException;

}
