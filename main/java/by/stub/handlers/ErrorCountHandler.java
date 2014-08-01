package by.stub.handlers;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.server.JettyContext;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorCountHandler extends AbstractHandler {
    public static int errorCount;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);
        ConsoleUtils.logIncomingRequest(request);

        baseRequest.setHandled(true);
        wrapper.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
        wrapper.setStatus(HttpStatus.OK_200);
        wrapper.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

        try {
            final String successMessage = String.valueOf(errorCount);
            wrapper.getWriter().println(successMessage);
            ANSITerminal.ok(successMessage);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }


    }
}
