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

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tests CloudstackRunner
 */
public class CloudstackRunnerTest {
    @Test
    public void testRun() throws Exception {
        try {
            CloudstackRunner.run(new String[]{});
            Assert.fail("Main method should require at least a config properties filename arg");
        } catch (FailureException e) {
            Assert.assertEquals(e.getMessage(), CloudstackRunner.MISSING_PROPS_FN);
        }

        try {
            CloudstackRunner.run(new String[]{"/non-existent"});
            Assert.fail("Sending a non existent filename to should fail");
        } catch (RuntimeException e) {
            // expected
        }

        try {
            CloudstackRunner.run(new String[]{"/dev/null"});
            Assert.fail("Properties file should contain some required parameters");
        } catch (RuntimeException e) {
            // expected
        }

        File f = null;
        try {
            f = makeTempFile("log4j.config.file=/non_existent");

            CloudstackRunner.run(new String[] { f.getAbsolutePath() });
        } catch (FailureException e) {
            // expected
        }  finally {
            if (f != null) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
    }

    private File makeTempFile(String contents) throws IOException {
        File f  = File.createTempFile("temp_file", ".properties");
        FileWriter fw = new FileWriter(f);
        try {
            fw.write(contents);
        } finally {
            fw.close();
        }
        return f;
    }
}
