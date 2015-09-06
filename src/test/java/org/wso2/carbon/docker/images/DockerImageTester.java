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

import com.spotify.docker.client.messages.Image;
import org.wso2.carbon.docker.interfaces.IDockerImageHandler;
import org.wso2.carbon.docker.JavaWebArtifactImageHandler;
import org.wso2.carbon.exceptions.WebArtifactHandlerException;

import java.util.List;

public class DockerImageTester {

    public static void main(String[] args) {
        try {
            IDockerImageHandler imageBuilder = new JavaWebArtifactImageHandler();
            //            imageBuilder.buildImage(DockerImageTestConstants.WEB_APP_PATH);
            /*imageBuilder.buildImage(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION, DockerImageTestConstants.WEB_APP_PATH);*/
            /*imageBuilder.removeImage(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME,
                    DockerImageTestConstants.VERSION);*/

            List<Image> images = imageBuilder.getExistingImages(DockerImageTestConstants.TENANT_NAME,
                    DockerImageTestConstants.APP_NAME, DockerImageTestConstants.VERSION);
            for(Image image : images) {
                System.out.println(image.id());

                for(String tag : image.repoTags()) {
                    System.out.println(tag);
                }

                System.out.println();
            }



//            System.out.println(imageBuilder.getExistingImageList(DockerImageTestConstants.TENANT_NAME, DockerImageTestConstants.APP_NAME).size());
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
