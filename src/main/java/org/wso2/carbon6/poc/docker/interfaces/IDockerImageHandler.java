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
package org.wso2.carbon6.poc.docker.interfaces;

import com.spotify.docker.client.messages.Image;
import org.wso2.carbon6.poc.exceptions.WebArtifactHandlerException;

import java.nio.file.Path;
import java.util.List;

/**
 * a Java interface for handling web artifact deployment in Docker Images
 */
public interface IDockerImageHandler {

    /**
     * builds up a Docker image which deploys the specified artifact
     *
     * @param creator      the name of the deployer
     * @param imageName    the Docker image identifier
     * @param imageVersion the Docker image version
     * @param artifactPath the artifact to be deployed
     * @return unique identifier of the created Docker image
     * @throws WebArtifactHandlerException
     */
    String buildImage(String creator, String imageName, String imageVersion, Path artifactPath)
            throws WebArtifactHandlerException;

    /**
     * returns a list images from existing Docker images specified by the creator,
     * image name and image version
     *
     * @param creator   the name of the deployer
     * @param imageName the Docker image identifier
     * @param version   the Docker image version
     * @return a list images from existing Docker images specified by the creator,
     * image name and image version
     */
    List<Image> getExistingImages(String creator, String imageName, String version) throws WebArtifactHandlerException;

    /**
     * deletes the specified Docker image
     *
     * @param creator      the name of the deployer
     * @param imageName    the Docker image identifier
     * @param imageVersion the Docker image version
     * @return unique identifier of the deleted Docker image
     * @throws WebArtifactHandlerException
     */
    String removeImage(String creator, String imageName, String imageVersion) throws WebArtifactHandlerException;

}
