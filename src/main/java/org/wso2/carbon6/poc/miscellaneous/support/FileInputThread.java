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
package org.wso2.carbon6.poc.miscellaneous.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FileInputThread implements Runnable {
    private String fileName;
    private List<String> fileContent;

    private static final Logger LOG = LogManager.getLogger(FileInputThread.class);

    public FileInputThread(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getFileContent() {
        if (fileContent != null) {
            return fileContent;
        } else {
            return new ArrayList<>();
        }
    }

    public void run() {
        try {
            FileInput input = new FileInput();
            input.openFile(fileName);
            List<String> data = input.readDataFromFile();
            input.closeFile();
            fileContent = data;
        } catch (Exception exception) {
            String message = "Could not input data from the external file.";
            LOG.error(message, exception);
        }
    }
}