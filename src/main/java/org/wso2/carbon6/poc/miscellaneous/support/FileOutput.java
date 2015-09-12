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

import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;

class FileOutput {
    // Formatter to output text to a specified file
    private Formatter output;

    /**
     * method takes a single String parameter specifying the name of the file on which the
     * Formatter instance is to be used
     *
     * @param fileName the name of the file on which the Formatter instance is to be used
     * @throws FileNotFoundException
     * @throws SecurityException
     */
    public void openFile(String fileName) throws FileNotFoundException, SecurityException {
        output = new Formatter(fileName);
    }

    /**
     * writes the String data items in a list to the specified file using the Formatter
     * instance
     *
     * @param data list of String data items to be written to the specified file
     */
    public void addDataToFile(List<String> data) {
        if (data != null) {
            for (String dataItem : data) {
                output.format("%s%n", dataItem);
            }
        }
    }

    /**
     * closes the Formatter instance if the Formatter instance is not equal to null
     */
    public void closeFile() {
        if (output != null) {
            output.close();
        }
    }
}
