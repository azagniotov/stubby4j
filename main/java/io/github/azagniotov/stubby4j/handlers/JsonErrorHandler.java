package io.github.azagniotov.stubby4j.handlers;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class JsonErrorHandler extends ErrorHandler {

    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException {

        final String method = request.getMethod();
        if (!HttpMethod.GET.is(method) && !HttpMethod.POST.is(method) && !HttpMethod.PUT.is(method) && !HttpMethod.HEAD.is(method)) {
            baseRequest.setHandled(true);
            return;
        }

        baseRequest.setHandled(true);
        response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());

        final ByteArrayISO8859Writer byteArrayWriter = new ByteArrayISO8859Writer(4096);
        final String reason = (response instanceof Response) ? ((Response) response).getReason() : null;

        handleErrorPage(request, byteArrayWriter, response.getStatus(), reason);

        byteArrayWriter.flush();
        response.setContentLength(byteArrayWriter.size());
        byteArrayWriter.writeTo(response.getOutputStream());
        byteArrayWriter.destroy();
    }

    @Override
    protected void writeErrorPage(final HttpServletRequest request,
                                  final Writer writer,
                                  final int code,
                                  final String message,
                                  final boolean showStacks) throws IOException {

        final String error = message == null ? HttpStatus.getMessage(code) : message;
        if (code == 404) {
            try {
                final JSONObject jsonObject = new JSONObject(error);
                jsonObject.putOpt("code", code);
                writer.write(jsonObject.toString());
            } catch (final JSONException e) {
                writer.write("{\"code\":\"" + code + "\",\"message\"=\"" + error + "\"}");
            }
        } else {
            writer.write("{\"code\":\"" + code + "\",\"message\"=\"" + error + "\"}");
        }
    }
}
