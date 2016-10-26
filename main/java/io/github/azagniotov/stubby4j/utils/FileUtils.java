/*
A Java-based HTTP stub server

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

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 11/8/12, 8:30 AM
 */
@SuppressWarnings("serial")
public final class FileUtils {

    private static final Set<String> ASCII_TYPES = Collections.unmodifiableSet(
            new HashSet<>(
                    Arrays.asList(
                            ".ajx", ".am", ".asa", ".asc", ".asp", ".aspx", ".awk", ".bat",
                            ".c", ".cdf", ".cf", ".cfg", ".cfm", ".cgi", ".cnf", ".conf", ".cpp",
                            ".css", ".csv", ".ctl", ".dat", ".dhtml", ".diz", ".file", ".forward",
                            ".grp", ".h", ".hpp", ".hqx", ".hta", ".htaccess", ".htc", ".htm", ".html",
                            ".htpasswd", ".htt", ".htx", ".in", ".inc", ".info", ".ini", ".ink", ".java",
                            ".js", ".json", ".jsp", ".log", ".logfile", ".m3u", ".m4", ".m4a", ".mak",
                            ".map", ".model", ".msg", ".nfo", ".nsi", ".info", ".old", ".pas", ".patch",
                            ".perl", ".php", ".php2", ".php3", ".php4", ".php5", ".php6", ".phtml", ".pix",
                            ".pl", ".pm", ".po", ".pwd", ".py", ".qmail", ".rb", ".rbl", ".rbw", ".readme",
                            ".reg", ".rss", ".rtf", ".ruby", ".session", ".setup", ".sh", ".shtm", ".shtml",
                            ".sql", ".ssh", ".stm", ".style", ".svg", ".tcl", ".text", ".threads", ".tmpl",
                            ".tpl", ".txt", ".ubb", ".vbs", ".xhtml", ".xml", ".xrc", ".xsl", ".yaml", ".yml"
                    )
            )
    );

    //TODO Fallback to using System.lineSeparator()
    private static final String LINE_SEPARATOR_UNIX = "\n";
    private static final String LINE_SEPARATOR_MAC_OS_PRE_X = "\r";
    private static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    private static final String LINE_SEPARATOR_TOKEN = "[_T_O_K_E_N_]";
    public static final String BR;

    // Instead of hard-coding '\n', makes the new line character be specific
    // to the platform (Mac, *Nix or Win) where stubby4j is running
    static {
        final int initialSize = 4;
        final StringBuilderWriter stringBuilderWriter = new StringBuilderWriter(initialSize);
        try (final PrintWriter out = new PrintWriter(stringBuilderWriter)) {
            out.println();
            BR = stringBuilderWriter.toString();
            out.flush();
        }
    }

    private FileUtils() {

    }

    public static File uriToFile(final String dataYamlConfigParentDir, final String relativePath) throws IOException {
        final File contentFile = new File(dataYamlConfigParentDir, relativePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", relativePath));
        }

        return contentFile;
    }

    public static File uriToFile(final String absolutePath) throws IOException {
        final File contentFile = new File(absolutePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", absolutePath));
        }

        return contentFile;
    }

    @CoberturaIgnore
    public static File fileFromString(final String content) throws IOException {
        final File temp = File.createTempFile("tmp", ".tmp");
        temp.deleteOnExit();
        try (final BufferedWriter out = new BufferedWriter(new FileWriter(temp))) {
            out.write(content);
            return temp;
        }
    }

    public static boolean isTemplateFile(final File file) throws IOException {
        return isCharacterFile(file) && containsTemplateToken(characterFileToString(file));
    }

    public static boolean doesFilePathContainTemplateTokens(final File file) {
        return containsTemplateToken(file.getAbsolutePath());
    }

    public static byte[] fileToBytes(final File file) throws IOException {
        if (isCharacterFile(file)) {
            return characterFileToUtf8Bytes(file);
        }
        return binaryFileToBytes(file);
    }


    @CoberturaIgnore
    static byte[] binaryFileToBytes(final String dataYamlConfigParentDir, final String relativePath) throws IOException {
        final File contentFile = new File(dataYamlConfigParentDir, relativePath);

        if (!contentFile.isFile()) {
            throw new IOException(String.format("Could not load file from path: %s", relativePath));
        }

        return Files.readAllBytes(Paths.get(contentFile.toURI()));
    }


    @CoberturaIgnore
    static byte[] binaryFileToBytes(final File file) throws IOException {
        return Files.readAllBytes(Paths.get(file.toURI()));
    }


    public static String enforceSystemLineSeparator(final String loadedContent) {
        if (!StringUtils.isSet(loadedContent)) {
            return "";
        }

        return loadedContent
                .replace(LINE_SEPARATOR_WINDOWS, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_MAC_OS_PRE_X, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_UNIX, LINE_SEPARATOR_TOKEN)
                .replace(LINE_SEPARATOR_TOKEN, BR);
    }

    public static BufferedReader constructReader(final String content) {
        final InputStream is = new ByteArrayInputStream(content.getBytes(StringUtils.charsetUTF8()));
        final Reader reader = new InputStreamReader(is, StringUtils.charsetUTF8());

        return new BufferedReader(reader);
    }

    public static BufferedReader constructReader(final File file) throws IOException {
        return Files.newBufferedReader(Paths.get(file.toURI()));
    }

    private static String characterFileToString(final File file) throws IOException {
        final String loadedContent = StringUtils.inputStreamToString(new BufferedInputStream(new FileInputStream(file)));
        return enforceSystemLineSeparator(loadedContent);
    }

    private static byte[] characterFileToUtf8Bytes(final File file) throws IOException {
        final String loadedContent = characterFileToString(file);

        return StringUtils.getBytesUtf8(loadedContent);
    }

    private static boolean isCharacterFile(final File file) throws IOException {
        return ASCII_TYPES.contains(StringUtils.extractFilenameExtension(file.getName()));
    }

    private static boolean containsTemplateToken(final String string) {
        return string.contains(StringUtils.TEMPLATE_TOKEN_LEFT);
    }

    private static final class StringBuilderWriter extends Writer implements Serializable {

        private final StringBuilder builder;

        private StringBuilderWriter(int capacity) {
            this.builder = new StringBuilder(capacity);
        }

        @Override
        public void write(char[] value, int offset, int length) {
            builder.append(value, offset, length);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}