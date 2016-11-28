/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.release.sequences;

import io.novaordis.clad.application.ApplicationRuntime;
import io.novaordis.clad.configuration.Configuration;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.Project;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.NativeExecutionResult;
import io.novaordis.utilities.os.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class BuildSequence implements Sequence {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(BuildSequence.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private boolean executeChangedState;

    // Constructors ----------------------------------------------------------------------------------------------------

    public BuildSequence() {

        this.executeChangedState = false;
    }

    // Sequence implementation -----------------------------------------------------------------------------------------

    @Override
    public boolean execute(SequenceExecutionContext context) throws Exception {

        Configuration c = context.getConfiguration();
        ApplicationRuntime r = context.getRuntime();
        Project m = context.getProject();

        //
        // make sure we have the command required to build all tests; depending on where we are in the release (or
        // build) sequence, we may want to run or not to run tests
        //
        //

        boolean doWeNeedToExecuteTests = !context.wereTestsExecuted();

        String osBuildCommand;

        if (doWeNeedToExecuteTests) {

            osBuildCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITH_TESTS);
        }
        else {

            osBuildCommand = c.get(ConfigurationLabels.OS_COMMAND_TO_BUILD_WITHOUT_TESTS);
        }

        if (osBuildCommand == null) {
            throw new UserErrorException(
                    "the OS command to use to build " + (doWeNeedToExecuteTests ? "with" : "without") +
                            " tests was not configured for this project");
        }

        Version currentVersion = m.getVersion();

        log.debug("building artifacts for release " + currentVersion + " ...");

        log.debug("building with \"" + osBuildCommand + "\" ...");

        NativeExecutionResult executionResult = OS.getInstance().execute(osBuildCommand);

        if (!executionResult.isSuccess()) {

            throw new UserErrorException("build failed");
        }

        executeChangedState = true;

        if (c.isVerbose()) {
            r.info(executionResult.getStdout());
        }

        r.info(currentVersion + " build ok");

        return executeChangedState;
    }

    @Override
    public boolean undo(SequenceExecutionContext context) {

        if (!executeChangedState) {

            // noop
            return false;
        }

        throw new RuntimeException("undo() NOT YET IMPLEMENTED");
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
