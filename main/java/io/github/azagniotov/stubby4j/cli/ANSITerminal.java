/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.azagniotov.stubby4j.cli;

/**
 * Prints to console any given message using ANSI colours.
 *
 * @author Eric Mrak
 */
public final class ANSITerminal {

    private static final char ESCAPE = 27;
    private static final String BOLD = String.format("%s[1m", ESCAPE);
    private static final String RESET = String.format("%s[0m", ESCAPE);
    private static final String BLACK = String.format("%s[30m", ESCAPE);
    private static final String BOLD_BLACK = String.format("%s%s", BOLD, BLACK);
    private static final String BLUE = String.format("%s[34m", ESCAPE);
    private static final String CYAN = String.format("%s[36m", ESCAPE);
    private static final String GREEN = String.format("%s[32m", ESCAPE);
    private static final String MAGENTA = String.format("%s[35m", ESCAPE);
    private static final String RED = String.format("%s[31m", ESCAPE);
    private static final String YELLOW = String.format("%s[33m", ESCAPE);
    private static boolean mute = false;

    private ANSITerminal() {

    }

    private static void print(final String color, final String msg) {
        if (mute) {
            return;
        }
        System.out.println(String.format("%s%s%s", color, msg, RESET));
    }

    public static void log(final String msg) {
        print("", msg);
    }

    /**
     * Prints message to the console
     *
     * @param msg message to to print to the console
     */
    public static void dump(final String msg) {
        log(msg);
    }

    /**
     * Prints message to the console in black colour
     *
     * @param msg message to to print to the console
     */
    public static void status(final String msg) {
        print(BOLD_BLACK, msg);
    }

    /**
     * Prints message to the console in blue colour
     *
     * @param msg message to to print to the console
     */
    public static void info(final String msg) {
        print(BLUE, msg);
    }

    /**
     * Prints message to the console in green colour
     *
     * @param msg message to to print to the console
     */
    public static void ok(final String msg) {
        print(GREEN, msg);
    }

    /**
     * Prints message to the console in red colour
     *
     * @param msg message to to print to the console
     */
    public static void error(final String msg) {
        print(RED, msg);
    }

    /**
     * Prints message to the console in yellow colour
     *
     * @param msg message to to print to the console
     */
    public static void warn(final String msg) {
        print(YELLOW, msg);
    }

    /**
     * Prints message to the console in cyan colour
     *
     * @param msg message to to print to the console
     */
    public static void incoming(final String msg) {
        print(CYAN, msg);
    }

    /**
     * Prints message to the console in magenta colour
     *
     * @param msg message to to print to the console
     */
    public static void loaded(final String msg) {
        print(MAGENTA, msg);
    }

    /**
     * Disables console output
     *
     * @param isMute if true, the console output will be disabled
     */
    public static void muteConsole(final boolean isMute) {
        mute = isMute;
    }

    /**
     * Checks whether console output has been disabled by user using command line argument
     *
     * @return true is the console output is disabled
     */
    public static boolean isMute() {
        return mute;
    }
}
