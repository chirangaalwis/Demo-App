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
package org.wso2.carbon.docker;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;
import org.wso2.carbon.docker.interfaces.IDockerImageHandler;
import org.wso2.carbon.docker.support.FileOutputThread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * a Java class which implements IDockerImageHandler Java interface
 */
public class JavaWebArtifactImageHandler implements IDockerImageHandler {

    private final DockerClient dockerClient;
    private static final Logger LOG = LogManager.getLogger(JavaWebArtifactImageHandler.class);

    public JavaWebArtifactImageHandler() throws WebArtifactHandlerException {
        try {

            // creates a new com.spotify.docker.client.DockerClient
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating new DockerClient.");
            }
            dockerClient = DefaultDockerClient.fromEnv().build();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Creating new DockerClient[docker-client]: %s.", dockerClient));
            }

        } catch (DockerCertificateException exception) {
            String message = "Could not create a new JavaWebArtifactImageHandler instance.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
    }

    public String buildImage(String creator, String imageName, String imageVersion, Path artifactPath)
            throws WebArtifactHandlerException {
        String dockerImageName = generateImageIdentifier(creator, imageName, imageVersion);
        try {
            /*
            sets up the environment by creating a new Dockerfile for the specified
            web-artifact deployment
             */
            setupEnvironment(artifactPath);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Creating a new Apache Tomcat based " + "Docker image for the [web-artifact] %s web artifact.",
                        artifactPath.getFileName().toString()));
            }
            dockerClient.build(artifactPath.getParent(), dockerImageName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Created a new Apache Tomcat based " + "Docker image for the [web-artifact] %s web artifact.",
                        artifactPath.getFileName()));
            }

        } catch (Exception exception) {
            String message = String
                    .format("Could not create the Docker image[docker-image]: " + "%s.", dockerImageName);
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }

        return dockerImageName;
    }

    public Image getExistingImage(String creator, String imageName, String version) throws WebArtifactHandlerException {
        ImmutableList<String> tags;
        String imageIdentifier = creator + "/" + imageName + ":" + version;
        try {
            List<Image> tempImages = dockerClient.listImages();
            for (Image image : tempImages) {
                tags = image.repoTags();
                for (String tag : tags) {
                    if (tag.contains(imageIdentifier)) {
                        return image;
                    }
                }
            }
        } catch (Exception exception) {
            String message = "Could not load the repo images.";
            LOG.error(message, exception);
            throw new WebArtifactHandlerException(message, exception);
        }
        return null;
    }

    public String removeImage(String creator, String imageName, String imageVersion)
            throws WebArtifactHandlerException {
        String dockerImageName = generateImageIdentifier(creator, imageName, imageVersion);
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Removing the Docker image [docker-image]: %s.", dockerImageName));
            }
            dockerClient.removeImage(dockerImageName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Removed the Docker image [docker-image]: %s.", dockerImageName));
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
    private void setupEnvironment(Path filePath) throws IOException {
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
        baseDockerFileContent = getDockerFileContent();

        /*
        set up a new Dockerfile with the specified WAR file deploying command in the Apache
        Tomcat server
        */
        baseDockerFileContent.add(2, "ADD " + filePath.getFileName().toString() + " /usr/local/tomcat/webapps/");
        setWebAppDockerFile(dockerFile, baseDockerFileContent);
    }

    /**
     * utility method which writes content to an external file
     *
     * @param filePath path to the file to which content are to be written
     * @param data     content to be written to the file
     */
    private void setWebAppDockerFile(File filePath, List<String> data) {
        FileOutputThread outputThread = new FileOutputThread(filePath.getAbsolutePath(), data);
        outputThread.run();
    }

    /**
     * returns a String list of base content to be written to the Apache
     * Tomcat based Dockerfile
     *
     * @return base content to be written to the Apache Tomcat based Dockerfile
     */
    private List<String> getDockerFileContent() {
        List<String> baseContent = new ArrayList<>();

        baseContent.add("FROM tomcat");
        baseContent.add("MAINTAINER user");
        baseContent.add("CMD [\"catalina.sh\", \"run\"]");

        return baseContent;
    }

    /**
     * utility method which generates a Docker image name
     *
     * @param creator      creator of the Docker image
     * @param imageName    name of the Docker image name
     * @param imageVersion deployed version of the image
     * @return Docker image identifier based on the data provided
     */
    private String generateImageIdentifier(String creator, String imageName, String imageVersion) {
        String imageIdentifier;
        if ((imageVersion == null) || (imageVersion.equals(""))) {
            imageIdentifier = creator + "/" + imageName + ":latest";
        } else {
            imageIdentifier = creator + "/" + imageName + ":" + imageVersion;
        }
        return imageIdentifier;
    }

}
