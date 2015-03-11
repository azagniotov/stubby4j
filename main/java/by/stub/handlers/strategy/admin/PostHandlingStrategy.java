package by.stub.handlers.strategy.admin;

import by.stub.database.StubbedDataManager;
import by.stub.handlers.AdminPortalHandler;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Alexander Zagniotov
 * Created: 4/25/13 11:30 PM
 */
public class PostHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubbedDataManager stubbedDataManager) throws Exception {

      if (!request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
         response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         response.getWriter().println("Method POST is not allowed on URI " + request.getRequestURI());
         return;
      }

      final String post = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
      if (!StringUtils.isSet(post)) {
         final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
         HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      stubbedDataManager.refreshStubbedData(new YamlParser(), post);

      if (stubbedDataManager.getStubHttpLifecycles().size() == 1) {
         response.addHeader(HttpHeader.LOCATION.asString(), stubbedDataManager.getOnlyStubRequestUrl());
      }

      response.setStatus(HttpStatus.CREATED_201);
      response.getWriter().println("Configuration created successfully");
   }
}