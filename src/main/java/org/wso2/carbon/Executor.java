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
package org.wso2.carbon;

import org.wso2.carbon.exceptions.WebArtifactHandlerException;
import org.wso2.carbon.webartifact.WebArtifactHandler;
import org.wso2.carbon.webartifact.interfaces.IWebArtifactHandler;

import java.lang.Object;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Executor {

    private static final String ENDPOINT_URL = "http://127.0.0.1:8080";
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            final IWebArtifactHandler webArtifactHandler = new WebArtifactHandler(ENDPOINT_URL);

            final String welcomeMessage = "***WELCOME TO JAVA WEB ARTIFACT HANDLER APP***\n\n";
            final String mainMenuContent = "1 - Deploy web artifact\n2 - Un-deploy web artifact\n"
                    + "3 - Exit\nEnter your choice: ";

            showMenu(welcomeMessage);

            while(true) {
                int userChoice;
                do{
                    showMenu(mainMenuContent);
                    userChoice = SCANNER.nextInt();
                    SCANNER.nextLine();
                }
                while((userChoice < 1) || (userChoice > 3));

                process(userChoice, webArtifactHandler);
            }
        } catch (WebArtifactHandlerException exception) {
            System.exit(1);
        }
    }

    private static void showMenu(String menuContent) {
        System.out.print(menuContent);
    }

    private static Map<String, Object> gatherDeploymentData() {
        Map<String, Object> inputs;

        inputs = gatherIdentifierData();

        int replicas;
        do {
            showMenu("Number of deployment replicas: ");
            replicas = SCANNER.nextInt();
            SCANNER.nextLine();
        }
        while((replicas < 1));

        // Add to list of inputs
        inputs.put("replicas", replicas);

        return inputs;
    }

    private static Map<String, Object> gatherIdentifierData() {
        Map<String, Object> inputs = new HashMap<>();

        showMenu("Tenant name: ");
        String tenant = SCANNER.next();
        SCANNER.nextLine();

        showMenu("App name: ");
        String appName = SCANNER.next();
        SCANNER.nextLine();

        showMenu("Artifact path: ");
        String path = SCANNER.nextLine();
        Path artifactPath = Paths.get(path);

        showMenu("App version: ");
        String version = SCANNER.nextLine();

        // Add to list of inputs
        inputs.put("tenant", tenant);
        inputs.put("app", appName);
        inputs.put("artifact", artifactPath);
        if(version.equals("")) {
            inputs.put("version", null);
        }
        else {
            inputs.put("version", version);
        }

        return inputs;
    }

    private static void process(int choice, IWebArtifactHandler webArtifactHandler)
            throws WebArtifactHandlerException {
        if(webArtifactHandler instanceof WebArtifactHandler) {
            WebArtifactHandler handler = (WebArtifactHandler) webArtifactHandler;

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
                artifactPath = (Path) inputs.get("artifact");
                version = (String) inputs.get("version");
                int replicas = (Integer) (inputs.get("replicas"));

                webArtifactHandler.deploy(tenant, appName, artifactPath, version, replicas);
                showMenu(handler.getAccessIPs(tenant, appName, artifactPath));
                break;
            case 2:
                inputs = gatherIdentifierData();

                tenant = (String) inputs.get("tenant");
                appName = (String) inputs.get("app");
                artifactPath = (Path) inputs.get("artifact");
                version = (String) inputs.get("version");

                webArtifactHandler.undeploy(tenant, appName, artifactPath, version);
                break;
            case 3:
                System.exit(0);
                break;
            }
        }

    }

}
