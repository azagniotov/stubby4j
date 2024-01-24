/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

@GeneratedCodeClassCoverageExclusion
public final class JarUtils {

    private JarUtils() {}

    public static String readManifestImplementationVersion() {
        try {
            final Manifest manifest = getManifest();

            return manifest.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            return "x.x.xx";
        }
    }

    public static String readManifestBuiltDate() {
        try {
            final Manifest manifest = getManifest();

            final String builtDate = manifest.getMainAttributes().getValue("Built-Date");
            if (builtDate != null) {
                return builtDate;
            }
        } catch (Exception e) {
            return DateTimeUtils.systemDefault();
        }

        return DateTimeUtils.systemDefault();
    }

    private static Manifest getManifest() throws IOException {

        final InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");

        return new Manifest(inputStream);
    }
}
