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

import java.io.File;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/24/16
 */
public class MockArtifact implements Artifact {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private ArtifactType t;
    private File f;

    // Constructors ----------------------------------------------------------------------------------------------------

    public MockArtifact(ArtifactType t, File f) {
        this.t = t;
        this.f = f;
    }

    // Artifact implementation -----------------------------------------------------------------------------------------

    @Override
    public ArtifactType getType() {

        return t;
    }

    @Override
    public File getRepositoryFile() {

        return f;
    }

    @Override
    public File getLocalFile() {
        throw new RuntimeException("getLocalFile() NOT YET IMPLEMENTED");
    }


    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
