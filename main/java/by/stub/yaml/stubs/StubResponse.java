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

package by.stub.yaml.stubs;

import by.stub.annotations.CoberturaIgnore;
import by.stub.utils.FileUtils;
import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubResponse {

    public static final String STUBBY_RESOURCE_ID_HEADER = "x-stubby-resource-id";

    protected String status;
    protected String body;
    protected File file;
    protected byte[] fileBytes;
    protected String latency;
    private final StubCallback callback; // Private so that StubCallback that extends StubResponse doesn't allow chaining of Callback
    protected Map<String, String> headers;
    protected String capture = "false";

    public StubResponse() {
        this.status = "200";
        this.body = "";
        this.file = null;
        this.fileBytes = new byte[]{};
        this.latency = "0";
        this.headers = new LinkedHashMap<String, String>();
        this.callback = null;
    }

    public StubResponse(final String status, final String body,
                        final File file, final String latency,
                        final Map<String, String> headers) {
        this.status = ObjectUtils.isNull(status) ? "200" : status;
        this.body = body;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{}
                : getFileBytes();
        this.latency = latency;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<String, String>()
                : headers;
        this.callback = null;
        this.capture = "false";
    }

    public StubResponse(final String status, final String body,
                        final File file, final String latency,
                        final Map<String, String> headers, final StubCallback callback,
                        final String capture) {
        this.status = ObjectUtils.isNull(status) ? "200" : status;
        this.body = body;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{}
                : getFileBytes();
        this.latency = latency;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<String, String>()
                : headers;
        this.callback = callback;
        this.capture = ObjectUtils.isNull(capture) ? "false" : capture;
    }

    public String getCapture(){ return capture; }

    public String getStatus() {
        return status;
    }

    public String getBody() {
        return (StringUtils.isSet(body) ? body : "");
    }

    public StubCallback getCallback() {
        return callback;
    }

    public boolean isRecordingRequired() {
        final String body = getBody();
        if (StringUtils.toLower(body).startsWith("http")) {
            return true;
        }

        return false;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getLatency() {
        return latency;
    }

    // Used by reflection when populating stubby admin page with stubbed
    // information
    public byte[] getFile() {
        return fileBytes;
    }

    public File getRawFile() {
        return file;
    }

    public byte[] getResponseBodyAsBytes() {

        if (fileBytes.length == 0) {
            return getBody().getBytes(StringUtils.charsetUTF8());
        }
        return fileBytes;
    }

    public boolean isContainsTemplateTokens() {
        final boolean isFileTemplate = fileBytes.length == 0 ? false
                : isTemplateFile();
        return isFileTemplate
                || getBody().contains(StringUtils.TEMPLATE_TOKEN_LEFT);
    }

    public boolean isContainsCallback() {
        if (callback != null) {
            return true;
        }
        return false;
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

    void addResourceIDHeader(final int httplifeCycleIndex) {
        getHeaders().remove(STUBBY_RESOURCE_ID_HEADER);
        final Map<String, String> shuffledHeaders = new LinkedHashMap<String, String>();
        shuffledHeaders.put(STUBBY_RESOURCE_ID_HEADER,
                String.valueOf(httplifeCycleIndex));
        shuffledHeaders.putAll(new LinkedHashMap<String, String>(getHeaders()));
        getHeaders().clear();
        getHeaders().putAll(shuffledHeaders);
    }

    public StubResponseTypes getStubResponseType() {
        return StubResponseTypes.OK_200;
    }

    public static StubResponse newStubResponse() {
        return new StubResponse(null, null, null, null, null,null,null);
    }

    public static StubResponse newStubResponse(final String status,
                                               final String body) {
        return new StubResponse(status, body, null, null, null,null,null);
    }

    public boolean isCaptureOn() {
        return Boolean.valueOf(capture);
    }
}