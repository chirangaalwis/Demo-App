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

import java.nio.file.Path;
import java.nio.file.Paths;

public class WebArtifactTestConstants {
    protected static final String TENANT_NAME = "abc-com";
    protected static final String APP_NAME = "app";
    protected static final String VERSION = "1.0";
    protected static final Path WEB_APP_PATH = Paths
            .get("/home/chiranga/WSO2CarbonIDEA/XWebAppTest/sample/sample.war");
    protected static final String ENDPOINT_URL = "http://127.0.0.1:8080";
}
