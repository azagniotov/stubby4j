package io.github.azagniotov.stubby4j.cli;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import org.eclipse.jetty.util.log.Logger;

/**
 * Class used to suppress default console output of Jetty
 *
 * @author Eric Mrak
 */
@GeneratedCodeCoverageExclusion
public final class EmptyLogger implements Logger {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void warn(final String s, final Object... objects) {
    }

    @Override
    public void warn(final Throwable throwable) {
    }

    @Override
    public void warn(final String s, final Throwable throwable) {
    }

    @Override
    public void info(final String s, final Object... objects) {
    }

    @Override
    public void info(final Throwable throwable) {
    }

    @Override
    public void info(final String s, final Throwable throwable) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(final boolean b) {
    }

    @Override
    public void debug(final String s, final Object... objects) {
    }

    @Override
    public void debug(final String msg, final long value) {

    }

    @Override
    public void debug(final Throwable throwable) {
    }

    @Override
    public void debug(final String s, final Throwable throwable) {
    }

    @Override
    public Logger getLogger(final String s) {
        return this;
    }

    @Override
    public void ignore(final Throwable throwable) {
    }
}
