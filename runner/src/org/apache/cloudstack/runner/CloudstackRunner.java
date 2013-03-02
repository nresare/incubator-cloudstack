// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.runner;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Main class for that starts an embedded jetty servlet environment suitable for running the cloudstack web
 * applications.
 */
public class CloudstackRunner {
    static final String MISSING_PROPS_FN = "Required parameter RUNNER_CONFIG_PROPERTIES not set";
    static final String NON_EXISTENT_CONFIG_PROPS = "Can not open config properties files '%s'";
    static final String NON_EXISTENT_CONFIG_LOG4J = "Fail to open log4j config file '%s'";

    private static final Logger s_logger = Logger.getLogger(CloudstackRunner.class);

    /**
     * Main method that launches the the webapp configuration. The first value of args is expected to be a properties
     * file containing configuration parameters.
     *
     * @param args command line parameters
     */
    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } catch (FailureException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * This is the inner main method, since methods invoking System.exit can't be effectively tested
     *
     * @param args command line parameters. First parameter is expected to be config properties file
     */
    static void run(String[] args) throws Exception {
        if (args.length < 1) {
            throw new FailureException(MISSING_PROPS_FN);
        }
        Properties config;
        try {
            config = getConfig(args[0]);
        } catch (FileNotFoundException e) {
            throw new FailureException(NON_EXISTENT_CONFIG_PROPS, args[0]);
        }
        setupLogging((String)config.get("log4j.config.file"));
        s_logger.info("Starting CloudstackRunner version 0.1.1");

        Server server = new Server(Integer.parseInt((String) config.get("listen.port")));

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setResourceBase((String)config.get("webapp.base"));

        server.setHandler(webapp);
        server.start();
        server.join();
    }

    /**
     * Configure logging with the provided log4j config file.
     *
     * @param log4jConfigFilename filename for log4j configuration
     */
    private static void setupLogging(String log4jConfigFilename) {
        if (log4jConfigFilename == null || !new File(log4jConfigFilename).exists()) {
            throw new FailureException(NON_EXISTENT_CONFIG_LOG4J, log4jConfigFilename);
        }
        DOMConfigurator.configureAndWatch(log4jConfigFilename);
    }

    /**
     * Parse the config file and returns the corresponding Properties object
     *
     * @param configFileName the relative or absolute path to the config Properties file
     * @return a Properties instance populated with data from the given file
     * @throws IOException if filesystem operations fail
     */
    private static Properties getConfig(String configFileName) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(configFileName);
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

}
