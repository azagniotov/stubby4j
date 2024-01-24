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

package io.github.azagniotov.stubby4j.cli;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import org.eclipse.jetty.util.log.Logger;

/**
 * Class used to suppress default console output of Jetty
 *
 * @author Eric Mrak
 */
@GeneratedCodeClassCoverageExclusion
public final class EmptyLogger implements Logger {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void warn(final String s, final Object... objects) {}

    @Override
    public void warn(final Throwable throwable) {}

    @Override
    public void warn(final String s, final Throwable throwable) {}

    @Override
    public void info(final String s, final Object... objects) {}

    @Override
    public void info(final Throwable throwable) {}

    @Override
    public void info(final String s, final Throwable throwable) {}

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(final boolean b) {}

    @Override
    public void debug(final String s, final Object... objects) {}

    @Override
    public void debug(final String msg, final long value) {}

    @Override
    public void debug(final Throwable throwable) {}

    @Override
    public void debug(final String s, final Throwable throwable) {}

    @Override
    public Logger getLogger(final String s) {
        return this;
    }

    @Override
    public void ignore(final Throwable throwable) {}
}
