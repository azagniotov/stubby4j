package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutHandlingStrategy implements AdminResponseHandlingStrategy {
   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception {

      if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
         response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         response.getWriter().println("Method PUT is not allowed on URI " + request.getRequestURI());
         return;
      }

      final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
      final String lastUriPathSegment = request.getRequestURI().substring(contextPathLength);

      // We are trying to update a stub by ID, e.g.: PUT localhost:8889/8
      if (StringUtils.isNumeric(lastUriPathSegment)) {
         final int stubIndexToUpdate = Integer.parseInt(lastUriPathSegment);

         if (!stubRepository.canMatchStubByIndex(stubIndexToUpdate)) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot update", stubIndexToUpdate);
            HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            return;
         }

         final String putPayload = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
         if (!StringUtils.isSet(putPayload)) {
            final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
            HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            return;
         }

         final String updatedCycleUrl = stubRepository.refreshStubByIndex(new YamlParser(), putPayload, stubIndexToUpdate);

         response.setStatus(HttpStatus.CREATED_201);
         response.addHeader(HttpHeader.LOCATION.asString(), updatedCycleUrl);
         final String successfulMessage = String.format("Stub request index#%s updated successfully", stubIndexToUpdate);
         response.getWriter().println(successfulMessage);

      } else {
         // We attempt to update a stub by uuid as a fallback, e.g.: UPDATE localhost:8889/9136d8b7-f7a7-478d-97a5-53292484aaf6
         if (!stubRepository.canMatchStubByUuid(lastUriPathSegment)) {
            final String errorMessage = String.format("Stub request uuid#%s does not exist, cannot update", lastUriPathSegment);
            HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            return;
         }

         final String putPayload = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
         if (!StringUtils.isSet(putPayload)) {
            final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
            HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            return;
         }

         final String updatedCycleUrl = stubRepository.refreshStubByUuid(new YamlParser(), putPayload, lastUriPathSegment);

         response.setStatus(HttpStatus.CREATED_201);
         response.addHeader(HttpHeader.LOCATION.asString(), updatedCycleUrl);
         final String successfulMessage = String.format("Stub request uuid#%s updated successfully", lastUriPathSegment);
         response.getWriter().println(successfulMessage);
      }
   }
}
