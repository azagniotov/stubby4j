package by.stub.handlers;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UnusedStubsHandler extends AbstractHandler {
    public static ArrayList<StubRequest> userStubRequests = new ArrayList<>();
    private List<StubHttpLifecycle> stubLifeCycles;

    public UnusedStubsHandler( StubbedDataManager stubbedDataManager) {
        this.stubLifeCycles = stubbedDataManager.getStubHttpLifecycles();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);
        ConsoleUtils.logIncomingRequest(request);
        String unusedStubs = getUnusedStubs();

        baseRequest.setHandled(true);
        wrapper.setContentType(MimeTypes.TEXT_PLAIN);
        wrapper.setStatus(HttpStatus.OK_200);
        wrapper.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

        try {
            final String successMessage = String.valueOf(unusedStubs);
            wrapper.getWriter().println(successMessage);
            ANSITerminal.ok(successMessage);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    private String getUnusedStubs() {

        List<StubHttpLifecycle> unUsedCycles = stubLifeCycles.stream().filter(cycle -> !userStubRequests.contains(cycle.getRequest())).collect(Collectors.toList());
        List<StubHttpLifecycle> usedCycles = stubLifeCycles.stream().filter(cycle -> userStubRequests.contains(cycle.getRequest())).collect(Collectors.toList());

        List<String> unUsedRequestJsons = getRequestFiles(unUsedCycles);
        List<String> unUsedResponseJsons = getResponseFiles(unUsedCycles);

        List<String> usedRequestJsons = getRequestFiles(usedCycles);
        List<String> usedResponseJsons = getResponseFiles(usedCycles);

        unUsedRequestJsons = unUsedRequestJsons.stream().filter((unUsedRequestJson) -> !usedRequestJsons.contains(unUsedRequestJson)).collect(Collectors.toList());
        unUsedResponseJsons = unUsedResponseJsons.stream().filter((unUsedResponseJson) -> !usedResponseJsons.contains(unUsedResponseJson)).collect(Collectors.toList());

        String stubsUnUsed = unUsedCycles.stream().map((unUsedCycle) -> unUsedCycle.getHttpLifeCycleAsYaml()).collect(Collectors.joining("\n"));
        String requestJsonUnUsed = unUsedRequestJsons.stream().reduce("", (start, unUsedRequestJson) -> start + "\n" + unUsedRequestJson);
        String responseJsonUnUsed = unUsedResponseJsons.stream().reduce("", (start, unUsedResponseJson) -> start + "\n" + unUsedResponseJson);
        String requestJsonUsed = usedRequestJsons.stream().reduce("", (start, unUsedRequestJson) -> start + "\n" + unUsedRequestJson);
        String responseJsonUsed = usedResponseJsons.stream().reduce("", (start, unUsedResponseJson) -> start + "\n" + unUsedResponseJson);

        String unUsed = stubsUnUsed
                + "\n" + "UsedJson:"
                + requestJsonUsed
                + responseJsonUsed
                + "\n" + "UnUsedJson:"
                + responseJsonUnUsed
                + requestJsonUnUsed;

        return unUsed;

    }

    private List<String> getResponseFiles(List<StubHttpLifecycle> unUsedCycles) {
        unUsedCycles = unUsedCycles.stream().filter((unUsedCycle) -> unUsedCycle.getResponse(false).getRawFile() != null).collect(Collectors.toList());
        return unUsedCycles.stream().map((unUsedCycle) -> unUsedCycle.getResponse(false).getRawFile().getAbsolutePath()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
    }

    private List<String> getRequestFiles(List<StubHttpLifecycle> unUsedCycles) {
        unUsedCycles = unUsedCycles.stream().filter((unUsedCycle) -> unUsedCycle.getRequest().getRawFile() != null).collect(Collectors.toList());
        return unUsedCycles.stream().map((unUsedCycle) -> unUsedCycle.getRequest().getRawFile().getAbsolutePath()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
    }
}
