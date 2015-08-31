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
package org.wso2.carbon.kubernetes.tomcat.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class FileInput {
    // Scanner instance which reads data from the specified file
    private Scanner input;

    private static final Log LOG = LogFactory.getLog(FileInput.class);

    /**
     * initializes the Scanner instance using the name of the file specified by the String
     * @param fileName name of the file to be used with the Scanner instance
     * @throws IOException I/O error when opening the file
     * @throws SecurityException If a security manager denies read access to the file or directory
     */
    public void openFile(String fileName) throws IOException, SecurityException {
        File outputFile = new File(fileName);

        boolean exists = outputFile.exists();
        if(!exists) {
            boolean created = outputFile.createNewFile();
            if(created) {
                LOG.debug("New file " + outputFile.getAbsolutePath() + " created.");
            }
        }
        input = new Scanner(Paths.get(fileName));
    }

    /**
     * returns the String data items read from the file
     * @return list of String data items read from the file
     */
    public List<String> readDataFromFile() {
        List<String> data = new ArrayList<String>();
        if(input != null) {
            while(input.hasNextLine()) {
                data.add(input.nextLine());
            }
        }
        return data;
    }

    /**
     * closes the Scanner instance if the Scanner instance is not equal to null
     */
    public void closeFile() {
        if(input != null) {
            input.close();
        }
    }
}
