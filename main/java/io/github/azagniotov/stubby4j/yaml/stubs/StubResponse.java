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

package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubResponse {

    public static final String STUBBY_RESOURCE_ID_HEADER = "x-stubby-resource-id";

    private final String status;
    private final String body;
    private final File file;
    private final byte[] fileBytes;
    private final String latency;
    private final Map<String, String> headers;

    public StubResponse(final String status,
                        final String body,
                        final File file,
                        final String latency,
                        final Map<String, String> headers) {
        this.status = ObjectUtils.isNull(status) ? "200" : status;
        this.body = body;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.latency = latency;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<>() : headers;
    }

    public String getStatus() {
        return status;
    }

    public String getBody() {
        return (StringUtils.isSet(body) ? body : "");
    }

    public boolean isRecordingRequired() {
        final String body = getBody();
        return StringUtils.toLower(body).startsWith("http");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getLatency() {
        return latency;
    }

    /**
     * Used by reflection when populating stubby admin page with stubbed information
     */
    public byte[] getFile() {
        return fileBytes;
    }

    public File getRawFile() {
        return file;
    }

    public String getRawFileAbsolutePath() {
        return file.getAbsolutePath();
    }

    public byte[] getResponseBodyAsBytes() {

        if (fileBytes.length == 0) {
            return StringUtils.getBytesUtf8(getBody());
        }
        return fileBytes;
    }

    public boolean isBodyContainsTemplateTokens() {
        final boolean isFileTemplate = fileBytes.length != 0 && isTemplateFile();
        return isFileTemplate || StringUtils.isTokenized(getBody());
    }

    public boolean isFilePathContainsTemplateTokens() {
        try {
            return FileUtils.isFilePathContainTemplateTokens(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private boolean isTemplateFile() {
        try {
            return FileUtils.isTemplateFile(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private byte[] getFileBytes() {
        try {
            return FileUtils.fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public boolean hasHeaderLocation() {
        return getHeaders().containsKey("location");
    }

    void addResourceIDHeader(final int resourceIndex) {
        getHeaders().put(STUBBY_RESOURCE_ID_HEADER, String.valueOf(resourceIndex));
    }

    public StubResponseTypes getStubResponseType() {
        return StubResponseTypes.OK_200;
    }

    public static StubResponse newStubResponse() {
        return new StubResponse(null, null, null, null, null);
    }

    public static StubResponse newStubResponse(final String status, final String body) {
        return new StubResponse(status, body, null, null, null);
    }
}