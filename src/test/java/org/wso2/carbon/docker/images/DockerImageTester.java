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
package org.wso2.carbon.docker.images;

import org.wso2.carbon.docker.interfaces.IDockerImageBuilder;
import org.wso2.carbon.docker.JavaWebArtifactImageBuilder;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;

public class DockerImageTester {

    public static void main(String[] args) {
        try {
            IDockerImageBuilder imageBuilder = new JavaWebArtifactImageBuilder();
            //            imageBuilder.buildImage(DockerImageTestConstants.WEB_APP_PATH);
            imageBuilder.buildImage(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION, DockerImageTestConstants.WEB_APP_PATH);
            /*imageBuilder.removeImage(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION);*/
        } catch (WebArtifactHandlerException e) {
            e.printStackTrace();
        }
        /*try {
            IDockerWebAppImageBuilder builder = new DockerWebAppImageBuilder();

            // uncomment when running tests

            // image build test
            builder.buildImage(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION, DockerImageTestConstants.WEB_APP_PATH);

            // image remove test
            *//*builder.removeImages(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION);*//*
        } catch (DockerImageBuilderException e) {
            e.printStackTrace();
        }*/
    }

}
