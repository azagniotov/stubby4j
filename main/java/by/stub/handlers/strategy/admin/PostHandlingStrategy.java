package by.stub.handlers.strategy.admin;

import by.stub.database.DataStore;
import by.stub.handlers.AdminHandler;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:30 PM
 */
public class PostHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final DataStore dataStore) throws Exception {

      if (!request.getRequestURI().equals(AdminHandler.ADMIN_ROOT)) {
         wrapper.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         wrapper.getWriter().println("Method POST is not allowed on URI " + request.getRequestURI());
         return;
      }

      final String post = HandlerUtils.extractPostRequestBody(request, AdminHandler.NAME);
      if (!StringUtils.isSet(post)) {
         final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(dataStore.getYamlParentDirectory(), FileUtils.constructReader(post));
      dataStore.resetStubHttpLifecycles(stubHttpLifecycles);

      if (stubHttpLifecycles.size() == 1) {
         wrapper.addHeader(HttpHeaders.LOCATION, stubHttpLifecycles.get(0).getRequest().getUrl());
      }

      wrapper.setStatus(HttpStatus.CREATED_201);
      wrapper.getWriter().println("Configuration created successfully");
   }
}