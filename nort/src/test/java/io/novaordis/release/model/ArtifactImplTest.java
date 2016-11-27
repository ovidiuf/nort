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

package io.novaordis.release.model;

import io.novaordis.release.version.Version;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class ArtifactImplTest extends ArtifactTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    @Test
    public void constructor() throws Exception {

        ArtifactImpl a = new ArtifactImpl(ArtifactType.JAR_LIBRARY,
                "io.test-group", "test-artifact", new Version("1.0"));

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(new File("io/test-group/test-artifact/1.0/test-artifact-1.0.jar"), a.getRepositoryFile());
    }

    @Test
    public void constructor_FinalNameSpecified() throws Exception {

        ArtifactImpl a = new ArtifactImpl(ArtifactType.JAR_LIBRARY,
                "io.test-group", "test-artifact", new Version("1.0"), "blah", null);

        assertEquals(ArtifactType.JAR_LIBRARY, a.getType());
        assertEquals(new File("io/test-group/test-artifact/1.0/blah-1.0.jar"), a.getRepositoryFile());
    }


    // equals() --------------------------------------------------------------------------------------------------------

    @Test
    public void equals() throws Exception {

        ArtifactImpl a = new ArtifactImpl(ArtifactType.JAR_LIBRARY, "io.test", "test-artifact", new Version("1.0"));
        ArtifactImpl a2 = new ArtifactImpl(ArtifactType.JAR_LIBRARY, "io.test", "test-artifact", new Version("1.0"));

        assertEquals(a, a2);
        assertEquals(a2, a);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    @Override
    protected ArtifactImpl getArtifactToTest() throws Exception {

        return new ArtifactImpl(ArtifactType.JAR_LIBRARY, "test", "test", new Version("0"));
    }

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
