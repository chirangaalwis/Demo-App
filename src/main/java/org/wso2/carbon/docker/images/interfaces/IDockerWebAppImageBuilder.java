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
package org.wso2.carbon.docker.images.interfaces;

import org.wso2.carbon.docker.images.exceptions.DockerImageBuilderException;

import java.nio.file.Path;

public interface IDockerWebAppImageBuilder {

    /**
     * builds up a Docker image which deploys the specified artifact
     * @param creator           the name of the deployer
     * @param imageName         the Docker image identifier
     * @param imageVersion      the Docker image version
     * @param artifactPath      the artifact to be deployed
     * @throws DockerImageBuilderException
     */
    void buildImage(String creator, String imageName, String imageVersion, Path artifactPath) throws
            DockerImageBuilderException;

    /**
     * deletes the specified Docker image
     * @param creator           the name of the deployer
     * @param imageName         the Docker image identifier
     * @param imageVersion      the Docker image version
     * @throws DockerImageBuilderException
     */
    void removeImages(String creator, String imageName, String imageVersion) throws
            DockerImageBuilderException;
}
