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
package org.wso2.strategy.poc.docker.interfaces;

import com.spotify.docker.client.messages.Image;
import org.wso2.strategy.poc.miscellaneous.exceptions.WebArtifactHandlerException;

import java.nio.file.Path;
import java.util.List;

/**
 * a Java interface for handling web artifact deployment in Docker Images
 */
public interface IDockerImageHandler {
    /**
     * builds up a Docker image which deploys the specified artifact
     *
     * @param creator              name of the person deploying the web artifact
     * @param deployedArtifactName name of the artifact to be deployed
     * @param version              Docker Image version
     * @param artifactPath         artifact to be deployed
     * @return unique identifier of the created Docker Image.
     * if at least one of either the creator or artifact name equals null, null is returned
     * @throws WebArtifactHandlerException
     */
    String buildImage(String creator, String deployedArtifactName, String version, Path artifactPath)
            throws WebArtifactHandlerException;

    /**
     * returns a list images from existing Docker images specified by the creator,
     * image name and image version
     *
     * @param creator              name of the person deploying the web artifact
     * @param deployedArtifactName name of the artifact deployed
     * @param version              Docker Image version
     * @return a list images from existing Docker images specified by the creator,
     * image name and image version
     */
    List<Image> getExistingImages(String creator, String deployedArtifactName, String version)
            throws WebArtifactHandlerException;

    /**
     * deletes the specified Docker image
     *
     * @param creator              name of the person deploying the web artifact
     * @param deployedArtifactName name of the artifact deployed
     * @param version              Docker Image version
     * @return unique identifier of the deleted Docker Image
     * @throws WebArtifactHandlerException
     */
    String removeImage(String creator, String deployedArtifactName, String version) throws WebArtifactHandlerException;
}
