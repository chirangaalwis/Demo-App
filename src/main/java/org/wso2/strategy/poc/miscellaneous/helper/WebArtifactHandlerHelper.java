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
package org.wso2.strategy.poc.miscellaneous.helper;

import org.wso2.strategy.poc.miscellaneous.io.FileOutputThread;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * a Java class which consists of various application specific utility methods
 * which are reusable by more than one class
 */
public class WebArtifactHandlerHelper {
    /**
     * returns an application specific Kubernetes component identifier
     *
     * @param artifactCreator name of the web artifact creator
     * @param artifactName    name of the web artifact
     * @return an application specific Kubernetes component identifier
     */
    public static String generateKubernetesComponentIdentifier(String artifactCreator, String artifactName) {
        if ((artifactCreator != null) && (artifactName != null)) {
            return artifactName + "-" + artifactCreator;
        } else {
            return null;
        }
    }

    /**
     * utility method which generates a Docker image name
     *
     * @param creator              creator of the Docker image
     * @param deployedArtifactName name of the artifact deployed
     * @param version              deployed version of the image
     * @return Docker image identifier based on the data provided, if either one or both the
     * creator or imageName is/are null, null is returned
     */
    public static String generateImageIdentifier(String creator, String deployedArtifactName, String version) {
        String imageIdentifier;
        if ((creator != null) && (deployedArtifactName != null)) {
            if ((version == null) || (version.equals(""))) {
                imageIdentifier = creator + "/" + deployedArtifactName + ":latest";
            } else {
                imageIdentifier = creator + "/" + deployedArtifactName + ":" + version;
            }
        } else {
            imageIdentifier = null;
        }
        return imageIdentifier;
    }

    /**
     * utility method which returns the name of the web artifact specified
     *
     * @param artifactPath path to the web artifact
     * @return the name of the web artifact specified
     */
    public static String getArtifactName(Path artifactPath) {
        String artifactFileName = artifactPath.getFileName().toString();
        return artifactFileName.substring(0, artifactFileName.length() - 4);
    }

    /**
     * compares two web artifact version builds and indicates which version should come before and after
     *
     * @param buildIdentifierOne web artifact version build one
     * @param buildIdentifierTwo web artifact version build two
     * @return indicates which version should come before and after
     */
    public static int compareBuildVersions(String buildIdentifierOne, String buildIdentifierTwo) {
        int result;
        String[] buildIdentifierOneTenantSplit = buildIdentifierOne.split(":");
        String[] buildIdentifierTwoTenantSplit = buildIdentifierTwo.split(":");
        String[] buildIdentifierOneIdentifierSplit = buildIdentifierOneTenantSplit[1].split("-");
        String[] buildIdentifierTwoIdentifierSplit = buildIdentifierTwoTenantSplit[1].split("-");
        int repoIndex = 0;
        int versionIndex = 0;
        int yearIndex = 1;
        int monthIndex = 2;
        int dayIndex = 3;
        String identifierOne =
                buildIdentifierOneTenantSplit[repoIndex] + ":" + buildIdentifierOneIdentifierSplit[versionIndex] +
                        "-" + buildIdentifierOneIdentifierSplit[yearIndex] + "-"
                        + buildIdentifierOneIdentifierSplit[monthIndex] +
                        "-" + buildIdentifierOneIdentifierSplit[dayIndex];
        String identifierTwo =
                buildIdentifierTwoTenantSplit[repoIndex] + ":" + buildIdentifierTwoIdentifierSplit[versionIndex] +
                        "-" + buildIdentifierTwoIdentifierSplit[yearIndex] + "-"
                        + buildIdentifierTwoIdentifierSplit[monthIndex] +
                        "-" + buildIdentifierTwoIdentifierSplit[dayIndex];

        if (identifierOne.compareTo(identifierTwo) < 0) {
            result = -1;
        } else if (identifierOne.compareTo(identifierTwo) > 0) {
            result = 1;
        } else {
            long identifierOneTime = Long.parseLong(buildIdentifierOneIdentifierSplit[4]);
            long identifierTwoTime = Long.parseLong(buildIdentifierTwoIdentifierSplit[4]);
            if (identifierOneTime < identifierTwoTime) {
                result = -1;
            } else if (identifierOneTime > identifierTwoTime) {
                result = 1;
            } else {
                result = 0;
            }
        }
        return result;
    }

    /**
     * a utility method which returns the version component of the Docker Image specified
     *
     * @param dockerImageName the Docker Image
     * @return the version component of the Docker Image specified
     */
    public static String getDockerImageVersion(String dockerImageName) {
        String[] imageComponents = dockerImageName.split(":");
        final int versionIndex = 1;
        return imageComponents[versionIndex];
    }

    /**
     * utility method which writes content to an external file
     *
     * @param filePath path to the file to which content are to be written
     * @param data     content to be written to the file
     */
    public static void writeToFile(File filePath, List<String> data) {
        FileOutputThread outputThread = new FileOutputThread(filePath.getAbsolutePath(), data);
        outputThread.run();
    }
}
