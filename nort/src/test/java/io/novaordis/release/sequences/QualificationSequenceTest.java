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

import io.novaordis.release.MockConfiguration;
import io.novaordis.release.MockOS;
import io.novaordis.release.MockReleaseApplicationRuntime;
import io.novaordis.release.ReleaseMode;
import io.novaordis.release.clad.ConfigurationLabels;
import io.novaordis.release.model.maven.MavenProject;
import io.novaordis.release.version.Version;
import io.novaordis.utilities.Files;
import io.novaordis.utilities.UserErrorException;
import io.novaordis.utilities.os.OS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/17/16
 */
public class QualificationSequenceTest extends SequenceTest {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(QualificationSequenceTest.class);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File scratchDirectory;
    private File baseDirectory;

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Before
    public void before() throws Exception {

        String projectBaseDirName = System.getProperty("basedir");
        scratchDirectory = new File(projectBaseDirName, "target/test-scratch");
        assertTrue(scratchDirectory.isDirectory());

        baseDirectory = new File(System.getProperty("basedir"));
        assertTrue(baseDirectory.isDirectory());

        System.setProperty("os.class", MockOS.class.getName());
    }

    @After
    public void after() throws Exception {

        ((MockOS) OS.getInstance()).reset();

        System.clearProperty("os.class");

        assertTrue(Files.rmdir(scratchDirectory, false));
    }

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void osCommandThatRunsAllTestsNotConfigured() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();

        File crtDir = new File(scratchDirectory, "test");
        assertTrue(Files.mkdir(crtDir));
        File pf = new File(crtDir, "test-pom.xml");
        Files.cp(new File(System.getProperty("basedir"), "src/test/resources/data/maven/pom-sample-snapshot.xml"), pf);

        MavenProject mp = new MavenProject(pf);

        //
        // insure that the command to execute all tests is not configured
        //
        assertNull(mc.get(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS));

        QualificationSequence s = new QualificationSequence();
        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, true, null);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("the OS command to use to execute all tests was not configured for this project", msg);
        }

        //
        // make sure the work are was not affected
        //

        pf = new File(crtDir, "test-pom.xml");
        MavenProject mp2 = new MavenProject(pf);
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), mp2.getVersion());
    }

    @Test
    public void dotVersionFoundInWorkArea() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS, "something");

        MockOS mockOS = (MockOS) OS.getInstance();
        mockOS.addToCommandsThatSucceed(mc.get(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS));

        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();

        File original = new File(System.getProperty("basedir"), "src/test/resources/data/maven/pom-sample-dot.xml");
        File crtDir = new File(scratchDirectory, "test");
        assertTrue(Files.mkdir(crtDir));
        File work = new File(crtDir, "test-pom.xml");
        Files.cp(original, work);

        MavenProject mp = new MavenProject(work);

        QualificationSequence s = new QualificationSequence();
        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, true, null);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals(
                    "the current version (1.2.3) is not a snapshot version, cannot start the release sequence", msg);
        }

        //
        // make sure the work are was not affected
        //

        assertTrue(Files.identical(original, work));
    }

    @Test
    public void testsFailed() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();

        File originalPom = new File(
                System.getProperty("basedir"), "src/test/resources/data/maven/pom-sample-snapshot.xml");
        File crtDir = new File(scratchDirectory, "test");
        assertTrue(Files.mkdir(crtDir));
        File actualPom = new File(crtDir, "test-pom.xml");
        Files.cp(originalPom, actualPom);

        MavenProject mp = new MavenProject(actualPom);

        //
        // instruct the mock OS instance to fail when running tests
        //

        MockOS mockOS = (MockOS)OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS, "mock all test execution");
        mockOS.addToCommandsThatFail("mock all test execution");

        QualificationSequence s = new QualificationSequence();
        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, null, true, null);

        try {

            s.execute(c);
            fail("should throw Exception");
        }
        catch(UserErrorException e) {

            String msg = e.getMessage();
            log.info(msg);
            assertEquals("tests failed", msg);
        }

        assertTrue(c.wereTestsExecuted());

        //
        // make sure the work are was not affected
        //

        assertTrue(Files.identical(originalPom, actualPom));
    }

    @Test
    public void successfulExecution_SnapshotRelease_SnapshotCurrentVersion() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-snapshot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);

        //
        // instruct the mock OS instance to succeed when running mock tests
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS, "mock all test execution");
        mockOS.addToCommandsThatSucceed("mock all test execution");

        QualificationSequence s = new QualificationSequence();

        //
        // snapshot release - current version is snapshot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, ReleaseMode.snapshot, true, null);

        assertTrue(mp.getVersion().isSnapshot());

        //
        //
        //

        boolean stateChanged = s.execute(c);

        assertFalse(stateChanged);

        //
        // make sure the typed context state we rely on was properly set
        //
        assertTrue(c.wereTestsExecuted());
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), c.getCurrentVersion());

        //
        // the release metadata is not supposed to be incremented by this sequence
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.3-SNAPSHOT-4"), mp2.getVersion());
    }

    @Test
    public void successfulExecution_DotRelease_SnapshotCurrentVersion() throws Exception {

        MockConfiguration mc = new MockConfiguration();
        MockReleaseApplicationRuntime mr = new MockReleaseApplicationRuntime();
        File testPom = new File(new File(scratchDirectory, "test"), "test-pom.xml");
        assertTrue(Files.mkdir(testPom.getParentFile()));
        assertTrue(Files.cp(new File(baseDirectory, "src/test/resources/data/maven/pom-sample-snapshot.xml"), testPom));

        MavenProject mp = new MavenProject(testPom);


        //
        // instruct the mock OS instance to succeed when running mock tests
        //
        MockOS mockOS = (MockOS) OS.getInstance();
        mc.set(ConfigurationLabels.OS_COMMAND_TO_EXECUTE_ALL_TESTS, "mock all test execution");
        mockOS.addToCommandsThatSucceed("mock all test execution");

        QualificationSequence s = new QualificationSequence();

        //
        // dot release - current version is snapshot
        //
        SequenceExecutionContext c = new SequenceExecutionContext(mc, mr, mp, ReleaseMode.patch, true, null);

        assertTrue(mp.getVersion().isSnapshot());

        //
        //
        //

        boolean stateChanged = s.execute(c);

        assertTrue(stateChanged);

        //
        // make sure the typed context state we rely on was properly set
        //
        assertTrue(c.wereTestsExecuted());
        assertEquals(new Version("1.2.3"), c.getCurrentVersion());

        //
        // the release metadata should be incremented by this sequence
        //

        MavenProject mp2 = new MavenProject(testPom);
        assertEquals(new Version("1.2.3"), mp2.getVersion());
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    @Override
    protected QualificationSequence getSequenceToTest() throws Exception {

        return new QualificationSequence();
    }

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
