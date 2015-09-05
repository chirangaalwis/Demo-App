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
package org.wso2.carbon.webartifact;

import org.wso2.carbon.exceptions.WebArtifactHandlerException;
import org.wso2.carbon.webartifact.interfaces.IWebArtifactHandler;

public class WebArtifactTester {
    public static void main(String[] args) {
        try {
            IWebArtifactHandler webArtifactHandler = new WebArtifactHandler(WebArtifactTestConstants.ENDPOINT_URL);

//            webArtifactHandler.deploy(WebArtifactTestConstants.TENANT_NAME, WebArtifactTestConstants.APP_NAME,
//                    WebArtifactTestConstants.WEB_APP_PATH, WebArtifactTestConstants.VERSION, 3);
            webArtifactHandler.remove(WebArtifactTestConstants.TENANT_NAME,
                    WebArtifactTestConstants.APP_NAME, WebArtifactTestConstants.VERSION);
        } catch (WebArtifactHandlerException e) {
            e.printStackTrace();
        }
    }

}
