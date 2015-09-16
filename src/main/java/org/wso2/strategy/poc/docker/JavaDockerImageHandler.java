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
package org.wso2.strategy.poc.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.strategy.poc.docker.interfaces.IDockerImageHandler;
import org.wso2.strategy.poc.miscellaneous.exceptions.WebArtifactHandlerException;
import org.wso2.strategy.poc.miscellaneous.helper.WebArtifactHandlerHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * a Java class which implements IDockerImageHandler Java interface
 */
public class JavaDockerImageHandler implements IDockerImageHandler {
    private final DockerClient dockerClient;
    private static final Logger LOG = LogManager.getLogger(JavaDockerImageHandler.class);

    public JavaDockerImageHandler(String dockerEndpointURI) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new DockerClient.");
        }
        dockerClient = DefaultDockerClient.builder().uri(dockerEndpointURI).build();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating new DockerClient[docker-client]: %s.", dockerClient));
        }
    }

    public String buildImage(String creator, String deployedArtifactName, String version, Path artifactPath)
            throws WebArtifactHandlerException {
        String dockerImageName = WebArtifactHandlerHelper
                .generateImageIdentifier(creator, deployedArtifactName, version);
        try {
            if (dockerImageName != null) {
                /*
                sets up the environment by creating a new Dockerfile for the specified
                web-artifact deployment
                 */
                setupEnvironment(artifactPath);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Creating a new Apache Tomcat based "
                                    + "Docker image for the [web-artifact] %s web artifact.",
                            artifactPath.getFileName()));
                }
                dockerClient.build(artifactPath.getParent(), dockerImageName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Created a new Apache Tomcat based "
                                    + "Docker image for the [web-artifact] %s web artifact.",
                            artifactPath.getFileName()));
                }
            }
        } catch (Exception exception) {
            String message = String.format("Could not create the Docker image[docker-image]: %s.", dockerImageName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
        return dockerImageName;
    }

    public List<Image> getExistingImages(String creator, String deployedArtifactName, String version)
            throws WebArtifactHandlerException {
        List<Image> matchingImageList = new ArrayList<>();
        String imageIdentifier = WebArtifactHandlerHelper
                .generateImageIdentifier(creator, deployedArtifactName, version);
        try {
            if (imageIdentifier != null) {
                List<Image> tempImages = dockerClient.listImages();
                for (Image image : tempImages) {
                    for (String tag : image.repoTags()) {
                        if (tag.contains(imageIdentifier)) {
                            if (!matchingImageList.contains(image)) {
                                matchingImageList.add(image);
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            String message = "Could not load the repo images.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
        return matchingImageList;
    }

    public String removeImage(String creator, String deployedArtifactName, String version)
            throws WebArtifactHandlerException {
        String dockerImageName = WebArtifactHandlerHelper
                .generateImageIdentifier(creator, deployedArtifactName, version);
        try {
            List<Image> existingImages = getExistingImages(creator, deployedArtifactName, version);
            if ((dockerImageName != null) && (existingImages.size() > 0)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Removing the Docker image [docker-image]: %s.", dockerImageName));
                }
                dockerClient.removeImage(dockerImageName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Removed the Docker image [docker-image]: %s.", dockerImageName));
                }
            }
        } catch (Exception exception) {
            String message = String
                    .format("Could not remove the docker image[docker-image]: " + "%s.", dockerImageName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
        return dockerImageName;
    }

    /**
     * utility method which sets up the environment required to build up an
     * Apache Tomcat based Docker image for the selected web-artifact
     *
     * @param filePath path to the web-artifact
     * @throws IOException
     */
    private void setupEnvironment(Path filePath) throws IOException, SecurityException {
        Path parentDirectory = filePath.getParent();
        File dockerFile;
        if (parentDirectory != null) {
            String parentDirectoryPath = parentDirectory.toString();
            dockerFile = new File(parentDirectoryPath + File.separator + "Dockerfile");
        } else {
            dockerFile = new File("Dockerfile");
        }
        boolean exists = dockerFile.exists();
        if (!exists) {
            boolean created = dockerFile.createNewFile();
            if (created) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("New Dockerfile created for " + filePath.toString() + ".");
                }
            }
        }
        // get base Apache Tomcat Dockerfile content from the application's file
        List<String> baseDockerFileContent;
        baseDockerFileContent = getTomcatDockerFileContent();
        /*
            set up a new Dockerfile with the specified WAR file deploying command in the Apache
            Tomcat server
        */
        baseDockerFileContent.add(2, "ADD " + filePath.getFileName().toString() + " /usr/local/tomcat/webapps/");
        WebArtifactHandlerHelper.writeToFile(dockerFile, baseDockerFileContent);
    }

    /**
     * returns a String list of base content to be written to the Apache
     * Tomcat based Dockerfile
     *
     * @return base content to be written to the Apache Tomcat based Dockerfile
     */
    private List<String> getTomcatDockerFileContent() {
        List<String> baseContent = new ArrayList<>();
        baseContent.add("FROM tomcat");
        baseContent.add("MAINTAINER user");
        baseContent.add("CMD [\"catalina.sh\", \"run\"]");
        return baseContent;
    }
}
