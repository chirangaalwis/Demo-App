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
package org.wso2.carbon6.poc;

import org.wso2.carbon6.poc.exceptions.WebArtifactHandlerException;
import org.wso2.carbon6.poc.webartifact.WebArtifactHandler;
import org.wso2.carbon6.poc.webartifact.interfaces.IWebArtifactHandler;

import java.lang.Object;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Executor {

    private static final String ENDPOINT_URL = "http://127.0.0.1:8080";
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            final IWebArtifactHandler webArtifactHandler = new WebArtifactHandler(ENDPOINT_URL);
            final String welcomeMessage = "***WELCOME TO JAVA WEB ARTIFACT HANDLER APP***\n\n";
            final String mainMenuContent = "1 - Deploy\n2 - Rolling update\n3 - Rollback\n"
                    + "4 - Un-deploy\n5 - Scaling\n6 - Exit\nEnter your choice: ";
            showMenu(welcomeMessage);
            while (true) {
                int userChoice;
                do {
                    showMenu(mainMenuContent);
                    userChoice = SCANNER.nextInt();
                    SCANNER.nextLine();
                } while ((userChoice < 1) || (userChoice > 6));
                process(userChoice, webArtifactHandler);
            }
        } catch (Exception e) {
            System.exit(1);
        }
    }

    private static void showMenu(String menuContent) {
        System.out.print(menuContent);
    }

    private static Map<String, Object> gatherRepositoryData() {
        Map<String, Object> inputs = new HashMap<>();

        showMenu("Tenant name: ");
        String tenant = SCANNER.next();
        SCANNER.nextLine();

        showMenu("App name: ");
        String appName = SCANNER.next();
        SCANNER.nextLine();

        // Add to list of inputs
        inputs.put("tenant", tenant);
        inputs.put("app", appName);

        return inputs;
    }

    private static Map<String, Object> gatherIdentifierData() {
        Map<String, Object> inputs;
        inputs = gatherRepositoryData();
        showMenu("App version: ");
        String version = SCANNER.nextLine();

        // Add to list of inputs
        inputs.put("version", version);
        return inputs;
    }

    private static Map<String, Object> gatherDeploymentData() {
        Map<String, Object> inputs;
        inputs = gatherIdentifierData();
        Path artifactPath;
        boolean exists = false;
        do {
            showMenu("Artifact path: ");
            String path = SCANNER.nextLine();
            artifactPath = Paths.get(path);
            if (!Files.isDirectory(artifactPath)) {
                exists = Files.exists(artifactPath);
            } else {
                showMenu("The path should not refer to a directory.");
                continue;
            }
            if (!exists) {
                showMenu("This file path does not exist.\n");
            }
        } while (!exists);
        int replicas;
        do {
            showMenu("Number of deployment replicas: ");
            replicas = SCANNER.nextInt();
            SCANNER.nextLine();
        } while ((replicas < 1));
        // Add to list of inputs
        inputs.put("artifact", artifactPath);
        inputs.put("replicas", replicas);
        return inputs;
    }

    private static Map<String, Object> gatherUpdateData() {
        Map<String, Object> inputs = gatherIdentifierData();
        Path artifactPath;
        boolean exists = false;
        do {
            showMenu("Artifact path: ");
            String path = SCANNER.nextLine();
            artifactPath = Paths.get(path);
            if (!Files.isDirectory(artifactPath)) {
                exists = Files.exists(artifactPath);
            } else {
                showMenu("The path should not refer to a directory.\n");
                continue;
            }
            if (!exists) {
                showMenu("This file path does not exist.\n");
            }
        } while (!exists);
        // Add to list of inputs
        inputs.put("artifact", artifactPath);
        return inputs;
    }

    private static Map<String, Object> gatherScalingData(IWebArtifactHandler webArtifactHandler)
            throws WebArtifactHandlerException {
        Map<String, Object> inputs;
        inputs = gatherRepositoryData();
        showMenu("Current no. of web artifact replicas running: " + webArtifactHandler
                .getNoOfReplicas((String) inputs.get("tenant"), (String) inputs.get("app")) + "\n");
        showMenu("Enter new no. of replicas: ");
        int replicas = SCANNER.nextInt();
        SCANNER.nextLine();
        // Add to list of inputs
        inputs.put("replicas", replicas);
        return inputs;
    }

    private static void process(int choice, IWebArtifactHandler webArtifactHandler) throws WebArtifactHandlerException {
        Map<String, Object> inputs;
        String tenant;
        String appName;
        String version;
        Path artifactPath;
        switch (choice) {
        case 1:
            inputs = gatherDeploymentData();
            tenant = (String) inputs.get("tenant");
            appName = (String) inputs.get("app");
            // set the image version
            version = (String) inputs.get("version");
            artifactPath = (Path) inputs.get("artifact");
            int replicas = (Integer) (inputs.get("replicas"));
            boolean deployed = webArtifactHandler.deploy(tenant, appName, artifactPath, version, replicas);
            if (deployed) {
                showMenu(webArtifactHandler.getServiceAccessIPs(tenant, appName, artifactPath));
            } else {
                showMenu("This web artifact has already been deployed. Please use a "
                        + "rolling update to make an updated deployment.\n");
            }
            break;
        case 2:
            inputs = gatherUpdateData();
            tenant = (String) inputs.get("tenant");
            appName = (String) inputs.get("app");
            version = (String) inputs.get("version");
            artifactPath = (Path) inputs.get("artifact");
            deployed = webArtifactHandler.rollUpdate(tenant, appName, version, artifactPath);
            if (deployed) {
                showMenu(webArtifactHandler.getServiceAccessIPs(tenant, appName, artifactPath));
            } else {
                showMenu("This web artifact version has not been deployed, before. "
                        + "Please deploy the artifact version, before making an updated deployment.\n");
            }
            break;
        case 3:
            inputs = gatherIdentifierData();
            tenant = (String) inputs.get("tenant");
            appName = (String) inputs.get("app");
            version = (String) inputs.get("version");
            List<String> displayLowerList = webArtifactHandler.
                    listLowerBuildArtifactVersions(tenant, appName, version);
            if ((displayLowerList != null) && (displayLowerList.size() > 0)) {
                displayList(displayLowerList);
                int userChoice;
                do {
                    showMenu("Enter your choice: ");
                    userChoice = SCANNER.nextInt();
                    SCANNER.nextLine();
                } while ((userChoice < 1) || (userChoice > displayLowerList.size()));
                webArtifactHandler.rollBack(tenant, appName, version, getListChoice(displayLowerList, userChoice));
            } else {
                showMenu("No lower web app build versions.\n");
            }
            break;
        case 4:
            inputs = gatherRepositoryData();
            tenant = (String) inputs.get("tenant");
            appName = (String) inputs.get("app");
            webArtifactHandler.remove(tenant, appName);
            break;
        case 5:
            inputs = gatherScalingData(webArtifactHandler);
            tenant = (String) inputs.get("tenant");
            appName = (String) inputs.get("app");
            int newReplicas = (Integer) inputs.get("replicas");
            webArtifactHandler.scale(tenant, appName, newReplicas);
            break;
        case 6:
            System.exit(0);
            break;
        }
    }

    private static void displayList(List<String> data) {
        if (data != null) {
            for (int count = 0; count < data.size(); count++) {
                System.out.print((count + 1) + ". " + data.get(count) + "\n");
            }
        }
    }

    private static String getListChoice(List<String> choices, int choice) {
        if ((choices != null) && (choice > 0 && choice <= choices.size())) {
            return choices.get(choice - 1);
        } else {
            return null;
        }
    }

}
